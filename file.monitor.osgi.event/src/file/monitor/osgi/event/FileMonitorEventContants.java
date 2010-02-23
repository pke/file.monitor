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
package file.monitor.osgi.event;

@SuppressWarnings("nls")
public final class FileMonitorEventContants {

	public static final String FILEMONITOR_EVENT_TOPIC_BASE = "file/monitor/event/";
	public static final String FILEMONITOR_EVENT_TOPIC_CREATED = FILEMONITOR_EVENT_TOPIC_BASE + "CREATED";
	public static final String FILEMONITOR_EVENT_TOPIC_DELETED = FILEMONITOR_EVENT_TOPIC_BASE + "DELETED";
	public static final String FILEMONITOR_EVENT_TOPIC_MODIFIED = FILEMONITOR_EVENT_TOPIC_BASE + "MODIFIED";
	public static final String FILEMONITOR_EVENT_TOPIC_ACCESSED = FILEMONITOR_EVENT_TOPIC_BASE + "ACCESSED";
	public static final String FILEMONITOR_EVENT_TOPIC_RENAMED = FILEMONITOR_EVENT_TOPIC_BASE + "RENAMED";
	public static final String FILEMONITOR_EVENT_TOPIC_SIZE_CHANGED = FILEMONITOR_EVENT_TOPIC_BASE + "SIZE_CHANGED";
	public static final String FILEMONITOR_EVENT_TOPIC_ATTRIBUTES_CHANGED = FILEMONITOR_EVENT_TOPIC_BASE
			+ "ATTRIBUTES_CHANGED";
	public static final String FILEMONITOR_EVENT_SECURITY_CHANGED = FILEMONITOR_EVENT_TOPIC_BASE + "SECURITY_CHANGED";
	public static final String FILEMONITOR_EVENT_TOPIC_ALL = FILEMONITOR_EVENT_TOPIC_BASE + "*";

	public static final String FILEMONITOR_EVENT_FILENAME_PROPERTY = "file.name";
	public static final String FILEMONITOR_EVENT_RENAME_OLD_NAME_PROPERTY = "file.oldname";
	public static final String FILEMONITOR_EVENT_RENAME_NEW_NAME_PROPERTY = "file.newname";
}
