package com.sun.jna.examples.win32;

import java.io.IOException;

import com.sun.jna.examples.win32.W32API.HANDLE;
import com.sun.jna.examples.win32.W32API.SIZE_T;
import com.sun.jna.ptr.PointerByReference;

/**
 * 
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 * 
 */
public final class Kernel32Helper {

	public static void checkLastError(String message) throws RuntimeException {
		int lastError = Kernel32.INSTANCE.GetLastError();
		if (lastError != W32Errors.NO_ERROR) {
			throw new RuntimeException(String.format("%s%s%s (%d)",
					message != null ? message : "",
					message != null ? ": " : "", formatSystemError(lastError),
					lastError));
		}
	}

	public static void checkLastError() throws RuntimeException {
		checkLastError(null);
	}

	public static String formatSystemError(int code) {
		Kernel32 lib = Kernel32.INSTANCE;
		PointerByReference pref = new PointerByReference();
		try {
			lib.FormatMessage(Kernel32.FORMAT_MESSAGE_ALLOCATE_BUFFER
					| Kernel32.FORMAT_MESSAGE_FROM_SYSTEM
					| Kernel32.FORMAT_MESSAGE_IGNORE_INSERTS, null, code, 0,
					pref, 0, null);
			String s = pref.getValue().getString(0,
					!Boolean.getBoolean("w32.ascii"));
			s = s.replace(".\r", ".").replace(".\n", ".");
			return s;
		} finally {
			lib.LocalFree(pref.getValue());
		}
	}

	public interface FunctionCall<R> {
		R call();
	}

	/**
	 * Runs the given runnable and then checks the the last error if the
	 * expected result does not match the one returned by the function call.
	 * 
	 * @param call
	 * @throws RuntimeException
	 *             if the last error was set after the function call did not
	 *             returned the expected result.
	 */
	public static <R> void checkedCall(R expectedResult, FunctionCall<R> call) {
		R result = call.call();
		if (!result.equals(expectedResult)) {
			checkLastError(call.toString());
		}
	}
	
	public static <H extends HANDLE> H createHandle(FunctionCall<H> call) throws IOException {
		H handle = call.call();
		if (W32API.INVALID_HANDLE_VALUE.equals(handle)) {
			checkLastError(call.toString());
		}
		return handle;
	}

	private static final SIZE_T zeroSize = new SIZE_T(0);

	public static void checkedVirtualFreeEx(final HANDLE hProcess,
			final int lpAddress, final int dwFreeType) {
		checkedCall(true, new FunctionCall<Boolean>() {

			public Boolean call() {
				return Kernel32.INSTANCE.VirtualFreeEx(hProcess, lpAddress,
						zeroSize, dwFreeType);
			}
		});
	}

	private Kernel32Helper() {
	}
}
