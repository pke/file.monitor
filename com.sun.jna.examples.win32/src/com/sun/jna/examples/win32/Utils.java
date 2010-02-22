package com.sun.jna.examples.win32;

import java.util.Date;

/**
 * 
 * Parts of this class Copyright 2002-2004 Apache Software Foundation. 
 * @author Rainer Klute (klute@rainer-klute.de) for the Apache Software Foundation (org.apache.poi.hpsf)
 */
public final class Utils {

	/**
	 * <p>The difference between the Windows epoch (1601-01-01
	 * 00:00:00) and the Unix epoch (1970-01-01 00:00:00) in
	 * milliseconds: 11644473600000L. (Use your favorite spreadsheet
	 * program to verify the correctness of this value. By the way,
	 * did you notice that you can tell from the epochs which
	 * operating system is the modern one? :-))</p>
	 */
	private static final long EPOCH_DIFF = 11644473600000L;

	/**
	 * <p>Converts a Windows FILETIME into a {@link Date}. The Windows
	 * FILETIME structure holds a date and time associated with a
	 * file. The structure identifies a 64-bit integer specifying the
	 * number of 100-nanosecond intervals which have passed since
	 * January 1, 1601. This 64-bit value is split into the two double
	 * words stored in the structure.</p>
	 *
	 * @param high The higher double word of the FILETIME structure.
	 * @param low The lower double word of the FILETIME structure.
	 * @return The Windows FILETIME as a {@link Date}.
	 */
	public static Date filetimeToDate(final int high, final int low) {
		final long filetime = (long) high << 32 | low & 0xffffffffL;
		final long ms_since_16010101 = filetime / (1000 * 10);
		final long ms_since_19700101 = ms_since_16010101 - EPOCH_DIFF;
		return new Date(ms_since_19700101);
	}

	/**
	 * <p>Converts a {@link Date} into a filetime.</p>
	 *
	 * @param date The date to be converted
	 * @return The filetime
	 * 
	 * @see #filetimeToDate
	 */
	public static long dateToFileTime(final Date date) {
		final long ms_since_19700101 = date.getTime();
		final long ms_since_16010101 = ms_since_19700101 + EPOCH_DIFF;
		return ms_since_16010101 * 1000 * 10;
	}

	private Utils() {
	}
}
