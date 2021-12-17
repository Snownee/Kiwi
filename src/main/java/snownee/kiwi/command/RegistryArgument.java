package snownee.kiwi.command;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * @since 5.2.0
 */
public class RegistryArgument<T> implements ArgumentType<T> {

	@SuppressWarnings("rawtypes")
	public static final DynamicCommandExceptionType BAD_ID = new DynamicCommandExceptionType(pair -> new TranslatableComponent("argument.kiwi.registry.id.invalid", ((Pair) pair).getLeft(), ((Pair) pair).getRight()));

	private final Registry<T> registry;
	private Collection<String> examples;

	public RegistryArgument(Registry<T> registry) {
		this.registry = registry;
	}

	@Override
	public T parse(StringReader reader) throws CommandSyntaxException {
		int i = reader.getCursor();
		ResourceLocation resourcelocation = ResourceLocation.read(reader);
		if (registry.containsKey(resourcelocation)) {
			return registry.get(resourcelocation);
		}
		reader.setCursor(i);
		throw BAD_ID.createWithContext(reader, Pair.of(registry.key().location().getPath(), resourcelocation));
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggestResource(registry.keySet(), builder);
	}

	@Override
	public Collection<String> getExamples() {
		if (examples == null) {
			ImmutableList.Builder<String> builder = ImmutableList.builder();
			int count = 0;
			for (ResourceLocation key : registry.keySet()) {
				builder.add(key.toString());
				if (++count == 3) {
					break;
				}
			}
			examples = builder.build();
		}
		return examples;
	}

	@SuppressWarnings("rawtypes")
	public static class Serializer implements ArgumentSerializer<RegistryArgument<?>> {

		//FIXME
		@Override
		public void serializeToNetwork(RegistryArgument<?> argument, FriendlyByteBuf buffer) {
			buffer.writeResourceLocation(argument.registry.key().location());
		}

		@Override
		public RegistryArgument<?> deserializeFromNetwork(FriendlyByteBuf buffer) {
			return new RegistryArgument(Registry.REGISTRY.get(buffer.readResourceLocation()));
		}

		@Override
		public void serializeToJson(RegistryArgument<?> argument, JsonObject json) {
			json.addProperty("registry", argument.registry.key().location().toString());
		}

	}

}
