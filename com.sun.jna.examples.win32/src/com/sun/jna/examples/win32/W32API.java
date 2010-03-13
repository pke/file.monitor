package com.sun.jna.examples.win32;

import java.util.HashMap;
import java.util.Map;

import com.sun.jna.FromNativeContext;
import com.sun.jna.IntegerType;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.ByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;

/** Base type for most W32 API libraries.  Provides standard options
 * for unicode/ASCII mappings.  Set the system property <code>w32.ascii</code>
 * to <code>true</code> to default to the ASCII mappings.
 */
@SuppressWarnings("serial")
public interface W32API extends StdCallLibrary, W32Errors {

	/** Standard options to use the unicode version of a w32 API. */
	Map<String, Object> UNICODE_OPTIONS = new HashMap<String, Object>() {
		{
			put(OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
			put(OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.UNICODE);
		}
	};
	/** Standard options to use the ASCII/MBCS version of a w32 API. */
	Map<String, Object> ASCII_OPTIONS = new HashMap<String, Object>() {
		{
			put(OPTION_TYPE_MAPPER, W32APITypeMapper.ASCII);
			put(OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.ASCII);
		}
	};
	Map<String, Object> DEFAULT_OPTIONS = Boolean.getBoolean("w32.ascii") ? ASCII_OPTIONS : UNICODE_OPTIONS; //$NON-NLS-1$
	boolean isUnicode = DEFAULT_OPTIONS.equals(UNICODE_OPTIONS);

	class HANDLE extends PointerType {
		private boolean immutable;

		public HANDLE() {
		}

		public HANDLE(final Pointer p) {
			setPointer(p);
			immutable = true;
		}

		/** Override to the appropriate object for INVALID_HANDLE_VALUE. */
		@Override
		public Object fromNative(final Object nativeValue, final FromNativeContext context) {
			final Object o = super.fromNative(nativeValue, context);
			if (INVALID_HANDLE_VALUE.equals(o)) {
				return INVALID_HANDLE_VALUE;
			}
			return o;
		}

		@Override
		public void setPointer(final Pointer p) {
			if (immutable) {
				throw new UnsupportedOperationException("immutable reference"); //$NON-NLS-1$
			}
			super.setPointer(p);
		}
	}

	class WORD extends IntegerType {
		public WORD() {
			this(0);
		}

		public WORD(final long value) {
			super(2, value);
		}
	}

	class DWORD extends IntegerType {
		public DWORD() {
			this(0);
		}

		public DWORD(final long value) {
			super(4, value);
		}
	}

	class LONG extends IntegerType {
		public LONG() {
			this(0);
		}

		public LONG(final long value) {
			super(Native.LONG_SIZE, value);
		}
	}

	class HDC extends HANDLE {
	}

	class HICON extends HANDLE {
	}

	class HBITMAP extends HANDLE {
	}

	class HRGN extends HANDLE {
	}

	class HWND extends HANDLE {
		public HWND() {
		}

		public HWND(final Pointer p) {
			super(p);
		}
	}

	class HINSTANCE extends HANDLE {
	}

	class HMODULE extends HINSTANCE {
	}

	class HRESULT extends NativeLong {
		public boolean succeeded() {
			return intValue() >= 0;
		}

		public boolean failed() {
			return intValue() < 0;
		}
	}

	/** Constant value representing an invalid HANDLE. */
	HANDLE INVALID_HANDLE_VALUE = new HANDLE(Pointer.createConstant(0xffffffffL));

	/** Special HWND value. */
	HWND HWND_BROADCAST = new HWND(Pointer.createConstant(0xFFFF));

	/** LPHANDLE */
	class HANDLEByReference extends ByReference {
		public HANDLEByReference() {
			this(null);
		}

		public HANDLEByReference(final HANDLE h) {
			super(Pointer.SIZE);
			setValue(h);
		}

		public void setValue(final HANDLE h) {
			getPointer().setPointer(0, h != null ? h.getPointer() : null);
		}

		public HANDLE getValue() {
			final Pointer p = getPointer().getPointer(0);
			if (p == null) {
				return null;
			}
			if (INVALID_HANDLE_VALUE.getPointer().equals(p)) {
				return INVALID_HANDLE_VALUE;
			}
			final HANDLE h = new HANDLE();
			h.setPointer(p);
			return h;
		}
	}

	class LONG_PTR extends IntegerType {
		public LONG_PTR() {
			this(0);
		}

		public LONG_PTR(final long value) {
			super(Pointer.SIZE, value);
		}
	}

	class SSIZE_T extends LONG_PTR {
		public SSIZE_T() {
			this(0);
		}

		public SSIZE_T(final long value) {
			super(value);
		}
	}

	class ULONG_PTR extends IntegerType {
		public ULONG_PTR() {
			this(0);
		}

		public ULONG_PTR(final long value) {
			super(Pointer.SIZE, value);
		}
	}

	class SIZE_T extends ULONG_PTR {
		public SIZE_T() {
			this(0);
		}

		public SIZE_T(final long value) {
			super(value);
		}
	}

	class LPARAM extends LONG_PTR {
		public LPARAM() {
			this(0);
		}

		public LPARAM(final long value) {
			super(value);
		}
	}

	class LRESULT extends LONG_PTR {
		public LRESULT() {
			this(0);
		}

		public LRESULT(final long value) {
			super(value);
		}
	}

	class UINT_PTR extends IntegerType {
		public UINT_PTR() {
			super(Pointer.SIZE);
		}

		public UINT_PTR(final long value) {
			super(Pointer.SIZE, value);
		}

		public Pointer toPointer() {
			return Pointer.createConstant(longValue());
		}
	}

	class WPARAM extends UINT_PTR {
		public WPARAM() {
			this(0);
		}

		public WPARAM(final long value) {
			super(value);
		}
		
		static long makeLong(int low, int high) {
			return ((long)(((short)((int)(low) & 0xffff)) | ((int)((short)((int)(high) & 0xffff))) << 16));
		}
		
		public WPARAM(int low, int high) {
			super(makeLong(low, high));
		}
	}
}