package snownee.kiwi.handler;

import java.util.function.Supplier;

import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * @since 2.7.0
 */
public class ExtractOnlyItemHandler<T extends ResourceHandler<ItemResource>> extends DelegatingResourceHandler<ItemResource> implements Supplier<T> {

	private final T handler;

	public ExtractOnlyItemHandler(T handler) {
		super(handler);
		this.handler = handler;
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
		return amount;
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return false;
	}

	@Override
	public T get() {
		return handler;
	}

}
