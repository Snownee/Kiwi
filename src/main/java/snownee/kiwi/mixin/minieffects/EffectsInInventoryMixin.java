package snownee.kiwi.mixin.minieffects;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import snownee.kiwi.minieffects.KiwiEffectsInInventory;
import snownee.kiwi.minieffects.MiniEffects;
import snownee.kiwi.minieffects.MiniEffectsConfig;

@Mixin(EffectsInInventory.class)
public abstract class EffectsInInventoryMixin implements KiwiEffectsInInventory {

	@Shadow
	@Final
	private Minecraft minecraft;
	@Shadow
	@Final
	private AbstractContainerScreen<?> screen;

	@Shadow
	public abstract boolean canSeeEffects();

	@Unique
	private boolean kiwi$isExpanded;
	@Unique
	private int kiwi$effects;
	@Unique
	private @Nullable ScreenRectangle kiwi$buttonArea;
	@Unique
	private final List<ScreenRectangle> kiwi$areas = Lists.newArrayList();
	@Unique
	private final ItemStack kiwi$iconItem = new ItemStack(Items.POTION);

	@Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
	private void kiwi$extractEffects(
			GuiGraphicsExtractor graphics,
			Collection<MobEffectInstance> activeEffects,
			int x0,
			int yStep,
			int mouseX,
			int mouseY,
			int maxWidth,
			CallbackInfo ci) {
		int effects = 0, bad = 0;
		for (MobEffectInstance effectInstance : activeEffects) {
			++effects;
			if (!effectInstance.getEffect().value().isBeneficial()) {
				++bad;
			}
		}
		kiwi$effects = effects;
		kiwi$areas.clear();
		if (effects == 0) {
			return;
		}

		if (canSeeEffects()) {
			return;
		}
		if (MiniEffectsConfig.holdTabToShow) {
			ci.cancel();
			return;
		}

		kiwi$iconItem.set(
				DataComponents.POTION_CONTENTS,
				new PotionContents(Optional.empty(), Optional.empty(), List.copyOf(activeEffects), Optional.empty()));

		x0 = MiniEffects.isLeftSide() ? screen.leftPos - 12 : x0 - 3;
		kiwi$buttonArea = new ScreenRectangle(x0, screen.topPos, 12, 12);
		MiniEffects.extractRenderState(graphics, minecraft.font, Objects.requireNonNull(kiwi$buttonArea), kiwi$iconItem, effects, bad);
		ci.cancel();
	}

	@WrapOperation(
			method = "extractEffects", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/EffectsInInventory;extractText(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/Font;IIIIII)V"))
	private void kiwi$recordAreas(
			EffectsInInventory instance,
			final GuiGraphicsExtractor graphics,
			final Component effectText,
			final Component duration,
			final Font font,
			final int x0,
			final int y0,
			final int textureWidth,
			final int yStep,
			final int mouseX,
			final int mouseY,
			Operation<Void> original) {
		kiwi$areas.add(new ScreenRectangle(x0, y0, textureWidth, yStep));
		original.call(instance, graphics, effectText, duration, font, x0, y0, textureWidth, yStep, mouseX, mouseY);
	}

	@Override
	public @Nullable ScreenRectangle kiwi$buttonArea() {
		if (kiwi$effects == 0 || kiwi$isExpanded()) {
			return null;
		}
		return kiwi$buttonArea;
	}

	@Override
	public List<ScreenRectangle> kiwi$areas() {
		ScreenRectangle buttonArea = kiwi$buttonArea();
		if (buttonArea != null) {
			return List.of(buttonArea);
		}
		return kiwi$areas;
	}

	@Override
	public boolean kiwi$isExpanded() {
		return kiwi$isExpanded || MiniEffectsConfig.holdTabToShow;
	}

	@Override
	public void kiwi$setExpanded(boolean bl) {
		kiwi$isExpanded = bl;
	}

	@Inject(at = @At("HEAD"), method = "canSeeEffects", cancellable = true)
	private void kiwi$canSeeEffects(CallbackInfoReturnable<Boolean> ci) {
		if (!kiwi$isExpanded()) {
			ci.setReturnValue(false);
		} else if (MiniEffectsConfig.holdTabToShow &&
				Minecraft.getInstance().options.keyInventory.key.getValue() == InputConstants.KEY_TAB) {
			ci.setReturnValue(false);
		} else if (MiniEffectsConfig.holdTabToShow && !InputConstants.isKeyDown(
				Minecraft.getInstance().getWindow(),
				InputConstants.KEY_TAB)) {
			ci.setReturnValue(false);
		}
	}
}