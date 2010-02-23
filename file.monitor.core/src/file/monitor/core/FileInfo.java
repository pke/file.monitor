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

/**
 * Information about a watched file/directory.
 * 
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 *
 */
public class FileInfo {
	public final File file;
	public final int mask;
	public final boolean recursive;
	
	public FileInfo(final File f, final int mask, final boolean recurse) {
		this.file = f;
		this.mask = mask;
		this.recursive = recurse;
	}
	
	/**
	 * Called by the {@link AbstractFileMonitor#unwatch(File)} when it removes the file info from its list.
	 * 
	 * <p>
	 * Subclasses should free their resources associated with this info.
	 */
	public void dispose() {
	}
}