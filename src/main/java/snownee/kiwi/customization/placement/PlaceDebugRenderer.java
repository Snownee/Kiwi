package snownee.kiwi.customization.placement;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.kiwi.util.VoxelUtil;

public class PlaceDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	private static final PlaceDebugRenderer INSTANCE = new PlaceDebugRenderer();
	private List<SlotRenderInstance> slots = List.of();

	public static PlaceDebugRenderer getInstance() {
		return INSTANCE;
	}

	private long lastUpdateTime;

	@Override
	public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, double pCamX, double pCamY, double pCamZ) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.isPaused()) {
			return;
		}
		long millis = Util.getMillis();
		if (millis - this.lastUpdateTime > 300) {
			this.lastUpdateTime = millis;
			Entity entity = mc.gameRenderer.getMainCamera().getEntity();
			Level level = entity.level();
			this.slots = BlockPos.betweenClosedStream(entity.getBoundingBox().inflate(4)).map(BlockPos::immutable).flatMap(pos -> {
				BlockState blockState = level.getBlockState(pos);
				return Direction.stream().flatMap(side -> PlaceSlot.find(blockState, side).stream().map(slot -> {
					return SlotRenderInstance.create(slot, pos, side);
				}));
			}).toList();
		}

		VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.lines());
		for (SlotRenderInstance instance : slots) {
			ShapeRenderer.renderShape(
					pPoseStack,
					vertexconsumer,
					instance.shape,
					instance.pos.getX() - pCamX,
					instance.pos.getY() - pCamY,
					instance.pos.getZ() - pCamZ,
					instance.color);
		}
	}

	private record SlotRenderInstance(PlaceSlot slot, BlockPos pos, Direction side, VoxelShape shape, int color) {
		private static final VoxelShape SHAPE_DOWN = Block.box(6, -0.5, 6, 10, 0.5, 10);
		private static final VoxelShape[] SHAPES = Direction.stream()
				.map(side -> VoxelUtil.rotate(SHAPE_DOWN, side))
				.toArray(VoxelShape[]::new);

		private static SlotRenderInstance create(PlaceSlot slot, BlockPos pos, Direction side) {
			String tag = slot.primaryTag();
			int color = 0xFFFFFFFF;
			if (tag.endsWith("side")) {
				color = 0xFFFFAAAA;
			} else if (tag.endsWith("front") || tag.endsWith("top")) {
				color = 0xFFAAFFAA;
			} else if (tag.endsWith("back") || tag.endsWith("bottom")) {
				color = 0xFFAAAAFF;
			}
			return new SlotRenderInstance(slot, pos, side, SHAPES[side.ordinal()], color);
		}
	}
}
