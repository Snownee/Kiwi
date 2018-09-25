package snownee.kiwi.potion;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

public class PotionMod extends Potion
{
    private final boolean shouldRender;
    private final boolean canCure;
    final int tickrate;
    private final String name;

    public PotionMod(String name, boolean shouldRender, int icon, boolean isBadEffect, int color, int tick, boolean canCure)
    {
        super(isBadEffect, color);
        if (!isBadEffect)
        {
            setBeneficial();
        }
        this.name = name;
        this.shouldRender = shouldRender;
        this.canCure = canCure;
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
    public int getStatusIconIndex()
    {
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(getRegistryName().getNamespace(), "textures/gui/potions.png"));
        return super.getStatusIconIndex();
    }
}
