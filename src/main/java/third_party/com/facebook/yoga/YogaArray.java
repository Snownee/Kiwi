//package third_party.com.facebook.yoga;
//
//import java.util.Arrays;
//
///**
// * Copyright (c) 2014-present, Facebook, Inc.
// * Copyright (c) 2018-present, Marius Klimantavičius
// *
// * This source code is licensed under the MIT license found in the
// * LICENSE file in the root directory of this source tree.
// */
//
//public class YogaArray<T> {
//    private T[] _array;
//
//    public T get(int index) {
//        return _array[index];
//    }
//
//    public T set(int index, T value) {
//        _array[index] = value;
//    }
//
//    public T get(Enum index) {
//        return get(index.ordinal());
//    }
//
//    public T set(Enum index, T value) {
//        set(index.ordinal(), value);
//    }
//
//    public int length() {
//        return _array.length;
//    }
//
//    public YogaArray(int length) {
//        _array = (T[]) new Object[length];
//    }
//
//    public YogaArray(T... values) {
//        _array = values;
//    }
//
//    public void CopyFrom(YogaArray<T> from) {
//        _array = Arrays.copyOf(from._array, from._array.length);
//    }
//
//    public void Clear() {
//        for (int i = 0; i < _array.length; i++)
//            _array[i] = null;
//    }
//
//    @Override
//    public int hashCode() {
//        if (_array == null)
//            return 0;
//
//        int hashCode = 0;
//        for (int i = 0; i < _array.length; i++)
//            hashCode = (hashCode << 5) ^ _array[i].hashCode();
//
//        return hashCode;
//    }
//
//    public static YogaArray<T> From<T>(T[] other)
//    {
//        var result = new T[other.Length];
//        for (int i = 0; i < result.Length; i++)
//            result[i] = other[i];
//        return new YogaArray<T>(result);
//    }
//
//    public static YogaArray<T> From<T>(YogaArray<T> other)
//    {
//        var result = new T[other.Length];
//        for (var i = 0; i < result.Length; i++)
//            result[i] = other[i];
//        return new YogaArray<T>(result);
//    }
//
//    public static boolean Equal(YogaArray<float> val1, YogaArray<float> val2)
//        {
//            var areEqual = true;
//            for (var i = 0; i < val1.Length && areEqual; ++i)
//                areEqual = FloatsEqual(val1[i], val2[i]);
//
//            return areEqual;
//        }
//
//    public static boolean Equal(YogaArray<float?> val1, YogaArray<float?> val2)
//        {
//            var areEqual = true;
//            for (var i = 0; i < val1.Length && areEqual; ++i)
//                areEqual = FloatsEqual(val1[i], val2[i]);
//
//            return areEqual;
//        }
//
//    public static boolean Equal(YogaArray<YogaValue> val1, YogaArray<YogaValue> val2)
//        {
//            boolean areEqual = true;
//            for (int i = 0; i < val1.length() && areEqual; ++i)
//                areEqual = val1[i].Equals(val2[i]);
//
//            return areEqual;
//        }
//
//    private static boolean FloatsEqual(float? a, float? b)
//        {
//            if (a != null && b != null)
//                return Math.Abs(a.Value - b.Value) < 0.0001f;
//
//            return a == null && b == null;
//        }
//}
