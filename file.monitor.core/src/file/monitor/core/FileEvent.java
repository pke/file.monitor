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
package file.monitor.core;

import java.io.File;
import java.util.EventObject;

/**
 * Base event for file monitor changes.
 * 
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 *
 */
public class FileEvent extends EventObject {
	public static final int FILE_CREATED = 0x1;
	public static final int FILE_DELETED = 0x2;
	public static final int FILE_MODIFIED = 0x4;
	public static final int FILE_ACCESSED = 0x8;
	public static final int FILE_NAME_CHANGED_OLD = 0x10;
	public static final int FILE_NAME_CHANGED_NEW = 0x20;
	public static final int FILE_RENAMED = FILE_NAME_CHANGED_OLD | FILE_NAME_CHANGED_NEW;
	public static final int FILE_SIZE_CHANGED = 0x40;
	public static final int FILE_ATTRIBUTES_CHANGED = 0x80;
	public static final int FILE_SECURITY_CHANGED = 0x100;
	public static final int FILE_ANY = 0x1FF;
	
	private static final long serialVersionUID = 1988162020776700581L;
	private final File file;
	private final int type;

	public FileEvent(final FileMonitor source, final File file, final int type) {
		super(source);
		this.file = file;
		this.type = type;
	}

	public File getFile() {
		return this.file;
	}

	public int getType() {
		return this.type;
	}

	public String toString() {
		return "FileEvent: " + this.file + ":" + this.type;
	}
}