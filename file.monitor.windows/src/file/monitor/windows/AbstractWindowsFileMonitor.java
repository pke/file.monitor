/*******************************************************************************
 * Copyright (c) 2010 Philipp Kursawe and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   twall (JNA project) - initial API and implementation
 *   Philipp Kursawe (phil.kursawe@gmail.com) - different exception handling
 ******************************************************************************/
package file.monitor.windows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.sun.jna.examples.win32.Kernel32;
import com.sun.jna.examples.win32.W32API;
import com.sun.jna.examples.win32.W32API.HANDLE;
import com.sun.jna.examples.win32.W32API.HANDLEByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import file.monitor.core.AbstractFileMonitor;
import file.monitor.core.FileEvent;
import file.monitor.core.FileListener;
import file.monitor.windows.internal.Win32FileInfo;

/**
 * Implements file monitoring for the Windows platform.
 *
 * <p>
 * Subclasses must implement listener management.
 * 
 * @remarks
 * Base implementation by twall for the JNA project.
 *
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 *
 */
public abstract class AbstractWindowsFileMonitor extends AbstractFileMonitor {
	private Thread watcher;
	private HANDLE port;
	private final Map<HANDLE, Win32FileInfo> handleMap = new HashMap<HANDLE, Win32FileInfo>();

	private final FileListener changeListener = new FileListener() { 
		public void fileChanged(FileEvent event) {
			fireChangeEvent(event);
		}
	};

	private Win32FileInfo waitForChange() {
		final IntByReference rcount = new IntByReference();
		final HANDLEByReference rkey = new HANDLEByReference();
		final PointerByReference roverlap = new PointerByReference();
		Kernel32.INSTANCE.GetQueuedCompletionStatus(this.port, rcount, rkey, roverlap, Kernel32.INFINITE);

		synchronized (this) {
			return this.handleMap.get(rkey.getValue());
		}
	}

	private static int watcherThreadID;

	@Override
	protected Win32FileInfo createWatch(File file, int eventMask,
			boolean recursive) throws IOException {
		File dir = file;
		if (!dir.isDirectory()) {
			recursive = false;
			dir = file.getParentFile();
		}
		while (dir != null && !dir.exists()) {
			recursive = true;
			dir = dir.getParentFile();
		}
		if (dir == null) {
			throw new FileNotFoundException("No ancestor found for " + file);
		}
		final Kernel32 klib = Kernel32.INSTANCE;
		final int mask = Kernel32.FILE_SHARE_READ | Kernel32.FILE_SHARE_WRITE | Kernel32.FILE_SHARE_DELETE;
		final int flags = Kernel32.FILE_FLAG_BACKUP_SEMANTICS | Kernel32.FILE_FLAG_OVERLAPPED;
		final HANDLE handle = klib.CreateFile(file.getAbsolutePath(), Kernel32.FILE_LIST_DIRECTORY, mask, null,
				Kernel32.OPEN_EXISTING, flags, null);
		if (W32API.INVALID_HANDLE_VALUE.equals(handle)) {
			throw new IOException("Unable to open " + file + " (" + klib.GetLastError() + ")");
		}
		// Existing port is returned
		this.port = klib.CreateIoCompletionPort(handle, this.port, handle.getPointer(), 0);
		if (W32API.INVALID_HANDLE_VALUE.equals(this.port)) {
			throw new IOException("Unable to create/use I/O Completion port " + "for " + file + " ("
					+ klib.GetLastError() + ")");
		}
		
		final Win32FileInfo finfo = new Win32FileInfo(this, file, handle, eventMask, recursive, handleMap);
				
		if (this.watcher == null) {
			this.watcher = new Thread("Win32 File Monitor-" + watcherThreadID++) {
				@Override
				public void run() {
					Win32FileInfo finfo;
					while (true) {
						finfo = waitForChange();
						if (finfo == null) {
							synchronized (AbstractWindowsFileMonitor.this) {
								if (AbstractWindowsFileMonitor.this.isEmpty()) {
									AbstractWindowsFileMonitor.this.watcher = null;
									break;
								}
							}
							continue;
						}

						try {
							finfo.handleChanges(changeListener);
						} catch (final IOException e) {
							// TODO: how is this best handled?
							e.printStackTrace();
						}
					}
				}
			};
			this.watcher.setDaemon(true);
			this.watcher.start();
		}
		return finfo;
	}
	
	public void wakeUp() {
		Kernel32.INSTANCE.PostQueuedCompletionStatus(this.port, 0, null, null);
	}

	public synchronized void dispose() {
		super.dispose();

		wakeUp();
		
		Kernel32.INSTANCE.CloseHandle(this.port);
		this.port = null;
		this.watcher = null;
	}
}
