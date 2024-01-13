package snownee.kiwi.contributor.client.gui;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.config.ConfigHandler;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.contributor.Contributors;
import snownee.kiwi.contributor.ContributorsClient;

public class CosmeticScreen extends Screen {

	private List list;
	@Nullable
	private ResourceLocation currentCosmetic;
	private Entry selectedEntry;

	public CosmeticScreen() {
		super(Component.translatable("gui.kiwi.cosmetic"));
	}

	private static String getPlayerName() {
		return Minecraft.getInstance().getUser().getName();
	}

	@Override
	protected void init() {
		currentCosmetic = Contributors.PLAYER_COSMETICS.get(getPlayerName());
		list = new List(minecraft, 150, height, 0, 20);
		list.setX(20);
		list.addEntry(selectedEntry = new Entry(this, null));
		String playerName = getPlayerName();
		boolean added = false;
		for (ResourceLocation tier : Contributors.getRenderableTiers()) {
			if (Contributors.isContributor(tier.getNamespace(), playerName, tier.getPath())) {
				Entry entry = new Entry(this, tier);
				list.addEntry(entry);
				added = true;
				if (tier.equals(currentCosmetic)) {
					selectedEntry = entry;
				}
			}
		}
		if (!added) {
			minecraft.setScreen(null);
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float pTicks) {
		renderBackground(guiGraphics, mouseX, mouseY, pTicks);
		super.render(guiGraphics, mouseX, mouseY, pTicks);
		list.render(guiGraphics, mouseX, mouseY, pTicks);
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
	public boolean mouseScrolled(double d, double e, double f, double g) {
		list.mouseScrolled(d, e, f, g);
		return super.mouseScrolled(d, e, f, g);
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
		if (currentCosmetic != null && selectedEntry.id == null) {
			KiwiClientConfig.contributorCosmetic = "";
			cfg.save();
			ContributorsClient.changeCosmetic();
		} else if (selectedEntry != null && !Objects.equals(selectedEntry.id, currentCosmetic)) {
			KiwiClientConfig.contributorCosmetic = selectedEntry.id.toString();
			cfg.save();
			ContributorsClient.changeCosmetic();
		}
	}

	private static class List extends ObjectSelectionList<Entry> {

		public List(Minecraft mcIn, int widthIn, int heightIn, int topIn, int slotHeightIn) {
			super(mcIn, widthIn, heightIn, topIn, slotHeightIn);
		}

		@Override
		public int addEntry(snownee.kiwi.contributor.client.gui.CosmeticScreen.Entry p_93487_) {
			return super.addEntry(p_93487_);
		}

	}

	private static class Entry extends ObjectSelectionList.Entry<Entry> {

		private final CosmeticScreen parent;
		@Nullable
		private final ResourceLocation id;
		private final String name;

		public Entry(CosmeticScreen parent, ResourceLocation id) {
			this.parent = parent;
			this.id = id;
			name = id == null ? "-" : I18n.get(Util.makeDescriptionId("cosmetic", id));
		}

		@Override
		public void render(GuiGraphics guiGraphics, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hover, float partialTicks) {
			int color = hover ? 0xFFFFAA : 0xFFFFFF;
			if (this == parent.selectedEntry) {
				color = 0xFFFF77;
			}
			guiGraphics.drawString(parent.font, name, left + 43, top + 2, color);
		}

		@Override
		public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
			parent.selectedEntry = this;
			return false;
		}

		@Override
		public Component getNarration() {
			return Component.translatable(name);
		}

	}

}
