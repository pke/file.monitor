/*******************************************************************************
 * Copyright (c) 2010 Philipp Kursawe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Philipp Kursawe (phil.kursawe@gmail.com) - initial API and implementation
 ******************************************************************************/
package file.monitor.windows.internal;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.sun.jna.examples.win32.Kernel32;
import com.sun.jna.examples.win32.Kernel32.FILE_NOTIFY_INFORMATION;
import com.sun.jna.examples.win32.Kernel32.OVERLAPPED;
import com.sun.jna.examples.win32.Kernel32Helper;
import com.sun.jna.examples.win32.Kernel32Helper.FunctionCall;
import com.sun.jna.examples.win32.W32API.HANDLE;
import com.sun.jna.ptr.IntByReference;

import file.monitor.core.FileEvent;
import file.monitor.core.FileInfo;
import file.monitor.core.FileListener;
import file.monitor.windows.AbstractWindowsFileMonitor;

/**
 * 
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 *
 */
public class Win32FileInfo extends FileInfo {
	static final int BUFFER_SIZE = 4096;

	private final HANDLE handle;
	private final FILE_NOTIFY_INFORMATION info = new FILE_NOTIFY_INFORMATION(BUFFER_SIZE);
	private final IntByReference infoLength = new IntByReference();
	private final OVERLAPPED overlapped = new OVERLAPPED();
	private Map<HANDLE, Win32FileInfo> handleMap;
	private final AbstractWindowsFileMonitor parent;

	private static int convertMask(final int mask) {
		int result = 0;
		if ((mask & FileEvent.FILE_CREATED) != 0) {
			result |= Kernel32.FILE_NOTIFY_CHANGE_CREATION;
		}
		if ((mask & FileEvent.FILE_DELETED) != 0) {
			result |= Kernel32.FILE_NOTIFY_CHANGE_NAME;
		}
		if ((mask & FileEvent.FILE_MODIFIED) != 0) {
			result |= Kernel32.FILE_NOTIFY_CHANGE_LAST_WRITE;
		}
		if ((mask & FileEvent.FILE_RENAMED) != 0) {
			result |= Kernel32.FILE_NOTIFY_CHANGE_NAME;
		}
		if ((mask & FileEvent.FILE_SIZE_CHANGED) != 0) {
			result |= Kernel32.FILE_NOTIFY_CHANGE_SIZE;
		}
		if ((mask & FileEvent.FILE_ACCESSED) != 0) {
			result |= Kernel32.FILE_NOTIFY_CHANGE_LAST_ACCESS;
		}
		if ((mask & FileEvent.FILE_ATTRIBUTES_CHANGED) != 0) {
			result |= Kernel32.FILE_NOTIFY_CHANGE_ATTRIBUTES;
		}
		if ((mask & FileEvent.FILE_SECURITY_CHANGED) != 0) {
			result |= Kernel32.FILE_NOTIFY_CHANGE_SECURITY;
		}
		return result;
	}
	
	public Win32FileInfo(AbstractWindowsFileMonitor parent, final File f, final HANDLE h, final int mask, final boolean recurse, Map<HANDLE, Win32FileInfo> handleMap) throws IOException {
		super(f, convertMask(mask), recurse);
		this.parent = parent;
		this.handle = h;
		this.handleMap = handleMap;
		handleMap.put(handle, this);
		
		readDirectoryChangesChecked();
	}
	
	@Override
	public void dispose() {
		this.handleMap.remove(handle);
		Kernel32.INSTANCE.CloseHandle(handle);
		parent.wakeUp();
		super.dispose();
	}
	
	private void readDirectoryChangesChecked() throws IOException {
		try {
			Kernel32Helper.checkedCall(true, new FunctionCall<Boolean>() {
				public Boolean call() {
					return Kernel32.INSTANCE.ReadDirectoryChangesW(handle, info, info.size(), recursive, mask,
							infoLength, overlapped, null);
				}
				
				@Override
				public String toString() {
					return "ReadDirectoryChangesW on " + file;
				}
			});
		} catch (RuntimeException e) {
			throw new IOException(e.getMessage());
		}
	}
	
	@SuppressWarnings("nls")
	public void handleChanges(FileListener listener) throws IOException {
		// Need an explicit read, since data was filled in asynchronously
		FILE_NOTIFY_INFORMATION fni = info;
		fni.read();
		do {
			FileEvent event = null;
			final File changedFile = new File(this.file, fni.getFilename());
			switch (fni.Action) {
			case Kernel32.FILE_ACTION_MODIFIED:
				event = new FileEvent(parent, changedFile, FileEvent.FILE_MODIFIED);
				break;
			case Kernel32.FILE_ACTION_ADDED:
				event = new FileEvent(parent, changedFile, FileEvent.FILE_CREATED);
				break;
			case Kernel32.FILE_ACTION_REMOVED:
				event = new FileEvent(parent, changedFile, FileEvent.FILE_DELETED);
				break;
			case Kernel32.FILE_ACTION_RENAMED_OLD_NAME:
				event = new FileEvent(parent, changedFile, FileEvent.FILE_NAME_CHANGED_OLD);
				break;
			case Kernel32.FILE_ACTION_RENAMED_NEW_NAME:
				event = new FileEvent(parent, changedFile, FileEvent.FILE_NAME_CHANGED_NEW);
				break;
			default:
				// TODO: other actions...
				System.err.println("Unrecognized file action '" + info.Action + "'");
			}
			if (event != null) {
				listener.fileChanged(event);
			}
			fni = fni.next();
		} while (fni != null);
		// Trigger the next read
		if (!file.exists()) {
			parent.unwatch(file);
			return;
		}

		readDirectoryChangesChecked();
	}
}