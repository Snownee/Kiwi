package snownee.kiwi.potion;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PotionMod extends Potion
{
    private final boolean shouldRender;
    private final boolean canCure;
    private final boolean registerType;
    final int tickrate;
    private final String name;

    public PotionMod(String name, boolean shouldRender, int icon, boolean isBadEffect, int color, int tick, boolean canCure)
    {
        this(name, shouldRender, icon, isBadEffect, color, tick, canCure, true);
    }

    public PotionMod(String name, boolean shouldRender, int icon, boolean isBadEffect, int color, int tick, boolean canCure, boolean registerType)
    {
        super(isBadEffect, color);
        if (!isBadEffect)
        {
            setBeneficial();
        }
        this.name = name;
        this.shouldRender = shouldRender;
        this.canCure = canCure;
        this.registerType = registerType;
        this.tickrate = tick;
        this.setIconIndex(icon % 8, icon / 8);
    }

    public void register(String modid)
    {
        setPotionName(modid + ".potion." + name);
        setRegistryName(modid, name);
    }

    @Override
    public boolean shouldRender(PotionEffect effect)
    {
        return shouldRender;
    }

    @Override
    public List<ItemStack> getCurativeItems()
    {
        return canCure ? super.getCurativeItems() : Collections.emptyList();
    }

    @Override
    public boolean shouldRenderInvText(PotionEffect effect)
    {
        return shouldRender(effect);
    }

    @Override
    public boolean shouldRenderHUD(PotionEffect effect)
    {
        return shouldRender(effect);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getStatusIconIndex()
    {
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(getRegistryName().getNamespace(), "textures/gui/potions.png"));
        return super.getStatusIconIndex();
    }

    public Collection<PotionType> getPotionTypes()
    {
        return registerType ? Arrays.asList(new PotionType(name, new PotionEffect(this, isBadEffect() ? 600 : 1200, 0))) : Collections.EMPTY_LIST;
    }
}
