package com.sun.jna.examples.win32.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sun.jna.examples.win32.Kernel32;
import com.sun.jna.examples.win32.W32API;
import com.sun.jna.examples.win32.W32API.HANDLE;

public class W32APITests {

	@Test
	public void handleShouldBeInvalid() {
		HANDLE handle = Kernel32.INSTANCE.CreateFile(null, 0, 0, null, 0, 0, null);
		assertEquals(W32API.INVALID_HANDLE_VALUE, handle);
	}
}
