package snownee.kiwi.mixin.customization.family;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import snownee.kiwi.customization.block.family.BlockFamilies;
import snownee.kiwi.customization.block.family.StonecutterRecipeMaker;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
	@Shadow
	private RecipeMap recipes;

	@WrapOperation(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/crafting/RecipeMap;create(Lnet/minecraft/core/HolderLookup;)Lnet/minecraft/world/item/crafting/RecipeMap;"))
	private RecipeMap kiwi$addStonecutterRecipes(HolderLookup<Recipe<?>> lookup, Operation<RecipeMap> original) {
		List<RecipeHolder<StonecutterRecipe>> extraRecipes = StonecutterRecipeMaker.makeRecipes();
		if (extraRecipes.isEmpty()) {
			return original.call(lookup);
		}
		return original.call(new KiwiRecipeLookup(lookup, extraRecipes));
	}

	@Inject(method = "finalizeRecipeLoading", at = @At("HEAD"))
	private void kiwi$finalizeRecipeLoading(FeatureFlagSet enabledFlags, CallbackInfo ci) {
		BlockFamilies.reloadRecipes(recipes.byType(RecipeType.STONECUTTING));
	}

	private static final class KiwiRecipeLookup implements HolderLookup<Recipe<?>> {
		private final HolderLookup<Recipe<?>> delegate;
		private final List<RecipeHolder<StonecutterRecipe>> extraRecipes;

		private KiwiRecipeLookup(HolderLookup<Recipe<?>> delegate, List<RecipeHolder<StonecutterRecipe>> extraRecipes) {
			this.delegate = delegate;
			this.extraRecipes = extraRecipes;
		}

		private List<Holder.Reference<Recipe<?>>> extraHolders() {
			return extraRecipes.stream()
					.map(holder -> (Holder.Reference<Recipe<?>>) new RecipeReference(this, holder.id(), holder.value()))
					.toList();
		}

		@Override
		public Stream<Holder.Reference<Recipe<?>>> listElements() {
			return Stream.concat(delegate.listElements(), extraHolders().stream());
		}

		@Override
		public Stream<HolderSet.Named<Recipe<?>>> listTags() {
			return delegate.listTags();
		}

		@Override
		public Optional<Holder.Reference<Recipe<?>>> get(ResourceKey<Recipe<?>> key) {
			return delegate.get(key).or(() -> extraHolders().stream().filter(holder -> holder.key().equals(key)).findFirst());
		}

		@Override
		public Optional<HolderSet.Named<Recipe<?>>> get(TagKey<Recipe<?>> tag) {
			return delegate.get(tag);
		}
	}

	private static final class RecipeReference extends Holder.Reference<Recipe<?>> {
		private RecipeReference(HolderOwner<Recipe<?>> owner, ResourceKey<Recipe<?>> key, Recipe<?> value) {
			super(Holder.Reference.Type.STAND_ALONE, owner, key, value);
		}
	}
}
