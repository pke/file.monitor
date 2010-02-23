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
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractFileMonitor implements FileMonitor {

	private final Map watched = new HashMap();
	
	private final Map fileMap = new HashMap();
	
	public synchronized void watch(final File file, final int eventMask, boolean recursive) throws IOException {
		FileInfo info = createWatch(file, eventMask, recursive);
		this.fileMap.put(file, info);
	}

	/**
	 * Creates a FileInfo for the given file and mask.
	 * 
	 * @param file to watch.
	 * @param eventMask FileEvent.FILE_* mask
	 * @param recursive
	 * @return Must <b>never</b> return <code>null</code>.
	 * @throws IOException
	 */
	protected abstract FileInfo createWatch(File file, int eventMask, boolean recursive) throws IOException;

	public synchronized void unwatch(final File file) {
		final FileInfo info = (FileInfo) this.fileMap.remove(file);
		if (info != null) {
			info.dispose();
		}
	}

	public synchronized void dispose() {
		int i = 0;
		for (final Object[] keys = this.fileMap.keySet().toArray(); !this.fileMap.isEmpty();) {
			unwatch((File) keys[i++]);
		}
	}

	public void addWatch(final File dir) throws IOException {
		addWatch(dir, FileEvent.FILE_ANY);
	}

	public void addWatch(final File dir, final int mask) throws IOException {
		addWatch(dir, mask, dir.isDirectory());
	}

	public void addWatch(final File dir, final int mask, final boolean recursive) throws IOException {
		this.watched.put(dir, new Integer(mask));
		watch(dir, mask, recursive);
	}

	public void removeWatch(final File file) {
		if (this.watched.remove(file) != null) {
			unwatch(file);
		}
	}
	
	public boolean isEmpty() {
		return fileMap.isEmpty();
	}

	protected abstract void fireChangeEvent(final FileEvent event);
	
	public void onListenerException(FileListener listener, Throwable t) {
		t.printStackTrace(System.err);
	}

	public void safeNotifyListener(FileListener listener, FileEvent event) {
		try {
			listener.fileChanged(event);
		} catch (Throwable t) {
			onListenerException(listener, t);
		}
	}
}
