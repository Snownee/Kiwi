package snownee.kiwi.test;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
//import snownee.kiwi.client.AdvancedFontRenderer;
import snownee.kiwi.item.ModItem;
import snownee.kiwi.util.PlayerUtil;

// Your class don't have to extends ModItem or ModBlock to be registered
public class TestItem extends ModItem
{

    public TestItem(Item.Properties builder)
    {
        super(builder);
    }

    //    @Override
    //    public FontRenderer getFontRenderer(ItemStack stack)
    //    {
    //        return AdvancedFontRenderer.INSTANCE;
    //    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context)
    {
        World world = context.getWorld();
        Hand hand = context.getHand();
        PlayerEntity player = context.getPlayer();
        BlockPos pos = context.getPos();
        Direction face = context.getFace();
        BlockState state = Blocks.CARVED_PUMPKIN.getDefaultState().with(CarvedPumpkinBlock.FACING, player.getHorizontalFacing().getOpposite());
        BlockPos result = PlayerUtil.tryPlace(world, pos, face, player, hand, state, null, true, true);
        return result == null ? ActionResultType.PASS : ActionResultType.SUCCESS;
    }

    //    @Override
    //    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, EnumHand handIn)
    //    {
    //        ItemStack stack = playerIn.getHeldItem(handIn);
    //        NBTTagCompound tag = NBTHelper.of(stack).setInt("Fluid.Amount", 1000).getTag("Fluid");
    //        System.out.println(tag);
    //        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    //    }
}
