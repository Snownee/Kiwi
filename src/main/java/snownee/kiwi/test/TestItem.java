package snownee.kiwi.test;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import snownee.kiwi.item.ModItem;
import snownee.kiwi.network.KPacketSender;

// Your class don't have to extend ModItem or ModBlock to be registered
public class TestItem extends ModItem {
	public static List<BlockPos> posList;
	public static Vec3 start;
	public static Vec3 end;

	public TestItem(Item.Properties builder) {
		super(builder);
	}

	//	@Override
	//	public InteractionResult useOn(UseOnContext context) {
	//		if (!context.getLevel().isClientSide) {
	//			Scheduler.add(new MyTask(context.getLevel(), Phase.END, "?"));
	//		}
	//		Level world = context.getLevel();
	//		Scheduler.add(new SimpleLevelTask(world, Phase.END, tick -> {
	//			if (tick >= 50) {
	//				System.out.println("五十已到");
	//				return true;
	//			} else {
	//				return false;
	//			}
	//		}));
	//		System.out.println(TestModule.FIRST_ITEM == TestModule2.FIRST_ITEM);
	//		return InteractionResult.SUCCESS;
	//		//        Level world = context.getLevel();
	//		//        Hand hand = context.getHand();
	//		//        Player player = context.getPlayer();
	//		//        BlockPos pos = context.getPos();
	//		//        Direction face = context.getFace();
	//		//        BlockState state = Blocks.CARVED_PUMPKIN.getDefaultState().with(CarvedPumpkinBlock.FACING, player.getHorizontalFacing().getOpposite());
	//		//        BlockPos result = PlayerUtil.tryPlace(world, pos, face, player, hand, state, null, true, true);
	//		//        return result == null ? InteractionResult.PASS : InteractionResult.SUCCESS;
	//	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		//        if (worldIn.isRemote)
		//        {
		//            Vec3 start = playerIn.getEyePosition(1).add(playerIn.getLookVec().scale(3));
		//            List<Vec3> points = MathUtil.fibonacciSphere(start, 2, 100, true);
		//            for (Vec3 point : points)
		//            {
		//                worldIn.addParticle(ParticleTypes.FIREWORK, point.x, point.y, point.z, 0, 0, 0);
		//            }
		//        }

		ItemStack stack = playerIn.getItemInHand(handIn);
//		//        NBTHelper data = NBTHelper.of(stack);
//		//        HitResult result = rayTrace(worldIn, playerIn, FluidMode.ANY);
//		//        if (result != null && result.getType() == Type.BLOCK)
//		//        {
//		//            BlockPos pos = ((BlockHitResult) result).getPos();
//		//            data.setPos("pos", pos);
//		//        }
//		//        else
//		//        {
//		//            data.remove("pos");
//		//        }
//		List<BlockPos> list = Lists.newLinkedList();
//		start = playerIn.getEyePosition(1);
//		end = start.add(playerIn.getLookAngle().scale(15));
//		MathUtil.posOnLine(start, end, list);
//		posList = list;


//		try {
//			RetextureRecipe recipe = new RetextureRecipe(CraftingBookCategory.MISC);
//			recipe.result = Items.DIAMOND.getDefaultInstance();
//			recipe.pattern = ShapedRecipePattern.of(Map.of('A', Ingredient.of(Items.DIAMOND)), List.of("A"));
//			recipe.textureKeys = Char2ObjectMaps.singleton('A', new String[]{"0"});
//			JsonElement e = DataModule.RETEXTURE.get().codec().encodeStart(JsonOps.INSTANCE, recipe).getOrThrow(false, Kiwi.LOGGER::error);
//			System.out.println(e);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		if (!worldIn.isClientSide) {
			KPacketSender.send(new CMyPacket(5), playerIn);
		}

		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	//	@Environment(EnvType.CLIENT)
	//	public void render(RenderLevelLastEvent event) {
	//        Screen.fill(0, 0, 20, 20, 20);
	//        Minecraft mc = Minecraft.getInstance();
	//        Player player = mc.player;
	//        ItemStack stack = player.getHeldItemMainhand();
	//        if (posList == null || stack.getItem() != TestModule.FIRST_ITEM) {
	//            return;
	//        }
	//
	//        RenderUtil.beginLevel();
	//        GlStateManager.disableDepthTest();
	//        GlStateManager.disableTexture();
	//        GlStateManager.lineWidth(5);
	//
	//        Tessellator tessellator = Tessellator.getInstance();
	//        BufferBuilder buffer = tessellator.getBuffer();
	//        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
	//        if (start != null) {
	//            buffer.pos(start.x, start.y, start.z).color(0.5f, 0, 0, 0.5f).endVertex();
	//        }
	//        if (end != null) {
	//            buffer.pos(end.x, end.y, end.z).color(0.5f, 0, 0, 0.5f).endVertex();
	//        }
	//        for (BlockPos pos : posList) {
	//            AABB box = new AABB(pos);
	//            LevelRenderer.drawBoundingBox(buffer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, 0, 0, 0.5f, 0.5f);
	//        }
	//        tessellator.draw();
	//        RenderUtil.endLevel();
	//
	//        GlStateManager.enableTexture();
	//        GlStateManager.enableDepthTest();
	//        //RenderUtil.drawPos(pos);
	//	}
}
