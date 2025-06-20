package snownee.kiwi.loader;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.WorkAtComposter;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class Platform {

	private Platform() {
	}

	public static boolean isModLoaded(String id) {
		ModList modList = ModList.get();
		if (modList == null) {
			return LoadingModList.get().getModFileById(id) != null;
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
		return Optional.of(ModList.get().getModContainerById(id).orElseThrow().getModInfo().getOwningFile().getFile().findResource(path));
	}

	public static boolean isPhysicalClient() {
		return FMLEnvironment.dist.isClient();
	}

	@Nullable
	public static MinecraftServer getServer() {
		return ServerLifecycleHooks.getCurrentServer();
	}

	public static boolean isProduction() {
		return FMLEnvironment.production;
	}

	public static boolean isDataGen() {
		return DatagenModLoader.isRunningDataGen();
	}

	public static Path getGameDir() {
		return FMLPaths.GAMEDIR.get();
	}

	public static Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}

	public static ItemStack getCraftingRemainingItem(ItemStack stack) {
		return stack.getCraftingRemainder();
	}

	public static boolean isFakePlayer(Player player) {
		return player instanceof FakePlayer;
	}

	public static Packet<ClientGamePacketListener> defaultAddEntityPacket(Entity entity, ServerEntity serverEntity) {
		return new ClientboundAddEntityPacket(entity, serverEntity);
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

	public static int[] getVersionNumber(String id) {
		ModContainer container = ModList.get().getModContainerById(id).orElseThrow();
		ArtifactVersion version = container.getModInfo().getVersion();
		return new int[]{version.getMajorVersion(), version.getMinorVersion(), version.getIncrementalVersion()};
	}

	public static Platform.Type getPlatform() {
		return Type.NeoForge;
	}

	public static Platform.Type getPlatformSeries() {
		return Type.NeoForge;
	}

	public static void setFireInfo(Block blockIn, int spread, int burn) {
		((FireBlock) Blocks.FIRE).setFlammable(blockIn, spread, burn);
	}

	public static void registerHoeConversion(Block k, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> v) {
		//noinspection deprecation
		HoeItem.TILLABLES.put(k, v);
	}

	public static void registerAxeConversion(Block k, Block v) {
		if (AxeItem.STRIPPABLES instanceof ImmutableMap) {
			AxeItem.STRIPPABLES = Maps.newHashMap(AxeItem.STRIPPABLES);
		}
		AxeItem.STRIPPABLES.put(k, v);
	}

	public static void registerShovelConversion(Block k, BlockState v) {
		if (ShovelItem.FLATTENABLES instanceof ImmutableMap) {
			ShovelItem.FLATTENABLES = Maps.newHashMap(ShovelItem.FLATTENABLES);
		}
		ShovelItem.FLATTENABLES.put(k, v);
	}

	public static void registerCompostable(float chance, ItemLike itemIn) {
		// do nothing. use DataMapProvider
	}

	public static void registerVillagerCompostable(ItemLike item) {
		if (WorkAtComposter.COMPOSTABLE_ITEMS instanceof ImmutableList) {
			WorkAtComposter.COMPOSTABLE_ITEMS = Lists.newArrayList(WorkAtComposter.COMPOSTABLE_ITEMS);
		}
		WorkAtComposter.COMPOSTABLE_ITEMS.add(item.asItem());
	}

	public static void registerVillagerFood(ItemLike item, int value) {
		if (Villager.FOOD_POINTS instanceof ImmutableMap) {
			Villager.FOOD_POINTS = Maps.newHashMap(Villager.FOOD_POINTS);
		}
		Villager.FOOD_POINTS.put(item.asItem(), value);
	}

	public enum Type {
		Vanilla, Fabric, Quilt, Forge, NeoForge, Unknown
	}
}
