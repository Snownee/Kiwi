package snownee.kiwi.network;

import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

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
import net.neoforged.neoforge.network.PacketDistributor;

public final class KPacketSender {
	private KPacketSender() {
	}

	public static void sendToTracking(CustomPacketPayload payload, Entity entity) {
		PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
	}

	public static void sendToTracking(CustomPacketPayload payload, BlockEntity blockEntity) {
		sendToTracking(payload, (ServerLevel) blockEntity.getLevel(), blockEntity.getBlockPos());
	}

	public static void sendToTracking(CustomPacketPayload payload, ServerLevel level, ChunkPos chunkPos) {
		PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, payload);
	}

	public static void sendToTracking(CustomPacketPayload payload, ServerLevel level, BlockPos blockPos) {
		sendToTracking(payload, level, new ChunkPos(blockPos));
	}

	public static void sendToWorld(CustomPacketPayload payload, ServerLevel level) {
		PacketDistributor.sendToPlayersInDimension(level, payload);
	}

	public static void sendToAll(CustomPacketPayload payload, MinecraftServer server) {
		PacketDistributor.sendToAllPlayers(payload);
	}

	@Deprecated
	public static void sendToAllExcept(CustomPacketPayload payload, ServerPlayer player) {
		sendToAll(payload, player.server);
	}

	public static void sendToAround(
			CustomPacketPayload payload,
			ServerLevel world,
			@Nullable ServerPlayer excluded,
			Vec3 pos,
			double radius) {
		PacketDistributor.sendToPlayersNear(world, excluded, pos.x, pos.y, pos.z, radius, payload);
	}

	public static void sendToAround(
			CustomPacketPayload payload,
			ServerLevel world,
			@Nullable ServerPlayer excluded,
			Vec3i pos,
			double radius) {
		PacketDistributor.sendToPlayersNear(world, excluded, pos.getX(), pos.getY(), pos.getZ(), radius, payload);
	}

	public static void send(CustomPacketPayload payload, Player player) {
		PacketDistributor.sendToPlayer((ServerPlayer) player, payload);
	}

	public static void send(CustomPacketPayload payload, Stream<ServerPlayer> players) {
		ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(payload);
		players.forEach(p -> p.connection.send(packet));
	}

	public static void sendToServer(CustomPacketPayload payload) {
		PacketDistributor.sendToServer(payload);
	}
}
