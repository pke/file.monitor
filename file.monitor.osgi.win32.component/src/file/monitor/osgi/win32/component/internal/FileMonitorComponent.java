package file.monitor.osgi.win32.component.internal;

import java.io.File;
import java.io.IOException;

import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

import file.monitor.core.FileEvent;
import file.monitor.core.FileListener;
import file.monitor.win32.WindowsFileMonitor;

public class FileMonitorComponent extends WindowsFileMonitor {
	
	private ComponentContext context;

	protected void activate(ComponentContext context) {
		this.context = context;
	}
	
	protected void deactivate() {
		dispose();
	}
	
	interface FileRunnable {
		void run(File object) throws Exception;
	}
	
	private void runForFile(ServiceReference ref, FileRunnable runnable) {
		Object property = ref.getProperty("file.name");
		String files[] = null;
		if (property instanceof String) {
			files = new String[]{(String) property};
		} else if (property instanceof String[]) {
			files = (String[]) property;
		}
		if (files != null) {
			for (int i=0; i<files.length; ++i) {
				try {
					runnable.run(new File(files[i]));
				} catch (Exception e) {
				}
			}
		}

	}
	
	protected void bind(ServiceReference ref) {
		runForFile(ref, new FileRunnable() {
			public void run(File file) throws IOException {
				addWatch(file);
			}
		});
	}
	
	protected void unbind(ServiceReference ref) {
		runForFile(ref, new FileRunnable() {
			public void run(File file) throws IOException {
				removeWatch(file);
			}
		});
	}

	protected void fireChangeEvent(FileEvent event) {
		Object[] listeners = context.locateServices("FileListener");
		if (listeners != null) {
			for (int i=0; i<listeners.length; ++i) {
				try {
					((FileListener)listeners[i]).fileChanged(event);
				} catch (Throwable t) {					
				}
			}
		}
	}
}
