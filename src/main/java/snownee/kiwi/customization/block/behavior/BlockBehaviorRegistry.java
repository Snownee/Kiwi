package snownee.kiwi.customization.block.behavior;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

public final class BlockBehaviorRegistry {
	private static final BlockBehaviorRegistry INSTANCE = new BlockBehaviorRegistry();

	public static BlockBehaviorRegistry getInstance() {
		return INSTANCE;
	}

	private Block context;
	private final Map<Block, UseHandler> useHandlers = Maps.newIdentityHashMap();

	private BlockBehaviorRegistry() {
	}

	public void addUseHandler(UseHandler handler) {
		Objects.requireNonNull(context, "Context not set");
		this.useHandlers.put(context, handler);
	}

	public void setContext(Block block) {
		this.context = block;
	}

	public InteractionResult onUseBlock(Player entity, Level level, InteractionHand hand, BlockHitResult hitVec) {
		var blockState = level.getBlockState(hitVec.getBlockPos());
		if (this.useHandlers.containsKey(blockState.getBlock())) {
			return this.useHandlers.get(blockState.getBlock()).use(blockState, entity, level, hand, hitVec);
		}
		return InteractionResult.PASS;
	}
}
