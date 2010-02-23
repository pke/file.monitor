package file.monitor.inotify;

import java.io.File;
import java.io.IOException;

import file.monitor.core.AbstractFileMonitor;
import file.monitor.core.FileInfo;

public abstract class AbstractINotifyFileMonitor extends AbstractFileMonitor {
	
	@Override
	protected FileInfo createWatch(File file, int eventMask, boolean recursive)
			throws IOException {
		return null;
	}
}