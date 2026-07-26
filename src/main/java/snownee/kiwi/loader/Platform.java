package snownee.kiwi.loader;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.jspecify.annotations.Nullable;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import snownee.kiwi.util.VanillaActions;
import snownee.kiwi.util.resource.OneTimeLoader;

public class Platform {

	public enum ConditionDecision {
		ALLOW,
		SKIP
	}

	public static ICondition.IContext conditionContext(RegistryOps.RegistryInfoLookup provider, FeatureFlagSet enabledFeatures) {
		return new ICondition.IContext() {
			@Override
			public <T> boolean isTagLoaded(TagKey<T> key) {
				return provider.lookup(key.registry()).flatMap($ -> $.getter().get(key)).isPresent();
			}

			@Override
			public <T> Collection<Holder<T>> getTag(TagKey<T> key) {
				return provider.lookup(key.registry())
						.flatMap($ -> $.getter().get(key))
						.map($ -> $.stream().toList())
						.orElseGet(List::of);
			}

			@Override
			public FeatureFlagSet enabledFeatures() {
				return enabledFeatures;
			}
		};
	}

	public static <T> DataResult<ConditionDecision> applyResourceConditions(
			Identifier file,
			Dynamic<T> dynamic,
			OneTimeLoader.Context context) {
		Optional<Dynamic<T>> conditions = dynamic.get(ConditionalOps.DEFAULT_CONDITIONS_KEY).result();
		if (conditions.isEmpty()) {
			return DataResult.success(ConditionDecision.ALLOW);
		}
		ConditionalOps<T> ops = new ConditionalOps<>(
				RegistryOps.create(dynamic.getOps(), context.registryLookup),
				context.conditionContext);
		return ICondition.LIST_CODEC.parse(ops, conditions.get().getValue())
				.map($ -> $.stream().allMatch(condition -> condition.test(context.conditionContext)) ?
						ConditionDecision.ALLOW :
						ConditionDecision.SKIP)
				.mapError($ -> "Failed to parse native conditions in " + file + ": " + $);
	}

	public static boolean isModLoaded(String id) {
		ModList modList = ModList.get();
		if (modList == null) {
			return FMLLoader.getCurrent().getLoadingModList().getModFileById(id) != null;
		}
		return modList.isLoaded(id);
	}

	public static String getModName(String id) {
		return ModList.get().getModContainerById(id).orElseThrow().getModInfo().getDisplayName();
	}

	public static String getModDescription(String id) {
		return ModList.get().getModContainerById(id).orElseThrow().getModInfo().getDescription();
	}

	public static Optional<Path> findResource(String id, String path) {
		return ModList.get()
				.getModContainerById(id)
				.flatMap($ -> $.getModInfo().getOwningFile().getFile().getContents().findFile(path))
				.map(it -> Path.of(it));
	}

	public static boolean isPhysicalClient() {
		return FMLEnvironment.getDist().isClient();
	}

	@Nullable
	public static MinecraftServer getServer() {
		return ServerLifecycleHooks.getCurrentServer();
	}

	public static boolean isProduction() {
		return FMLEnvironment.isProduction();
	}

	public static boolean isDataGen() {
		return DatagenModLoader.isRunningDataGen();
	}

	public static int[] getVersionNumber(String id) {
		ModContainer container = ModList.get().getModContainerById(id).orElseThrow();
		ArtifactVersion version = container.getModInfo().getVersion();
		return new int[]{version.getMajorVersion(), version.getMinorVersion(), version.getIncrementalVersion()};
	}

	public static Path getGameDir() {
		return FMLPaths.GAMEDIR.get();
	}

	public static Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}

	public static @Nullable ItemStackTemplate getCraftingRemainingItem(ItemStack stack) {
		return stack.getCraftingRemainder();
	}

	public static boolean isFakePlayer(Player player) {
		return player instanceof FakePlayer;
	}

	public static Packet<ClientGamePacketListener> defaultAddEntityPacket(Entity entity, ServerEntity serverEntity) {
		return new ClientboundAddEntityPacket(entity, serverEntity);
	}

	public static boolean isCurativeItem(MobEffectInstance effectInstance, ItemStack stack) {
		return stack.is(Tags.Items.BUCKETS_MILK);
	}

	public static boolean isShearsLeftClickable(ItemStack stack) {
		return stack.canPerformAction(ItemAbilities.SHEARS_DIG);
	}

	public static boolean isShearsRightClickable(ItemStack stack) {
		return stack.canPerformAction(ItemAbilities.SHEARS_HARVEST) || stack.canPerformAction(ItemAbilities.SHEARS_DISARM);
	}

	public static Fluid getFluidFromBucket(BucketItem item) {
		return item.content;
	}

	public static MutableComponent format(String s, Object... objects) {
		try {
			return Component.literal(MessageFormat.format(I18n.get(s), objects));
		} catch (Exception e) {
			return Component.translatable(s, objects);
		}
	}

	public static String getTagTranslationKey(TagKey<?> tagKey) {
		return Tags.getTagTranslationKey(tagKey);
	}

	public static Platform.Type getPlatform() {
		return Type.NeoForge;
	}

	public static Platform.Type getPlatformSeries() {
		return Type.NeoForge;
	}

	public static void setFireInfo(Block blockIn, int spread, int burn) {
		VanillaActions.setFireInfo(blockIn, spread, burn);
	}

	public static void registerHoeConversion(Block k, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> v) {
		VanillaActions.registerHoeConversion(k, v);
	}

	public static void registerAxeConversion(Block k, Block v) {
		VanillaActions.registerAxeConversion(k, v);
	}

	public static void registerShovelConversion(Block k, BlockState v) {
		VanillaActions.registerShovelConversion(k, v);
	}

	public static void registerCompostable(float chance, ItemLike itemIn) {
		VanillaActions.registerCompostable(chance, itemIn);
	}

	public static void registerVillagerCompostable(ItemLike item) {
		VanillaActions.registerVillagerCompostable(item);
	}

	public static void registerVillagerFood(ItemLike item, int value) {
		VanillaActions.registerVillagerFood(item, value);
	}

	public static <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> getRecipes(
			RecipeManager recipeManager,
			RecipeType<T> recipeType) {
		return recipeManager.recipeMap().byType(recipeType);
	}

	public enum Type {
		Vanilla, Fabric, Quilt, Forge, NeoForge, Unknown
	}
}
