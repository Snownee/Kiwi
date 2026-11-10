package snownee.kiwi.contributor.client.gui;

import java.util.Objects;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Pose;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.config.ConfigHandler;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.contributor.Contributors;
import snownee.kiwi.contributor.ContributorsClient;
import snownee.kiwi.contributor.CosmeticRenderState;
import snownee.kiwi.contributor.client.CosmeticLayer;

public class CosmeticScreen extends Screen {

	private @Nullable List list;
	private @Nullable Identifier currentCosmetic;
	private @Nullable Entry selectedEntry;
	private final long animationStart = Util.getMillis();

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
			minecraft.gui.setScreen(null);
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
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		extractPlayerPreview(graphics, mouseX, mouseY);
	}

	private void extractPlayerPreview(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		LocalPlayer player = minecraft.player;
		if (player == null) {
			return;
		}
		int x0 = 180;
		int y0 = 60;
		int x1 = width - 30;
		int y1 = height - 30;
		if (x1 - x0 < 140 || y1 - y0 < 80) {
			return;
		}
		int gap = 16;
		int panelWidth = (x1 - x0 - gap) / 2;
		if (panelWidth < 70) {
			return;
		}
		int splitX = x0 + panelWidth;
		extractPlayerPreview(graphics, player, x0, y0, splitX, y1, mouseX, mouseY, false);
		extractPlayerPreview(graphics, player, splitX + gap, y0, x1, y1, mouseX, mouseY, true);
	}

	private void extractPlayerPreview(
			GuiGraphicsExtractor graphics,
			LocalPlayer player,
			int x0,
			int y0,
			int x1,
			int y1,
			int mouseX,
			int mouseY,
			boolean back) {
		graphics.fill(x0, y0, x1, y1, 0x33000000);
		int scale = Math.clamp((y1 - y0) / 4, 30, 90);

		float centerX = (x0 + x1) / 2.0f;
		float centerY = (y0 + y1) / 2.0f;
		float headYaw = (float) Math.atan((centerX - mouseX) / 40.0f) * 20.0f;
		float headPitch = (float) Math.atan((centerY - mouseY) / 40.0f) * 20.0f;
		Quaternionf rotationZ = new Quaternionf().rotateZ((float) Math.PI);
		Quaternionf rotationX = new Quaternionf().rotateX(headPitch * 0.017453292f);
		rotationZ.mul(rotationX);

		var renderer = minecraft.getEntityRenderDispatcher().getRenderer(player);
		EntityRenderState state = renderer.createRenderState(player, 1.0f);
		state.shadowPieces.clear();
		state.outlineColor = 0;
		// The game is paused while a screen is open, so the player's tick count (and therefore the
		// extracted age) does not advance. Use elapsed wall time so cosmetic animations keep playing.
		state.ageInTicks = (Util.getMillis() - animationStart) / 50.0f;
		if (state instanceof LivingEntityRenderState livingState) {
			livingState.bodyRot = (back ? 0.0f : 180.0f) + headYaw;
			livingState.yRot = headYaw;
			livingState.xRot = livingState.pose == Pose.FALL_FLYING ? 0.0f : -headPitch;
			livingState.boundingBoxWidth /= livingState.scale;
			livingState.boundingBoxHeight /= livingState.scale;
			livingState.scale = 1.0f;
		}
		if (state instanceof AvatarRenderState avatarState) {
			CosmeticLayer parent = CosmeticLayer.ALL_LAYERS.get(player.getSkin().model());
			CosmeticLayer layer = selectedEntry == null || selectedEntry.id == null || parent == null ?
					null :
					CosmeticLayer.createRenderer(selectedEntry.id, parent);
			CosmeticRenderState cosmeticState = (CosmeticRenderState) avatarState;
			cosmeticState.kiwi$setCosmeticLayer(layer);
			cosmeticState.kiwi$setName(player.getGameProfile().name());
		}
		Vector3f translation = new Vector3f(0.0f, state.boundingBoxHeight / 2.0f, 0.0f);
		graphics.entity(state, scale, translation, rotationZ, rotationX, x0, y0, x1, y1);
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
