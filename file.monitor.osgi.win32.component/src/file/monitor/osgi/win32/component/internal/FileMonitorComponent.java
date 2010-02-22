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
package file.monitor.osgi.win32.component.internal;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogService;

import file.monitor.core.FileEvent;
import file.monitor.core.FileListener;
import file.monitor.win32.AbstractWindowsFileMonitor;

public class FileMonitorComponent extends AbstractWindowsFileMonitor {

	private ComponentContext context;

	AtomicReference<LogService> logRef = new AtomicReference<LogService>();

	protected void bind(LogService log) {
		logRef.set(log);
	}

	protected void unbind(LogService log) {
		logRef.compareAndSet(log, null);
	}

	protected void activate(ComponentContext context) {
		this.context = context;
	}

	protected void deactivate() {
		dispose();
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
		Object[] listeners = context.locateServices("FileListener"); //$NON-NLS-1$
		if (listeners != null) {
			for (Object listener : listeners) {
				safeNotifyListener((FileListener) listener, event);
			}
		}
	}

	@Override
	protected void onListenerException(FileListener listener, Throwable t) {
		LogService log = logRef.get();
		if (log != null) {
			log.log(LogService.LOG_ERROR, String.format(
					"Error calling listener from %s", FrameworkUtil.getBundle( //$NON-NLS-1$
							listener.getClass()).getSymbolicName()), t);
		} else {
			super.onListenerException(listener, t);
		}
	}
}
