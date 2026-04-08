package snownee.kiwi.loader;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Optional;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.jspecify.annotations.Nullable;

import net.minecraft.SharedConstants;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class Platform {

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
		return ModList.get().getModContainerById(id).flatMap($ -> Optional.ofNullable(Platform.class.getClassLoader().getResource(path))).map(url -> Path.of(url.getPath()));
	}

	public static boolean isPhysicalClient() {
		return FMLEnvironment.getDist().isClient();
	}

	@Nullable
	public static MinecraftServer getServer() {
		return ServerLifecycleHooks.getCurrentServer();
	}

	public static boolean isProduction() {
		return !SharedConstants.IS_RUNNING_IN_IDE;
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

	public enum Type {
		Vanilla, Fabric, Quilt, Forge, NeoForge, Unknown
	}
}
