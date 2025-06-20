package snownee.kiwi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

@Mixin(RenderLayer.class)
public interface RenderLayerAccess<S extends EntityRenderState, M extends EntityModel<? super S>> {
	@Accessor
	RenderLayerParent<S, M> getRenderer();
}
