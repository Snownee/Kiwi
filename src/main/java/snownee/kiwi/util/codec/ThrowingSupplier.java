package snownee.kiwi.util.codec;

@FunctionalInterface
public interface ThrowingSupplier<T> {
	T get() throws Exception;
}