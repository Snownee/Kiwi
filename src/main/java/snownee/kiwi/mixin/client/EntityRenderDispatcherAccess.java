package snownee.kiwi.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.player.PlayerModelType;

@Mixin(EntityRenderDispatcher.class)
public interface EntityRenderDispatcherAccess {
	@Accessor
	Map<PlayerModelType, AvatarRenderer<AbstractClientPlayer>> getPlayerRenderers();
}
