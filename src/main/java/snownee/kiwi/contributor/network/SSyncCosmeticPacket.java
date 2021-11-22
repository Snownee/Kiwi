package snownee.kiwi.contributor.network;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import snownee.kiwi.contributor.Contributors;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.Networking.Direction;
import snownee.kiwi.network.PacketHandler;
import snownee.kiwi.util.Util;

@KiwiPacket(value = "sync_cosmetic", dir = Direction.PLAY_TO_CLIENT)
public class SSyncCosmeticPacket extends PacketHandler.Impl {

	public static SSyncCosmeticPacket I;

	@Override
	public CompletableFuture<@Nullable FriendlyByteBuf> receive(Function<Runnable, CompletableFuture<@Nullable FriendlyByteBuf>> executor, FriendlyByteBuf buf, ServerPlayer sender) {
		ImmutableMap.Builder<String, ResourceLocation> builder = ImmutableMap.builder();
		int size = buf.readVarInt();
		for (int i = 0; i < size; i++) {
			String k = buf.readUtf();
			String v = buf.readUtf();
			builder.put(k, Util.RL(v));
		}
		return executor.apply(() -> Contributors.changeCosmetic(builder.build()));
	}

	public static void send(Map<String, ResourceLocation> map, ServerPlayer player, boolean except) {
		Consumer<FriendlyByteBuf> consumer = buf -> {
			buf.writeVarInt(map.size());
			map.forEach((k, v) -> {
				buf.writeUtf(k);
				buf.writeResourceLocation(v);
			});
		};
		if (except) {
			I.sendAllExcept(player, consumer);
		} else {
			I.send(player, consumer);
		}
	}

}
