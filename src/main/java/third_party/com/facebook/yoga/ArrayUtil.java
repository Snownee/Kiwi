package third_party.com.facebook.yoga;

public final class ArrayUtil {
    public static <T> void copy(T[] from, T[] to) {
        for (int i = 0; i < from.length; i++) {
            to[i] = from[i];
        }
    }

    public static void copy(float[] from, float[] to) {
        for (int i = 0; i < from.length; i++) {
            to[i] = from[i];
        }
    }

}
