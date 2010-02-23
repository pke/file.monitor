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
import java.io.IOException;

/** 
 * Provides notification of file system changes.  Actual capabilities may
 * vary slightly by platform.
 * <p>
 * Watched files which are removed from the filesystem are no longer watched.
 * 
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 * 
 * @see FileListener
 */
public interface FileMonitor {
	
	/**
	 * Adds a file or directory to watch for changes.
	 * 
	 * @param file or directory to check for changes. If this is a directory then the <code>recursive</code> flag 
	 * indicates if a whole directory tree is watched for changes.
	 * @param mask that describes what changes should be watched. This can be a combination of the FILE_* constants.
	 * @param recursive whether or not to watch sub-directories if <code>file</code> specifies a directory.
	 * @throws IOException if the <code>file</code> could not be watched. For instance if the file or directory does not 
	 * exist. 
	 */
	void watch(File file, int mask, boolean recursive) throws IOException;

	/**
	 * Removes a file or directory from the watch list.
	 * 
	 * <p>
	 * Calling this multiple times on the same file has no effect after the file or directory was removed from the 
	 * watch list.
	 * 
	 * @param file or directory to remove from the watch list.
	 */
	void unwatch(File file);
}