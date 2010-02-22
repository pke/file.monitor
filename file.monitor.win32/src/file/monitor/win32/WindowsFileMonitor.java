package file.monitor.win32;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.sun.jna.examples.win32.Kernel32;
import com.sun.jna.examples.win32.Kernel32Helper;
import com.sun.jna.examples.win32.W32API;
import com.sun.jna.examples.win32.Kernel32.FILE_NOTIFY_INFORMATION;
import com.sun.jna.examples.win32.Kernel32Helper.FunctionCall;
import com.sun.jna.examples.win32.W32API.HANDLE;
import com.sun.jna.examples.win32.W32API.HANDLEByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import file.monitor.core.AbstractFileMonitor;
import file.monitor.core.FileEvent;
import file.monitor.win32.internal.Win32FileInfo;

public abstract class WindowsFileMonitor extends AbstractFileMonitor {

	
	private Thread watcher;
	private HANDLE port;
	private final Map<HANDLE, Win32FileInfo> handleMap = new HashMap<HANDLE, Win32FileInfo>();

	@SuppressWarnings("nls")
	protected void handleChanges(final Win32FileInfo finfo) throws IOException {
		FILE_NOTIFY_INFORMATION fni = finfo.info;
		// Need an explicit read, since data was filled in asynchronously
		fni.read();
		do {
			FileEvent event = null;
			final File file = new File(finfo.file, fni.getFilename());
			switch (fni.Action) {
			case Kernel32.FILE_ACTION_MODIFIED:
				event = new FileEvent(this, file, FileEvent.FILE_MODIFIED);
				break;
			case Kernel32.FILE_ACTION_ADDED:
				event = new FileEvent(this, file, FileEvent.FILE_CREATED);
				break;
			case Kernel32.FILE_ACTION_REMOVED:
				event = new FileEvent(this, file, FileEvent.FILE_DELETED);
				break;
			case Kernel32.FILE_ACTION_RENAMED_OLD_NAME:
				event = new FileEvent(this, file, FileEvent.FILE_NAME_CHANGED_OLD);
				break;
			case Kernel32.FILE_ACTION_RENAMED_NEW_NAME:
				event = new FileEvent(this, file, FileEvent.FILE_NAME_CHANGED_NEW);
				break;
			default:
				// TODO: other actions...
				System.err.println("Unrecognized file action '" + fni.Action + "'");
			}
			if (event != null) {
				fireChangeEvent(event);
			}
			fni = fni.next();
		} while (fni != null);
		// Trigger the next read
		if (!finfo.file.exists()) {
			unwatch(finfo.file);
			return;
		}

		readDirectoryChangesChecked(finfo);
	}

	private void readDirectoryChangesChecked(final Win32FileInfo finfo) throws IOException {
		try {
			Kernel32Helper.checkedCall(true, new FunctionCall<Boolean>() {
				public Boolean call() {
					return Kernel32.INSTANCE.ReadDirectoryChangesW(finfo.handle, finfo.info, finfo.info.size(), finfo.recursive, finfo.notifyMask,
							finfo.infoLength, finfo.overlapped, null);
				}
				
				@Override
				public String toString() {
					return "ReadDirectoryChangesW on " + finfo.file;
				}
			});
		} catch (RuntimeException e) {
			throw new IOException(e.getMessage());
		}
	}

	private Win32FileInfo waitForChange() {
		final Kernel32 klib = Kernel32.INSTANCE;
		final IntByReference rcount = new IntByReference();
		final HANDLEByReference rkey = new HANDLEByReference();
		final PointerByReference roverlap = new PointerByReference();
		klib.GetQueuedCompletionStatus(this.port, rcount, rkey, roverlap, Kernel32.INFINITE);

		synchronized (this) {
			return this.handleMap.get(rkey.getValue());
		}
	}

	private int convertMask(final int mask) {
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
		final int notifyMask = convertMask(eventMask);
		final Win32FileInfo finfo = new Win32FileInfo(file, handle, notifyMask, recursive, handleMap);
		// Existing port is returned
		this.port = klib.CreateIoCompletionPort(handle, this.port, handle.getPointer(), 0);
		if (W32API.INVALID_HANDLE_VALUE.equals(this.port)) {
			throw new IOException("Unable to create/use I/O Completion port " + "for " + file + " ("
					+ klib.GetLastError() + ")");
		}
		
		readDirectoryChangesChecked(finfo);
		
		if (this.watcher == null) {
			this.watcher = new Thread("Win32 File Monitor-" + watcherThreadID++) {
				@Override
				public void run() {
					Win32FileInfo finfo;
					while (true) {
						finfo = waitForChange();
						if (finfo == null) {
							synchronized (WindowsFileMonitor.this) {
								if (WindowsFileMonitor.this.isEmpty()) {
									WindowsFileMonitor.this.watcher = null;
									break;
								}
							}
							continue;
						}

						try {
							handleChanges(finfo);
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

	protected synchronized void dispose() {
		super.dispose();

		final Kernel32 klib = Kernel32.INSTANCE;
		klib.PostQueuedCompletionStatus(this.port, 0, null, null);
		klib.CloseHandle(this.port);
		this.port = null;
		this.watcher = null;
	}
}
