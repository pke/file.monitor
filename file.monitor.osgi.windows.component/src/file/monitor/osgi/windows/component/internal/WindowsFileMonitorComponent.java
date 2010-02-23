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
package file.monitor.osgi.windows.component.internal;

import java.io.File;

import file.monitor.core.AbstractFileMonitor;
import file.monitor.core.FileEvent;
import file.monitor.core.FileListener;
import file.monitor.osgi.component.FileMonitorComponent;
import file.monitor.windows.AbstractWindowsFileMonitor;

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
public class WindowsFileMonitorComponent extends FileMonitorComponent {

	@Override
	protected AbstractFileMonitor createMonitor() {
		return new AbstractWindowsFileMonitor() {
			@Override
			protected void fireChangeEvent(FileEvent event) {
				WindowsFileMonitorComponent.this.fireChangeEvent(event);
			}
			
			@Override
			public void onListenerException(FileListener listener, Throwable t) {
				WindowsFileMonitorComponent.this.onListenerException(listener, t);
			}
		};
	}
}
