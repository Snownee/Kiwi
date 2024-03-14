package snownee.kiwi.network;

import java.util.Objects;
import java.util.stream.Stream;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public final class KPacketSender {
	private KPacketSender() {
	}

	public static void sendToTracking(CustomPacketPayload payload, Entity entity) {
		send(payload, PlayerLookup.tracking(entity).stream());
	}

	public static void sendToTracking(CustomPacketPayload payload, BlockEntity blockEntity) {
		send(payload, PlayerLookup.tracking(blockEntity).stream());
	}

	public static void sendToTracking(CustomPacketPayload payload, ServerLevel level, ChunkPos chunkPos) {
		send(payload, PlayerLookup.tracking(level, chunkPos).stream());
	}

	public static void sendToTracking(CustomPacketPayload payload, ServerLevel level, BlockPos blockPos) {
		send(payload, PlayerLookup.tracking(level, blockPos).stream());
	}

	public static void sendToWorld(CustomPacketPayload payload, ServerLevel level) {
		send(payload, PlayerLookup.world(level).stream());
	}

	public static void sendToAll(CustomPacketPayload payload, MinecraftServer server) {
		send(payload, PlayerLookup.all(server).stream());
	}

	public static void sendToAllExcept(CustomPacketPayload payload, ServerPlayer player) {
		send(payload, PlayerLookup.all(player.server).stream().filter(p -> p != player));
	}

	public static void sendToAround(CustomPacketPayload payload, ServerLevel world, Vec3 pos, double radius) {
		send(payload, PlayerLookup.around(world, pos, radius).stream());
	}

	public static void sendToAround(CustomPacketPayload payload, ServerLevel world, Vec3i pos, double radius) {
		send(payload, PlayerLookup.around(world, pos, radius).stream());
	}

	public static void send(CustomPacketPayload payload, Player player) {
		((ServerPlayer) player).connection.send(new ClientboundCustomPayloadPacket(payload));
	}

	public static void send(CustomPacketPayload payload, Stream<ServerPlayer> players) {
		ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(payload);
		players.forEach(p -> p.connection.send(packet));
	}

	public static void sendToServer(CustomPacketPayload payload) {
		Objects.requireNonNull(Minecraft.getInstance().getConnection()).send(new ClientboundCustomPayloadPacket(payload));
	}
}
