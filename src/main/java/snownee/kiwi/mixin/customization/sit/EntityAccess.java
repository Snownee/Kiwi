package snownee.kiwi.mixin.customization.sit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.Entity;

@Mixin(Entity.class)
public interface EntityAccess {
	@Invoker
	boolean callCanRide(Entity entity);
}
