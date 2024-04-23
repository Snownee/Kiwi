package snownee.kiwi.customization.network;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;
import snownee.kiwi.customization.duck.KPlayer;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PacketHandler;

@KiwiPacket(value = "sync_place_count", dir = KiwiPacket.Direction.PLAY_TO_CLIENT)
public class SSyncPlaceCountPacket extends PacketHandler {
	public static SSyncPlaceCountPacket I;

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(
			Function<Runnable, CompletableFuture<FriendlyByteBuf>> executor,
			FriendlyByteBuf friendlyByteBuf,
			@Nullable ServerPlayer serverPlayer) {
		int count = friendlyByteBuf.readVarInt();
		return executor.apply(() -> {
			(Objects.requireNonNull((KPlayer) Minecraft.getInstance().player)).kiwi$setPlaceCount(count);
		});
	}

	public static void sync(ServerPlayer player) {
		I.send(player, buf -> buf.writeVarInt(((KPlayer) player).kiwi$getPlaceCount()));
	}
}
