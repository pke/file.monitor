package file.monitor.core;

import java.io.File;

public class FileInfo {
	public final File file;
	public final int notifyMask;
	public final boolean recursive;
	
	public FileInfo(final File f, final int mask, final boolean recurse) {
		this.file = f;
		this.notifyMask = mask;
		this.recursive = recurse;
	}
	
	public void dispose() {
	}
}