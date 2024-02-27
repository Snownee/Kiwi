package snownee.kiwi.loader;

import java.util.List;
import java.util.Locale;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ClientPlatform {
	private ClientPlatform() {
	}

	public static BakedModel getModel(ResourceLocation id) {
		return Minecraft.getInstance().getModelManager().getModel(id);
	}

	public static void addExtraModels(List<? extends ResourceLocation> ids) {
		ModelLoadingPlugin.register(ctx -> ctx.addModels(ids));
	}

	public static <E extends Entity> void registerEntityRenderer(EntityType<? extends E> entityType, EntityRendererProvider<E> entityRendererFactory) {
		EntityRendererRegistry.register(entityType, entityRendererFactory);
	}

	public static <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<? extends T> blockEntityType, BlockEntityRendererProvider<T> blockEntityRendererProvider) {
		BlockEntityRenderers.register(blockEntityType, blockEntityRendererProvider);
	}

	public static <T extends ParticleOptions> void registerParticleType(ParticleType<T> type, ParticleProvider<T> factory) {
		ParticleFactoryRegistry.getInstance().register(type, factory);
	}

	public static void registerItemColor(ItemColor itemColor, ItemLike... items) {
		ColorProviderRegistry.ITEM.register(itemColor, items);
	}

	public static void registerBlockColor(BlockColor blockColor, Block... blocks) {
		ColorProviderRegistry.BLOCK.register(blockColor, blocks);
	}

	public static void setRenderType(Block block, RenderType renderType) {
		BlockRenderLayerMap.INSTANCE.putBlock(block, renderType);
	}

	public static Locale getLocale() {
		return Locale.getDefault();
	}
}
