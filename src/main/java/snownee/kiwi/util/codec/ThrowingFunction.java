package snownee.kiwi.util.codec;

@FunctionalInterface
public interface ThrowingFunction<T, R> {
	R apply(T t) throws Exception;
}