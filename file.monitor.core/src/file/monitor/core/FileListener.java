package file.monitor.core;

import java.util.EventListener;

/**
 * Listener interface 
 * 
 * 
 */
public interface FileListener extends EventListener {

	public void fileChanged(FileEvent event);
}
