package third_party.com.facebook.yoga;

public final class ArrayUtil {
	public static <T> void copy(T[] from, T[] to) {
		System.arraycopy(from, 0, to, 0, from.length);
	}

	public static void copy(float[] from, float[] to) {
		System.arraycopy(from, 0, to, 0, from.length);
	}

}
