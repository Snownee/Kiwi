package snownee.kiwi.loader;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
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
import net.minecraft.world.level.material.Fluid;
import snownee.kiwi.Kiwi;

public final class Platform {

	private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+).*?$");
	private static final boolean DATA_GEN = System.getProperty("fabric-api.datagen") != null;

	private Platform() {
	}

	public static boolean isModLoaded(String id) {
		return FabricLoader.getInstance().isModLoaded(id);
	}

	public static boolean isPhysicalClient() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
	}

	public static MinecraftServer getServer() {
		return Kiwi.currentServer;
	}

	public static boolean isProduction() {
		return !FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	public static boolean isDataGen() {
		return DATA_GEN;
	}

	public static int getVersionNumber(String id) {
		ModContainer container = FabricLoader.getInstance().getModContainer(id).orElseThrow();
		String version = container.getMetadata().getVersion().getFriendlyString();
		Matcher matcher = VERSION_PATTERN.matcher(version);
		int result = 0;
		if (!matcher.matches()) {
			throw new RuntimeException("Invalid version string: " + version);
		}
		for (int i = 1; i <= 3; i++) {
			int group = Math.min(Integer.parseInt(matcher.group(i)), 99);
			result = result * 100 + group;
		}
		return result;
	}

	public static Path getGameDir() {
		return FabricLoader.getInstance().getGameDir();
	}

	public static Path getConfigDir() {
		return FabricLoader.getInstance().getConfigDir();
	}

	public static ItemStack getCraftingRemainingItem(ItemStack stack) {
		return stack.getRecipeRemainder();
	}

	public static boolean isFakePlayer(Player player) {
		return player instanceof FakePlayer;
	}

	public static Packet<ClientGamePacketListener> defaultAddEntityPacket(Entity entity, ServerEntity serverEntity) {
		return new ClientboundAddEntityPacket(entity, serverEntity);
	}

	public static boolean isCurativeItem(MobEffectInstance effectInstance, ItemStack stack) {
		return stack.is(ConventionalItemTags.MILK_BUCKETS);
	}

	public static boolean isShearsLeftClickable(ItemStack stack) {
		return stack.is(ConventionalItemTags.SHEARS_TOOLS);
	}

	public static boolean isShearsRightClickable(ItemStack stack) {
		return stack.is(ConventionalItemTags.SHEARS_TOOLS);
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
		return tagKey.getTranslationKey();
	}

	public static Platform.Type getPlatform() {
		return Type.Fabric;
	}

	public static Platform.Type getPlatformSeries() {
		return Type.Fabric;
	}

	public enum Type {
		Vanilla, Fabric, Quilt, Forge, NeoForge
	}

}
