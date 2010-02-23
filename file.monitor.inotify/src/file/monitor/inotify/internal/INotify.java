package file.monitor.inotify.internal;

import com.sun.jna.Native;
import com.sun.jna.Structure;

public interface INotify {
	INotify INSTANCE = (INotify) Native.loadLibrary("inotify", INotify.class);

	class inotify_event extends Structure {
		public int wd;		/* watch descriptor */
		public int mask;		/* watch mask */
		public int cookie;		/* cookie to synchronize two events */
		public int len;		/* length (including nulls) of name */
		public byte name[] = new byte[0];	/* stub for possible name */
	};

	int IN_ACCESS = 0x00000001	/* File was accessed */;
	int IN_MODIFY = 0x00000002	/* File was modified */;
	int IN_ATTRIB = 0x00000004	/* Metadata changed */;
	int IN_CLOSE_WRITE = 0x00000008	/* Writtable file was closed */;
	int IN_CLOSE_NOWRITE = 0x00000010	/* Unwrittable file closed */;
	int IN_OPEN = 0x00000020	/* File was opened */;
	int IN_MOVED_FROM = 0x00000040	/* File was moved from X */;
	int IN_MOVED_TO = 0x00000080	/* File was moved to Y */;
	int IN_CREATE = 0x00000100	/* Subfile was created */;
	int IN_DELETE = 0x00000200	/* Subfile was deleted */;
	int IN_DELETE_SELF = 0x00000400	/* Self was deleted */;

	/* the following are legal events.  they are sent as needed to any watch */
	int IN_UNMOUNT = 0x00002000	/* Backing fs was unmounted */;
	int IN_Q_OVERFLOW = 0x00004000	/* Event queued overflowed */;
	int IN_IGNORED = 0x00008000	/* File was ignored */;

	/* helper events */
	int IN_CLOSE = (IN_CLOSE_WRITE | IN_CLOSE_NOWRITE) /* close */;
	int IN_MOVE = (IN_MOVED_FROM | IN_MOVED_TO) /* moves */;

	/* special flags */
	int IN_ISDIR = 0x40000000	/* event occurred against dir */;
	int IN_ONESHOT = 0x80000000	/* only send event once */;

}
