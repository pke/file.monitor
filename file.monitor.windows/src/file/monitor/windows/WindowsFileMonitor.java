/*******************************************************************************
 * Copyright (c) 2010 Philipp Kursawe and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Philipp Kursawe (phil.kursawe@gmail.com) - initial API and implementation
 ******************************************************************************/
package file.monitor.windows;

import java.util.HashSet;
import java.util.Set;

import file.monitor.core.FileEvent;
import file.monitor.core.FileListener;

/**
 * Concreate implementation of a Windows file monitor with listener management.
 * 
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 * 
 */
public class WindowsFileMonitor extends AbstractWindowsFileMonitor {

	private Set<FileListener> listeners = new HashSet<FileListener>();

	@Override
	protected void fireChangeEvent(FileEvent event) {
		for (FileListener listener : listeners) {
			safeNotifyListener(listener, event);
		}
	}

	public void addListener(FileListener listener) {
		listeners.add(listener);
	}

	public void removeListener(FileListener listener) {
		listeners.remove(listener);
	}
}
