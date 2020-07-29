package snownee.kiwi.contributor.client.gui;

import java.util.Objects;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.gui.widget.list.ExtendedList.AbstractListEntry;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.config.ConfigHandler;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.contributor.Contributors;

@OnlyIn(Dist.CLIENT)
public class RewardScreen extends Screen {

    private List list;
    @Nullable
    private ResourceLocation currentReward;
    private Entry selectedEntry;

    public RewardScreen() {
        super(new TranslationTextComponent("gui.kiwi.reward"));
    }

    @Override
    protected void init() {
        currentReward = Contributors.PLAYER_EFFECTS.get(getPlayerName());
        list = new List(minecraft, 150, height, 0, height, 20);
        list.setLeftPos(20);
        list.addEntry(selectedEntry = new Entry(this, null));
        String playerName = getPlayerName();
        for (ResourceLocation tier : Contributors.getRenderableTiers()) {
            if (Contributors.isContributor(tier.getNamespace(), playerName, tier.getPath())) {
                Entry entry = new Entry(this, tier);
                list.addEntry(entry);
                if (tier.equals(currentReward)) {
                    selectedEntry = entry;
                }
            }
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float pTicks) {
        renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, pTicks);
        list.render(matrixStack, mouseX, mouseY, pTicks);
    }

    @Override
    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
        list.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
    }

    @Override
    public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_) {
        list.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
        return super.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
    }

    @Override
    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
        list.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
        return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
    }

    @Override
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
        list.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
        return super.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        list.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }

    @Override
    public void onClose() {
        super.onClose();
        list = null;
        ConfigHandler cfg = KiwiConfigManager.getHandler(KiwiClientConfig.class);
        ConfigValue<String> val = (ConfigValue<String>) cfg.getValueByPath("contributorEffect");
        if (currentReward != null && selectedEntry.id == null) {
            val.set("");
            cfg.refresh();
            Contributors.changeEffect();
        } else if (selectedEntry != null && !Objects.equals(selectedEntry.id, currentReward)) {
            val.set(selectedEntry.id.toString());
            cfg.refresh();
            Contributors.changeEffect();
        }
    }

    private static String getPlayerName() {
        return Minecraft.getInstance().getSession().getUsername();
    }

    private static class List extends ExtendedList<Entry> {

        public List(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
            super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        }

        @Override
        public int addEntry(Entry p_addEntry_1_) {
            return super.addEntry(p_addEntry_1_);
        }

    }

    private static class Entry extends AbstractListEntry<Entry> {

        private final RewardScreen parent;
        @Nullable
        private final ResourceLocation id;
        private final String name;

        public Entry(RewardScreen parent, ResourceLocation id) {
            this.parent = parent;
            this.id = id;
            this.name = id == null ? "-" : I18n.format(Util.makeTranslationKey("reward", id));
        }

        @Override
        public void render(MatrixStack matrixStack, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hover, float partialTicks) {
            int color = hover ? 0xFFFFAA : 0xFFFFFF;
            if (this == parent.selectedEntry) {
                color = 0xFFFF77;
            }
            parent.font.drawString(matrixStack, name, left + 43, top + 2, color);
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            parent.selectedEntry = this;
            return false;
        }

    }

}
