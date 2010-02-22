package file.monitor.win32.internal;

import java.io.File;
import java.util.Map;

import com.sun.jna.examples.win32.Kernel32;
import com.sun.jna.examples.win32.Kernel32.FILE_NOTIFY_INFORMATION;
import com.sun.jna.examples.win32.Kernel32.OVERLAPPED;
import com.sun.jna.examples.win32.W32API.HANDLE;
import com.sun.jna.ptr.IntByReference;

import file.monitor.core.FileInfo;

public class Win32FileInfo extends FileInfo {
	static final int BUFFER_SIZE = 4096;

	public final HANDLE handle;
	public final FILE_NOTIFY_INFORMATION info = new FILE_NOTIFY_INFORMATION(BUFFER_SIZE);
	public final IntByReference infoLength = new IntByReference();
	public final OVERLAPPED overlapped = new OVERLAPPED();
	private Map<HANDLE, Win32FileInfo> handleMap;

	public Win32FileInfo(final File f, final HANDLE h, final int mask, final boolean recurse, Map<HANDLE, Win32FileInfo> handleMap) {
		super(f, mask, recurse);
		this.handle = h;
		this.handleMap = handleMap;
		handleMap.put(handle, this);
	}
	
	@Override
	public void dispose() {
		this.handleMap.remove(handle);
		final Kernel32 klib = Kernel32.INSTANCE;
		klib.CloseHandle(handle);
		super.dispose();
	}
}