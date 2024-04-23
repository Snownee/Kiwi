package snownee.kiwi.customization.block.behavior;

import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.kiwi.customization.CustomFeatureTags;
import snownee.kiwi.customization.block.KBlockSettings;
import snownee.kiwi.customization.block.KBlockUtils;
import snownee.kiwi.customization.block.component.KBlockComponent;

public class SitManager {
	public static final Component ENTITY_NAME = Component.literal("Seat from Kiwi");
	public static final double VERTICAL_OFFSET = 0.15;

	public static void tick(Display.BlockDisplay display) {
		if (display.tickCount < 7) {
			return;
		}
		if (!display.isVehicle()) {
			display.discard();
		}
		BlockPos pos = BlockPos.containing(display.getX(), display.getY() + VERTICAL_OFFSET, display.getZ());
		BlockState blockState = display.level().getBlockState(pos);
		if (!blockState.is(display.getBlockState().getBlock())) {
			display.discard();
		}
	}

	public static boolean sit(Player player, BlockHitResult hitResult) {
		if (hitResult.getDirection() == Direction.DOWN || player.isSecondaryUseActive()) {
			return false;
		}
		if (player.getEyePosition().distanceToSqr(hitResult.getLocation()) > 12) {
			return false;
		}
		Level level = player.level();
		BlockPos pos = hitResult.getBlockPos();
		BlockState blockState = level.getBlockState(pos);
		if (!blockState.is(CustomFeatureTags.SITTABLE)) {
			return false;
		}
		if (!level.getEntities(EntityType.BLOCK_DISPLAY, new AABB(pos, pos.above()), SitManager::isSeatEntity).isEmpty()) {
			return false;
		}
		if (!level.isClientSide) {
			Display.BlockDisplay display = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, level);
			display.setCustomName(ENTITY_NAME);
//			display.setInvisible(true);
			display.setBlockState(blockState);
			VoxelShape shape = blockState.getShape(level, pos, CollisionContext.of(player));
			Vec3 seatPos = null;
			Direction facing = guessBlockFacing(blockState, player);
			if (!shape.isEmpty()) {
				AABB bounds = shape.bounds();
				double x = (bounds.minX + bounds.maxX) / 2;
				double y = bounds.maxX + 0.1;
				double z = (bounds.minZ + bounds.maxZ) / 2;
				if (facing != null) {
					x += facing.getStepX() * 0.1;
					z += facing.getStepZ() * 0.1;
				}
				Vec3 traceStart = new Vec3(x, y, z);
				Vec3 traceEnd = traceStart.add(0, -2, 0);
				BlockHitResult hit = shape.clip(traceStart, traceEnd, pos);
				if (hit != null && hit.getType() == BlockHitResult.Type.BLOCK) {
					seatPos = hit.getLocation();
				}
			}
			if (facing != null) {
				float yRot = facing.toYRot();
				display.setYRot(yRot);
				display.setNoGravity(true); //hacky way to tell the client that this block has facing
			}
			if (seatPos == null) {
				seatPos = Vec3.atCenterOf(pos);
			}
			display.setPos(seatPos.x, seatPos.y - VERTICAL_OFFSET, seatPos.z);
			if (level.addFreshEntity(display)) {
				player.startRiding(display, true);
			}
		}
		return true;
	}

	@Nullable
	public static Direction guessBlockFacing(BlockState blockState, @Nullable Player player) {
		KBlockSettings settings = KBlockSettings.of(blockState.getBlock());
		if (settings != null) {
			for (KBlockComponent component : settings.components.values()) {
				Direction facing = component.getHorizontalFacing(blockState);
				if (facing != null) {
					return facing;
				}
			}
		}
		boolean oppose = false;
		try {
			String shape = KBlockUtils.getValueString(blockState, "shape");
			if (!"straight".equals(shape)) {
				return null;
			}
			oppose = blockState.getBlock() instanceof StairBlock;
		} catch (Throwable ignored) {
		}
		if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
			Direction direction = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
			return oppose ? direction.getOpposite() : direction;
		}
		if (player != null && blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
			Direction.Axis axis = blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
			Direction direction = player.getDirection();
			if (axis.test(direction)) {
				return direction;
			}
		}
		return null;
	}

	public static boolean isSeatEntity(@Nullable Entity entity) {
		if (entity == null || entity.getType() != EntityType.BLOCK_DISPLAY) {
			return false;
		}
		Component customName = entity.getCustomName();
		return customName != null && Objects.equals(customName.getString(), SitManager.ENTITY_NAME.getString());
	}

	//modified from Boat
	public static void clampRotation(Player player, Entity seat) {
		if (!seat.isNoGravity()) {
			return;
		}
		float seatRot = Mth.wrapDegrees(seat.getYRot());
		float f = Mth.wrapDegrees(player.getYRot() - seatRot);
		float f1 = Mth.clamp(f, -105.0F, 105.0F);
		player.yRotO += f1 - f;
		player.setYRot(player.getYRot() + f1 - f);
		player.setYHeadRot(player.getYRot());
		player.setYBodyRot(seatRot);
		f = Mth.wrapDegrees(player.getXRot());
		f1 = Math.max(f, -45F);
		player.setXRot(f1);
	}

	public static Vec3 dismount(Entity display, LivingEntity passenger) {
		Direction direction;
		if (display.isNoGravity()) {
			direction = display.getDirection();
		} else {
			direction = passenger.getDirection();
		}
		BlockPos pos = BlockPos.containing(display.getX(), display.getY() + VERTICAL_OFFSET, display.getZ());
		Optional<Vec3> vec3 = BedBlock.findStandUpPosition(
				passenger.getType(),
				passenger.level(),
				pos,
				direction,
				passenger.getYRot());
		return vec3.isPresent() ? vec3.get() : Vec3.atBottomCenterOf(pos.above());
	}
}
