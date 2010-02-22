package file.monitor.core;

import java.io.File;
import java.io.IOException;

/** 
 * Provides notification of file system changes.  Actual capabilities may
 * vary slightly by platform.
 * <p>
 * Watched files which are removed from the filesystem are no longer watched.
 * 
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
 * @remarks
 * Base implementation by twall for the JNA project.
 * 
 * @author <a href="mailto:kursawe@topsystem.de">Philipp Kursawe</a>
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