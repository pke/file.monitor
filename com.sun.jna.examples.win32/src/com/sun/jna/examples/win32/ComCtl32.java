package com.sun.jna.examples.win32;

import com.sun.jna.Native;
import com.sun.jna.Structure;

/**
 * Incomplete interface to the Common Controls Library (comctl32.dll)
 * 
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 * 
 */
public interface ComCtl32 extends W32API {
	ComCtl32 INSTANCE = (ComCtl32) Native.loadLibrary("comctl32",
			ComCtl32.class, DEFAULT_OPTIONS);

	class TCITEM extends Structure {
		public int mask;
		public int dwState;
		public int dwStateMask;
		public int pszText;
		public int cchTextMax;
		public int iImage;
		public LPARAM lParam;
	}

	int TCIF_TEXT = 0x0001;
	int TCIF_IMAGE = 0x0002;
	int TCIF_PARAM = 0x0008;
	int TCIF_STATE = 0x0010;
	int TCM_GETITEMA = ComCtl32.TCM_FIRST + 5;
	int TCM_GETITEMW = ComCtl32.TCM_FIRST + 60;
	int TCM_GETITEM = UNICODE_OPTIONS.equals(DEFAULT_OPTIONS) ? TCM_GETITEMW
			: TCM_GETITEMA;
	int TCM_FIRST = 0x1300;
	int TCM_GETITEMCOUNT = TCM_FIRST + 4;

}
