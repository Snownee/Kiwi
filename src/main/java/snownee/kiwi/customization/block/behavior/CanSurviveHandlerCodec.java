package snownee.kiwi.customization.block.behavior;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class CanSurviveHandlerCodec implements Codec<CanSurviveHandler> {

	@Override
	public <T> DataResult<Pair<CanSurviveHandler, T>> decode(DynamicOps<T> ops, T input) {
		DataResult<String> stringValue = ops.getStringValue(input);
		if (stringValue.result().isPresent()) {
			String s = stringValue.result().get();
			CanSurviveHandler handler = switch (s) {
				case "check_floor" -> CanSurviveHandler.checkFloor();
				case "check_ceiling" -> CanSurviveHandler.checkCeiling();
				case "check_facing" -> CanSurviveHandler.checkFace(BlockStateProperties.FACING);
				case "check_horizontal_facing" -> CanSurviveHandler.checkFace(BlockStateProperties.HORIZONTAL_FACING);
				case "check_vertical_facing" -> CanSurviveHandler.checkFace(BlockStateProperties.VERTICAL_DIRECTION);
				case "check_facing_hopper" -> CanSurviveHandler.checkFace(BlockStateProperties.FACING_HOPPER);
				default -> null;
			};
			if (handler != null) {
				return DataResult.success(Pair.of(handler, ops.empty()));
			} else {
				return DataResult.error(() -> "Unknown CanSurviveHandler: " + s);
			}
		}
		//TODO
		return DataResult.error(() -> "Failed to decode CanSurviveHandler: " + input);
	}

	@Override
	public <T> DataResult<T> encode(CanSurviveHandler input, DynamicOps<T> ops, T prefix) {
		return DataResult.error(() -> "CanSurviveHandler is not serializable");
	}
}
