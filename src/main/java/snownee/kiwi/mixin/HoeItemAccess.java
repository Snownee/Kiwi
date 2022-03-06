package snownee.kiwi.mixin;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.datafixers.util.Pair;

import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

@Mixin(HoeItem.class)
public interface HoeItemAccess {

	@Accessor
	static Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> getTILLABLES() {
		throw new IllegalStateException();
	}

}
