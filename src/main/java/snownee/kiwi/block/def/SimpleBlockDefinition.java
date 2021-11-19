package snownee.kiwi.block.def;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class SimpleBlockDefinition implements BlockDefinition {

	private static final MethodHandle GET_STATE_FOR_PLACEMENT;

	static {
		MethodHandle m = null;
		try {
			m = MethodHandles.lookup().unreflect(ObfuscationReflectionHelper.findMethod(BlockItem.class, "m_5965_", BlockPlaceContext.class));
		} catch (Exception e) {
			throw new RuntimeException("Report this to author", e);
		}
		GET_STATE_FOR_PLACEMENT = m;
	}

	@Nullable
	private static BlockState getStateForPlacement(BlockItem blockItem, BlockPlaceContext context) {
		try {
			return (BlockState) GET_STATE_FOR_PLACEMENT.invokeExact(blockItem, context);
		} catch (Throwable e) {
			return null;
		}
	}

	public enum Factory implements BlockDefinition.Factory<SimpleBlockDefinition> {
		INSTANCE;

		@Override
		public SimpleBlockDefinition fromNBT(CompoundTag tag) {
			BlockState state = NbtUtils.readBlockState(tag.getCompound(TYPE));
			if (state.isAir())
				return null;
			return of(state);
		}

		@Override
		public SimpleBlockDefinition fromBlock(BlockState state, BlockEntity blockEntity, LevelReader level, BlockPos pos) {
			return of(state);
		}

		@Override
		public SimpleBlockDefinition fromItem(ItemStack stack, BlockPlaceContext context) {
			if (!(stack.getItem() instanceof BlockItem)) {
				return null;
			}
			BlockItem blockItem = (BlockItem) stack.getItem();
			if (context == null) {
				return of(blockItem.getBlock().defaultBlockState());
			}
			context = blockItem.updatePlacementContext(context);
			if (context == null) {
				return null;
			}
			BlockState state = getStateForPlacement(blockItem, context);
			if (state == null) {
				return null;
			}
			return of(state);
		}

		@Override
		public String getId() {
			return TYPE;
		}

	}

	public static final String TYPE = "Block";
	private static final Map<BlockState, SimpleBlockDefinition> MAP = Maps.newIdentityHashMap();

	public static SimpleBlockDefinition of(BlockState state) {
		if (state.getBlock() == Blocks.GRASS_BLOCK) {
			state = state.setValue(BlockStateProperties.SNOWY, false);
		}
		return MAP.computeIfAbsent(state, SimpleBlockDefinition::new);
	}

	public final BlockState state;
	@OnlyIn(Dist.CLIENT)
	private Material[] materials;

	private SimpleBlockDefinition(BlockState state) {
		this.state = state;
		if (FMLEnvironment.dist.isClient()) {
			materials = new Material[7];
		}
	}

	@Override
	public BlockDefinition.Factory<?> getFactory() {
		return Factory.INSTANCE;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public BakedModel model() {
		return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(state);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Material renderMaterial(Direction direction) {
		int index = direction == null ? 0 : direction.ordinal() + 1;
		if (materials[index] == null) {
			BakedModel model = model();
			Random random = new Random();
			random.setSeed(42L);
			ResourceLocation particleIcon = model.getParticleIcon(EmptyModelData.INSTANCE).getName();
			ResourceLocation sprite = particleIcon;
			if (state.getBlock() == Blocks.GRASS_BLOCK) {
				direction = Direction.UP;
			}
			if (direction != null) {
				List<BakedQuad> quads = model.getQuads(state, direction, random, EmptyModelData.INSTANCE);
				if (quads.isEmpty())
					quads = model.getQuads(state, null, random, EmptyModelData.INSTANCE);
				for (BakedQuad quad : quads) {
					sprite = quad.getSprite().getName();
					if (sprite.equals(particleIcon)) {
						break;
					}
				}
			}
			materials[index] = ModelLoaderRegistry.blockMaterial(sprite);
		}
		return materials[index];
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canRenderInLayer(RenderType layer) {
		return ItemBlockRenderTypes.canRenderInLayer(state, layer);
	}

	@Override
	public boolean canOcclude() {
		return state.canOcclude();
	}

	@Override
	public void save(CompoundTag tag) {
		tag.put(TYPE, NbtUtils.writeBlockState(state));
	}

	@Override
	public String toString() {
		return state.toString();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getColor(BlockState blockState, BlockAndTintGetter level, BlockPos worldPosition, int index) {
		return Minecraft.getInstance().getBlockColors().getColor(state, level, worldPosition, index);
	}

	@Override
	public Component getDescription() {
		return state.getBlock().getName();
	}

	@Override
	public void place(Level level, BlockPos pos) {
		BlockState state = this.state;
		if (state.hasProperty(BlockStateProperties.LIT))
			state = state.setValue(BlockStateProperties.LIT, false);
		level.setBlockAndUpdate(pos, state);
	}

	@Override
	public ItemStack createItem(HitResult target, BlockGetter world, BlockPos pos, Player player) {
		return getBlockState().getPickBlock(target, world, pos, player);
	}

	@Override
	public BlockState getBlockState() {
		return state;
	}

	@Override
	public SoundType getSoundType() {
		return state.getSoundType();
	}

	public static void reload() {
		for (SimpleBlockDefinition supplier : MAP.values()) {
			Arrays.fill(supplier.materials, null);
		}
	}

}
