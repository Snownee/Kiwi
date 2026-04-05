package snownee.kiwi.contributor.client.gui;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.config.ConfigHandler;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.contributor.Contributors;
import snownee.kiwi.contributor.ContributorsClient;

public class CosmeticScreen extends Screen {

	private List list;
	@Nullable
	private Identifier currentCosmetic;
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
		list = new List(getMinecraft(), 150, height, 0, 20);
		list.setX(20);
		list.addEntry(selectedEntry = new Entry(this, null));
		String playerName = getPlayerName();
		boolean added = false;
		for (Identifier tier : Contributors.getRenderableTiers()) {
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
			getMinecraft().setScreen(null);
		}
		addRenderableWidget(Button.builder(
				Component.translatable(KiwiClientConfig.cosmeticScreenKeybind ? "gui.kiwi.cosmetic.enabled" : "gui.kiwi.cosmetic.disabled"),
				b -> {
					KiwiClientConfig.cosmeticScreenKeybind = !KiwiClientConfig.cosmeticScreenKeybind;
					KiwiConfigManager.getHandler(KiwiClientConfig.class).save();
					b.setMessage(Component.translatable(KiwiClientConfig.cosmeticScreenKeybind ?
							"gui.kiwi.cosmetic.enabled" :
							"gui.kiwi.cosmetic.disabled"));
				}).pos(180, 30).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float pTicks) {
		extractBackground(guiGraphics, mouseX, mouseY, pTicks);
		super.extractRenderState(guiGraphics, mouseX, mouseY, pTicks);
		list.extractRenderState(guiGraphics, mouseX, mouseY, pTicks);
		guiGraphics.text(getMinecraft().font, title, 180, 10, 0xFFFFFF);
	}

	@Override
	public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
		list.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
		return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
	}

	@Override
	public boolean mouseDragged(
			double p_mouseDragged_1_,
			double p_mouseDragged_3_,
			int p_mouseDragged_5_,
			double p_mouseDragged_6_,
			double p_mouseDragged_8_) {
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
		public int addEntry(CosmeticScreen.Entry p_93487_) {
			return super.addEntry(p_93487_);
		}

	}

	private static class Entry extends ObjectSelectionList.Entry<Entry> {

		private final CosmeticScreen parent;
		@Nullable
		private final Identifier id;
		private final String name;

		public Entry(CosmeticScreen parent, @Nullable Identifier id) {
			this.parent = parent;
			this.id = id;
			name = id == null ? "-" : I18n.get(Util.makeDescriptionId("cosmetic", id));
		}

		@Override
		public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
			int color = hovered ? 0xFFFFFFAA : 0xFFFFFF;
			if (this == parent.selectedEntry) {
				color = 0xFFFFFF77;
			}
			graphics.text(parent.font, name, getContentX() + 43, getContentY(), color);
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
			parent.selectedEntry = this;
			return false;
		}

		@Override
		public Component getNarration() {
			return Component.translatable(name);
		}

	}

}
