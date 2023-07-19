package snownee.kiwi.network;

import java.util.function.Consumer;
import java.util.stream.Stream;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public interface KPacketTarget {
	static KPacketTarget tracking(Entity entity) {
		return () -> PlayerLookup.tracking(entity).stream();
	}

	static KPacketTarget tracking(BlockEntity blockEntity) {
		return () -> PlayerLookup.tracking(blockEntity).stream();
	}

	static KPacketTarget tracking(ServerLevel level, ChunkPos chunkPos) {
		return () -> PlayerLookup.tracking(level, chunkPos).stream();
	}

	static KPacketTarget tracking(ServerLevel level, BlockPos blockPos) {
		return () -> PlayerLookup.tracking(level, blockPos).stream();
	}

	static KPacketTarget world(ServerLevel level) {
		return () -> PlayerLookup.world(level).stream();
	}

	static KPacketTarget all(MinecraftServer server) {
		return () -> PlayerLookup.all(server).stream();
	}

	static KPacketTarget allExcept(ServerPlayer player) {
		return () -> PlayerLookup.all(player.server).stream().filter(p -> p != player);
	}

	static KPacketTarget around(ServerLevel world, Vec3 pos, double radius) {
		return () -> PlayerLookup.around(world, pos, radius).stream();
	}

	static KPacketTarget around(ServerLevel world, Vec3i pos, double radius) {
		return () -> PlayerLookup.around(world, pos, radius).stream();
	}

	default void send(PacketHandler handler, Consumer<FriendlyByteBuf> buf) {
		handler.send(getPlayers(), buf);
	}

	Stream<ServerPlayer> getPlayers();
}
