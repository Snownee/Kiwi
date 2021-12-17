package snownee.kiwi.schedule;

import org.jetbrains.annotations.Nullable;

public abstract class Task<T extends ITicker> {
	public Task() {
	}

	abstract public boolean tick(T ticker);

	@Nullable
	abstract public T ticker();

	public boolean shouldSave() {
		return true;
	}
}
