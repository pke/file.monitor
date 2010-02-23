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
package file.monitor.osgi.event.internal;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicReference;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import file.monitor.core.FileEvent;
import file.monitor.core.FileListener;
import file.monitor.osgi.event.FileMonitorEventContants;

public class FileListenerImpl implements FileListener {
	
	AtomicReference<EventAdmin> ref = new AtomicReference<EventAdmin>();

	protected void bind(EventAdmin eventAdmin) {
		ref.set(eventAdmin);
	}
	
	protected void unbind(EventAdmin eventAdmin) {
		ref.compareAndSet(eventAdmin, null);
	}
	

	public void fileChanged(final FileEvent e) {
		EventAdmin eventAdmin = ref.get();
		if (eventAdmin != null) {
			final Dictionary<String, Object> properties = new Hashtable<String, Object>();
			properties.put(FileMonitorEventContants.FILEMONITOR_EVENT_FILENAME_PROPERTY, e.getFile().getAbsolutePath());
			String topic;
			switch (e.getType()) {
			case FileEvent.FILE_CREATED:
				topic = FileMonitorEventContants.FILEMONITOR_EVENT_TOPIC_CREATED;
				break;
			case FileEvent.FILE_DELETED:
				topic = FileMonitorEventContants.FILEMONITOR_EVENT_TOPIC_DELETED;
				break;
			case FileEvent.FILE_MODIFIED:
				topic = FileMonitorEventContants.FILEMONITOR_EVENT_TOPIC_MODIFIED;
				break;
			case FileEvent.FILE_ACCESSED:
				topic = FileMonitorEventContants.FILEMONITOR_EVENT_TOPIC_ACCESSED;
				break;
			default:
				return;
			}
			eventAdmin.postEvent(new Event(topic, properties));
		}
	}
}
