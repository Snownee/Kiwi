package snownee.kiwi.schedule;

import javax.annotation.Nullable;

public abstract class Task<T extends ITicker> {
    public Task() {}

    abstract public boolean tick(T ticker);

    @Nullable
    abstract public T ticker();

    public boolean shouldSave() {
        return true;
    }
}
