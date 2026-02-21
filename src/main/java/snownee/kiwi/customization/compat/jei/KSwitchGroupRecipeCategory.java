package snownee.kiwi.customization.compat.jei;

import java.util.List;

import com.google.common.collect.Lists;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawablesView;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IScrollGridWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class KSwitchGroupRecipeCategory extends AbstractRecipeCategory<KSwitchGroupRecipe> {
	private static final int WIDTH = 142;
	private static final int HEIGHT = 110;

	public KSwitchGroupRecipeCategory(IGuiHelper guiHelper, RecipeType<KSwitchGroupRecipe> recipeType) {
		super(
				recipeType,
				Component.translatable("emi.category.kiwi.kswitch"),
				guiHelper.createDrawableItemLike(Items.CHEST),
				WIDTH,
				HEIGHT
		);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, KSwitchGroupRecipe recipe, IFocusGroup focuses) {
		builder.addInputSlot()
				.addIngredients(recipe.family().value().ingredient())
				.setStandardSlotBackground();

		for (Holder.Reference<Item> item : recipe.family().value().itemHolders()) {
			builder.addOutputSlot().addItemStack(item.value().getDefaultInstance());
		}
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, KSwitchGroupRecipe recipe, IFocusGroup focuses) {
		ResourceLocation key = recipe.family().key();
		List<FormattedText> text = Lists.newArrayList();
		String langKey = "kiwi.family.%s".formatted(key.toLanguageKey());
		if (I18n.exists(langKey)) {
			text.add(Component.translatable(langKey));
		}
		text.add(Component.literal(key.toString()).withStyle(ChatFormatting.GRAY));
		builder.addText(text, getWidth() - 22, 20)
				.setPosition(22, 0)
				.setColor(0xFF505050)
				.setLineSpacing(0)
				.setTextAlignment(VerticalAlignment.CENTER)
				.setTextAlignment(HorizontalAlignment.CENTER);

		IRecipeSlotDrawablesView recipeSlots = builder.getRecipeSlots();
		List<IRecipeSlotDrawable> outputSlots = recipeSlots.getSlots(RecipeIngredientRole.OUTPUT);

		IScrollGridWidget scrollGridWidget = builder.addScrollGridWidget(outputSlots, 7, 5);
		scrollGridWidget.setPosition(0, 0, getWidth(), getHeight(), HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);

		IRecipeSlotDrawable inputSlot = recipeSlots.getSlots(RecipeIngredientRole.INPUT).getFirst();
		inputSlot.setPosition(scrollGridWidget.getScreenRectangle().position().x() + 1, 1);
	}

	@Override
	public ResourceLocation getRegistryName(KSwitchGroupRecipe recipe) {
		return recipe.family().key();
	}
}
