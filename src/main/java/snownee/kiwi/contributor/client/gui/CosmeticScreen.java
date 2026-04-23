package snownee.kiwi.contributor.client.gui;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.config.ConfigHandler;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.contributor.Contributors;
import snownee.kiwi.contributor.ContributorsClient;

public class CosmeticScreen extends Screen {

	private @Nullable List list;
	private @Nullable Identifier currentCosmetic;
	private @Nullable Entry selectedEntry;

	public CosmeticScreen() {
		super(Component.translatable("gui.kiwi.cosmetic"));
	}

	@Override
	protected void init() {
		currentCosmetic = Contributors.PLAYER_COSMETICS.get(ContributorsClient.getSelfUUID());
		list = new List(minecraft, 150, height, 0, 20);
		list.setX(20);
		list.addEntry(selectedEntry = new Entry(this, null));
		String playerName = ContributorsClient.getSelfName();
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
			minecraft.setScreen(null);
		}
		StringWidget stringWidget = new StringWidget(title, minecraft.font);
		stringWidget.setPosition(180, 10);
		addRenderableWidget(stringWidget);
		addRenderableWidget(list);
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
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		list.mouseClicked(event, doubleClick);
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		list.mouseDragged(event, dx, dy);
		return super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		list.mouseReleased(event);
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f, double g) {
		list.mouseScrolled(d, e, f, g);
		return super.mouseScrolled(d, e, f, g);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		list.keyPressed(event);
		return super.keyPressed(event);
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
			int color = hovered ? 0xFFFFFFAA : 0xFFFFFFFF;
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
