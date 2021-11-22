package snownee.kiwi.contributor.network;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import snownee.kiwi.contributor.Contributors;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PacketHandler;
import snownee.kiwi.util.Util;

@KiwiPacket("set_cosmetic")
public class CSetCosmeticPacket extends PacketHandler.Impl {

	public static CSetCosmeticPacket I;

	@Override
	public CompletableFuture<@Nullable FriendlyByteBuf> receive(Function<Runnable, CompletableFuture<@Nullable FriendlyByteBuf>> executor, FriendlyByteBuf buf, ServerPlayer sender) {
		ResourceLocation id = Util.RL(buf.readUtf(32767));
		Contributors.changeCosmetic(sender, id);
		return CompletableFuture.completedFuture(null);
	}

	public static void send(ResourceLocation cosmetic) {
		String id = cosmetic == null ? "" : cosmetic.toString();
		I.sendToServer($ -> $.writeUtf(id));
	}

}
