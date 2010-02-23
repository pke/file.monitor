/*******************************************************************************
 * Copyright (c) 2010 Marcus Ilgner and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Marcus Ilgner (phil.kursawe@gmail.com) - initial API and implementation
 ******************************************************************************/

package file.monitor.inotify.internal;

import java.io.File;

import file.monitor.core.FileInfo;

/**
 * @author Marcus Ilgner
 */
public class INotifyFileInfo extends FileInfo {

	public INotifyFileInfo(File f, int mask, boolean recurse) {
		super(f, mask, recurse);		
	}

}
