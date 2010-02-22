package file.monitor.osgi.example.log.internal;

import java.util.concurrent.atomic.AtomicReference;

import org.osgi.service.log.LogService;

import file.monitor.core.FileEvent;
import file.monitor.core.FileListener;

public class FileListenerImpl implements FileListener {
	AtomicReference<LogService> logRef = new AtomicReference<LogService>();
	
	protected void bind(LogService log) {
		logRef.set(log);
	}
	
	protected void unbind(LogService log) {
		logRef.compareAndSet(log, null);
	}
	
	public void fileChanged(final FileEvent e) {
		LogService log = logRef.get();
		if (log != null) {
			log.log(LogService.LOG_DEBUG, e.toString());
		}
	}
}
