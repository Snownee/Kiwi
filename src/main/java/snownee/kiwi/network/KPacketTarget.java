package snownee.kiwi.network;

import java.util.Objects;
import java.util.function.Consumer;

import io.netty.buffer.Unpooled;
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
import net.minecraftforge.network.PacketDistributor;

public interface KPacketTarget {
	static KPacketTarget tracking(Entity entity) {
		return () -> PacketDistributor.TRACKING_ENTITY.with(() -> entity);
	}

	static KPacketTarget tracking(BlockEntity blockEntity) {
		return tracking((ServerLevel) Objects.requireNonNull(blockEntity.getLevel()), blockEntity.getBlockPos());
	}

	static KPacketTarget tracking(ServerLevel level, ChunkPos chunkPos) {
		return () -> PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunk(chunkPos.x, chunkPos.z));
	}

	static KPacketTarget tracking(ServerLevel level, BlockPos blockPos) {
		return tracking(level, new ChunkPos(blockPos));
	}

	static KPacketTarget world(ServerLevel level) {
		return () -> PacketDistributor.DIMENSION.with(level::dimension);
	}

	static KPacketTarget all(MinecraftServer server) {
		return PacketDistributor.ALL::noArg;
	}

	static KPacketTarget allExcept(ServerPlayer player) {
		return () -> Networking.ALL_EXCEPT.with(() -> player);
	}

	static KPacketTarget around(ServerLevel world, Vec3 pos, double radius) {
		return () -> PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(pos.x, pos.y, pos.z, radius, world.dimension()));
	}

	static KPacketTarget around(ServerLevel world, Vec3i pos, double radius) {
		return around(world, Vec3.atCenterOf(pos), radius);
	}

	default void send(PacketHandler handler, Consumer<FriendlyByteBuf> buf) {
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer()).writeResourceLocation(handler.id);
		buf.accept(buffer);
		Networking.send(internal(), buffer);
	}

	PacketDistributor.PacketTarget internal();
}
