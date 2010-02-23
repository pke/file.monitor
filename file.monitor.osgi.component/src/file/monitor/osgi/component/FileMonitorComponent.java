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
package file.monitor.osgi.component;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogService;

import file.monitor.core.AbstractFileMonitor;
import file.monitor.core.FileEvent;
import file.monitor.core.FileListener;

/**
 * <p>
 * This OSGi service consumes {@link FileListener} services. Such a listener service can optionally provide a 
 * <code>file.name</code> property to specify the file or directory that this listener wants to add to the list of 
 * watches. If the listener service does not specify this property, then it is informed about all changes of all 
 * currently watched files and directories. If no listener service specifies such a property, then nothing is watched 
 * unless someone calls {@link #watch(File, int, boolean)} directly.
 * 
 * @TODO:
 * Use CM support to add file/directory watches
 * 
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 *
 */
public abstract class FileMonitorComponent  {

	private ComponentContext context;

	AtomicReference<LogService> logRef = new AtomicReference<LogService>();
	
	private final AbstractFileMonitor monitor ;
	private File[] lastWatches;

	public FileMonitorComponent() {
		this.monitor = createMonitor(); 
	}
	
	protected abstract AbstractFileMonitor createMonitor();

	protected void bind(LogService log) {
		logRef.set(log);
	}

	protected void unbind(LogService log) {
		logRef.compareAndSet(log, null);
	}

	protected void activate(ComponentContext context) {
		this.context = context;
		getWatches(context.getProperties().get("watch")); //$NON-NLS-1$
	}

	protected void deactivate() {
		monitor.dispose();
	}

	interface FileRunnable {
		void run(File object) throws IOException;
	}

	private void runForFile(ServiceReference ref, FileRunnable runnable) {
		Object property = ref.getProperty("file.name"); //$NON-NLS-1$
		String files[] = null;
		if (property instanceof String) {
			files = new String[] { (String) property };
		} else if (property instanceof String[]) {
			files = (String[]) property;
		}
		if (files != null) {
			for (String file : files) {
				try {
					runnable.run(new File(file));
				} catch (Exception e) {
				}
			}
		}

	}

	protected void addListener(ServiceReference ref) {
		runForFile(ref, new FileRunnable() {
			public void run(File file) throws IOException {
				monitor.addWatch(file);
			}
		});
	}

	protected void removeListener(ServiceReference ref) {
		runForFile(ref, new FileRunnable() {
			public void run(File file) throws IOException {
				monitor.removeWatch(file);
			}
		});
	}

	protected void fireChangeEvent(FileEvent event) {
		Object[] listeners = context.locateServices("FileListener"); //$NON-NLS-1$
		if (listeners != null) {
			for (Object listener : listeners) {
				monitor.safeNotifyListener((FileListener) listener, event);
			}
		}
	}

	protected void onListenerException(FileListener listener, Throwable t) {
		if (!logError(t, String.format(
				"Error calling listener from %s", FrameworkUtil.getBundle( //$NON-NLS-1$
						listener.getClass()).getSymbolicName()))) {
			monitor.onListenerException(listener, t);
		}
	}
	
	private boolean logError(Throwable t, String message, Object... args) {
		LogService log = logRef.get();
		if (log != null) {
			log.log(context.getServiceReference(), LogService.LOG_ERROR, String.format(message, args), t);
			return true;
		}
		return false;
	}
	
	protected void getWatches(Object values) {
		String watches[] = null;
		if (values instanceof String[]) {
			watches = (String[])values;
		} else if (values instanceof String) {
			watches = new String[]{(String) values};
		}
		
		if (lastWatches != null) {
			for (File watch : lastWatches) {
				if (watch != null) {
					monitor.removeWatch(watch);
				}
			}
			this.lastWatches = null;
		}
		
		if (watches != null) {
			this.lastWatches = new File[watches.length];
			for (int i=0; i<watches.length; ++i) {
				lastWatches[i] = new File(watches[i]);
				try {
					monitor.addWatch(lastWatches[i]);
				} catch (IOException e) {
					logError(e, "Could not add watch for %s", watches[i]); //$NON-NLS-1$
				}
			}
		}
	}
	
	protected void modified(Map<?, ?> config) {
		getWatches(config.get("watch")); //$NON-NLS-1$
	}
}
