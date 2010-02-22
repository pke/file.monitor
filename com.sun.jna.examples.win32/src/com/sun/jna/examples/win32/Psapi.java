package com.sun.jna.examples.win32;

import com.sun.jna.Native;

public interface Psapi extends W32API {
	Psapi INSTANCE = (Psapi) Native.loadLibrary(
			"psapi", Psapi.class, DEFAULT_OPTIONS); //$NON-NLS-1$

	int GetModuleFileNameEx(HANDLE hProcess, HMODULE hModule,
			char[] lpFilename, int nSize);

	int GetModuleFileNameEx(HANDLE hProcess, HMODULE hModule,
			byte[] lpFilename, int nSize);

}
