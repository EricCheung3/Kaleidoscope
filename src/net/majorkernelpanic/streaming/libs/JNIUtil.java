package net.majorkernelpanic.streaming.libs;

public class JNIUtil {

	static {
		System.loadLibrary("Utils");
	}

	/**
	 * Y:U:V = 4:1:1 and U and V was switched, reversible transformation
	 * 
	 * @param buffer
	 * @param width
	 * @param height
	 */
	public static void yV12ToYUV420P(byte[] buffer, int width, int height) {
		callMethod("YV12ToYUV420P", null, buffer, width, height);
	}

	/**
	 * Y: U+V = 4:2, U and V are opposite direction, reversible transformation
	 * 
	 * @param buffer
	 * @param width
	 * @param height
	 */
	public static void nV21To420SP(byte[] buffer, int width, int height) {
		callMethod("NV21To420SP", null, buffer, width, height);
	}

	/**
	 * Rotating a byte array
	 * 
	 * @param data
	 * @param offset
	 * @param width
	 * @param height
	 * @param degree
	 */
	public static void rotateMatrix(byte[] data, int offset, int width, int height, int degree) {
		callMethod("RotateByteMatrix", null, data, offset, width, height, degree);
	}

	/**
	 * Rotating two bytes array
	 * 
	 * @param data
	 * @param offset
	 * @param width
	 * @param height
	 * @param degree
	 */
	public static void rotateShortMatrix(byte[] data, int offset, int width, int height, int degree) {
		callMethod("RotateShortMatrix", null, data, offset, width, height, degree);
	}

	private static native void callMethod(String methodName, Object[] returnValue, Object... params);

}
