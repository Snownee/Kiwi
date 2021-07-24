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

import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.IArgumentSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;

/**
 * @since 2.7.0
 */
public class ForgeRegistryArgument<T extends IForgeRegistryEntry<T>> implements ArgumentType<T> {

	public static final DynamicCommandExceptionType BAD_ID = new DynamicCommandExceptionType(pair -> new TranslationTextComponent("argument.cuisine.registry.id.invalid", ((Pair) pair).getLeft(), ((Pair) pair).getRight()));

	private final IForgeRegistry<T> registry;
	private Collection<String> examples;

	public ForgeRegistryArgument(IForgeRegistry<T> registry) {
		this.registry = registry;
	}

	@Override
	public T parse(StringReader reader) throws CommandSyntaxException {
		int i = reader.getCursor();
		ResourceLocation resourcelocation = ResourceLocation.read(reader);
		if (registry.containsKey(resourcelocation)) {
			return registry.getValue(resourcelocation);
		}
		reader.setCursor(i);
		throw BAD_ID.createWithContext(reader, Pair.of(registry.getRegistryName().getPath(), resourcelocation));
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return ISuggestionProvider.suggestResource(registry.getKeys(), builder);
	}

	@Override
	public Collection<String> getExamples() {
		if (examples == null) {
			ImmutableList.Builder<String> builder = ImmutableList.builder();
			int count = 0;
			for (T value : registry.getValues()) {
				builder.add(value.getRegistryName().toString());
				if (++count == 3) {
					break;
				}
			}
			examples = builder.build();
		}
		return examples;
	}

	public static class Serializer implements IArgumentSerializer<ForgeRegistryArgument<? extends IForgeRegistryEntry>> {

		@Override
		public void serializeToNetwork(ForgeRegistryArgument<? extends IForgeRegistryEntry> argument, PacketBuffer buffer) {
			buffer.writeResourceLocation(argument.registry.getRegistryName());
		}

		@Override
		public ForgeRegistryArgument<? extends IForgeRegistryEntry> deserializeFromNetwork(PacketBuffer buffer) {
			return new ForgeRegistryArgument(RegistryManager.ACTIVE.getRegistry(buffer.readResourceLocation()));
		}

		@Override
		public void serializeToJson(ForgeRegistryArgument<? extends IForgeRegistryEntry> argument, JsonObject json) {
			json.addProperty("registry", argument.registry.getRegistryName().toString());
		}

	}

}
