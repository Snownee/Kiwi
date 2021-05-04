package snownee.kiwi.util;

public class MutablePair<T> extends org.apache.commons.lang3.tuple.MutablePair<T, T> {
	public void set(int index, T value) {
		if (index == 0) {
			setLeft(value);
		} else if (index == 1) {
			setRight(value);
		} else {
			throw new IndexOutOfBoundsException();
		}
	}

	public T get(int index) {
		if (index == 0) {
			return getLeft();
		}
		if (index == 1) {
			return getRight();
		}
		throw new IndexOutOfBoundsException();
	}
}
