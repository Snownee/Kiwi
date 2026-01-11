package snownee.kiwi.customization.builder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.joml.Matrix4f;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.kiwi.util.KHolder;
import snownee.kiwi.util.NotNullByDefault;

@NotNullByDefault
public class BuilderModePreview implements DebugRenderer.SimpleDebugRenderer {
	public KHolder<BuilderRule> rule;
	public BlockPos pos;
	private BlockState blockState = Blocks.AIR.defaultBlockState();
	public List<BlockPos> positions = List.of();
	private final ListMultimap<Direction, AABB> faces = ArrayListMultimap.create(6, 32);
	private long lastUpdateTime;

	@Override
	public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, double pCamX, double pCamY, double pCamZ) {
		Minecraft mc = Minecraft.getInstance();
		if (!BuildersButton.isBuilderModeOn() || !(mc.hitResult instanceof BlockHitResult hitResult) ||
				mc.hitResult.getType() == HitResult.Type.MISS) {
			positions = List.of();
			return;
		}
		long millis = Util.getMillis();
		Player player = Objects.requireNonNull(mc.player);
		BlockState blockState = player.level().getBlockState(hitResult.getBlockPos());
		if (millis - this.lastUpdateTime > 200 || !hitResult.getBlockPos().equals(pos) || this.blockState != blockState) {
			this.lastUpdateTime = millis;
			this.blockState = blockState;
			updatePositions(player, hitResult);
		}
		if (positions.isEmpty()) {
			return;
		}

		VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.debugQuads());
		Matrix4f pose = pPoseStack.last().pose();
		float r = 1.0F;
		float g = 1.0F;
		float b = 1.0F;
		float a = 0.08F + Mth.sin(millis / 350F) * 0.05F;
		for (Map.Entry<Direction, Collection<AABB>> entry : faces.asMap().entrySet()) {
			Direction direction = entry.getKey();
			for (AABB aabb : entry.getValue()) {
				aabb = aabb.move(-pCamX, -pCamY, -pCamZ);
				drawFace(pose, vertexconsumer, aabb, direction, r, g, b, a);
			}
		}
	}

	private void updatePositions(Player player, BlockHitResult hitResult) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.isPaused()) {
			return;
		}
		InteractionHand hand = InteractionHand.MAIN_HAND;
		ItemStack itemStack = player.getItemInHand(hand);
		this.rule = null;
		this.pos = null;
		positions = List.of();
		for (KHolder<BuilderRule> holder : BuilderRules.find(blockState.getBlock())) {
			if (holder.value().matches(player, itemStack, blockState)) {
				positions = holder.value().searchPositions(new UseOnContext(player, hand, hitResult));
				if (positions.isEmpty()) {
					continue;
				}
				this.rule = holder;
				this.pos = hitResult.getBlockPos();
				faces.clear();
				VoxelShape fullShape = positions.stream().map(BuilderModePreview::getShape).reduce(
						Shapes.empty(),
						(a, b) -> Shapes.joinUnoptimized(a, b, BooleanOp.OR));
				fullShape = fullShape.optimize();
				List<AABB> aabbs = fullShape.toAabbs();
				for (Direction direction : snownee.kiwi.util.Util.DIRECTIONS) {
					for (AABB aabb : aabbs) {
						VoxelShape faceShape = getFaceShape(aabb, direction);
						faceShape = Shapes.join(faceShape, fullShape, BooleanOp.ONLY_FIRST);
						if (!faceShape.isEmpty()) {
							for (AABB faceShapeAabb : faceShape.toAabbs()) {
								faces.put(direction, faceShapeAabb);
							}
						}
					}
				}
				break;
			}
		}
	}

	private void drawFace(Matrix4f pose, VertexConsumer consumer, AABB aabb, Direction face, float r, float g, float b, float a) {
		float minX = (float) aabb.minX;
		float minY = (float) aabb.minY;
		float minZ = (float) aabb.minZ;
		float maxX = (float) aabb.maxX;
		float maxY = (float) aabb.maxY;
		float maxZ = (float) aabb.maxZ;
		switch (face) {
			case DOWN -> {
				consumer.vertex(pose, minX, minY, minZ).color(r, g, b, a).endVertex();
				consumer.vertex(pose, minX, minY, maxZ).color(r, g, b, a).endVertex();
				consumer.vertex(pose, maxX, minY, maxZ).color(r, g, b, a).endVertex();
				consumer.vertex(pose, maxX, minY, minZ).color(r, g, b, a).endVertex();
			}
			case UP -> {
				consumer.vertex(pose, minX, maxY, minZ).color(r, g, b, a).endVertex();
				consumer.vertex(pose, maxX, maxY, minZ).color(r, g, b, a).endVertex();
				consumer.vertex(pose, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
				consumer.vertex(pose, minX, maxY, maxZ).color(r, g, b, a).endVertex();
			}
			case NORTH -> {
				consumer.vertex(pose, minX, minY, minZ).color(r, g, b, a).endVertex();
				consumer.vertex(pose, maxX, minY, minZ).color(r, g, b, a).endVertex();
				consumer.vertex(pose, maxX, maxY, minZ).color(r, g, b, a).endVertex();
				consumer.vertex(pose, minX, maxY, minZ).color(r, g, b, a).endVertex();
			}
			case SOUTH -> {
				consumer.vertex(pose, minX, minY, maxZ).color(r, g, b, a).endVertex();
				consumer.vertex(pose, minX, maxY, maxZ).color(r, g, b, a).endVertex();
				consumer.vertex(pose, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
				consumer.vertex(pose, maxX, minY, maxZ).color(r, g, b, a).endVertex();
			}
			case WEST -> {
				consumer.vertex(pose, minX, minY, minZ).color(r, g, b, a).endVertex();
				consumer.vertex(pose, minX, minY, maxZ).color(r, g, b, a).endVertex();
				consumer.vertex(pose, minX, maxY, maxZ).color(r, g, b, a).endVertex();
				consumer.vertex(pose, minX, maxY, minZ).color(r, g, b, a).endVertex();
			}
			case EAST -> {
				consumer.vertex(pose, maxX, minY, minZ).color(r, g, b, a).endVertex();
				consumer.vertex(pose, maxX, maxY, minZ).color(r, g, b, a).endVertex();
				consumer.vertex(pose, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
				consumer.vertex(pose, maxX, minY, maxZ).color(r, g, b, a).endVertex();
			}
		}
	}

	private static VoxelShape getFaceShape(AABB aabb, Direction face) {
		aabb = switch (face) {
			case DOWN -> new AABB(aabb.minX, aabb.minY - 0.0001, aabb.minZ, aabb.maxX, aabb.minY, aabb.maxZ);
			case UP -> new AABB(aabb.minX, aabb.maxY, aabb.minZ, aabb.maxX, aabb.maxY + 0.0001, aabb.maxZ);
			case NORTH -> new AABB(aabb.minX, aabb.minY, aabb.minZ - 0.0001, aabb.maxX, aabb.maxY, aabb.minZ);
			case SOUTH -> new AABB(aabb.minX, aabb.minY, aabb.maxZ, aabb.maxX, aabb.maxY, aabb.maxZ + 0.0001);
			case WEST -> new AABB(aabb.minX - 0.0001, aabb.minY, aabb.minZ, aabb.minX, aabb.maxY, aabb.maxZ);
			case EAST -> new AABB(aabb.maxX, aabb.minY, aabb.minZ, aabb.maxX + 0.0001, aabb.maxY, aabb.maxZ);
		};
		return Shapes.create(aabb);
	}

	private static VoxelShape getShape(BlockPos pos) {
		ClientLevel level = Objects.requireNonNull(Minecraft.getInstance().level);
		BlockState blockState = level.getBlockState(pos);
		return blockState.getShape(level, pos).move(pos.getX(), pos.getY(), pos.getZ());
	}
}
