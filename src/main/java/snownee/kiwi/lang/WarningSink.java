package snownee.kiwi.lang;

@FunctionalInterface
public interface WarningSink {
	void warn(String key, String message);
}
