package snownee.kiwi.client;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Function;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.item.ModItem;

@EventBusSubscriber(Dist.CLIENT)
public final class DebugTooltip {
    private DebugTooltip() {}

    private static CompoundNBT lastNBT;
    private static ITextComponent lastFormatted;
    private static Function<CompoundNBT, ITextComponent> formatter;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltip(ItemTooltipEvent event) {
        if (KiwiClientConfig.globalTooltip)
            ModItem.addTip(event.getItemStack(), event.getToolTip(), event.getFlags());
        if (!KiwiClientConfig.debugTooltip || !event.getFlags().isAdvanced()) {
            return;
        }
        ItemStack stack = event.getItemStack();
        List<ITextComponent> tooltip = event.getToolTip();

        if (Screen.hasShiftDown() && stack.hasTag()) {
            tooltip.removeIf(c -> c.getClass() == TranslationTextComponent.class && ((TranslationTextComponent) c).getKey().equals("item.nbt_tags"));
            if (lastNBT != stack.getTag()) {
                switch (KiwiClientConfig.debugTooltipNBTFormatter) {
                case "kiwi":
                    formatter = tag -> {
                        TextFormatting[] colors = { TextFormatting.LIGHT_PURPLE, TextFormatting.RED, TextFormatting.GOLD, TextFormatting.YELLOW, TextFormatting.GREEN, TextFormatting.AQUA };
                        String s = tag.toString();
                        StringBuilder sb = new StringBuilder();
                        int i = 0;
                        boolean quoted = false;
                        for (int ch : s.chars().boxed().collect(Collectors.toList())) {
                            boolean special = false;
                            if (quoted) {
                                if (ch == '"') {
                                    quoted = false;
                                    sb.appendCodePoint(ch);
                                    sb.append(TextFormatting.WHITE);
                                    continue;
                                }
                            } else {
                                if (ch == ':' || ch == ',') {
                                    sb.append(TextFormatting.GRAY);
                                    sb.appendCodePoint(ch);
                                    sb.append(TextFormatting.WHITE);
                                    continue;
                                } else if (ch == '"') {
                                    quoted = true;
                                    sb.append(TextFormatting.GRAY);
                                } else if (ch == '{' || ch == '[') {
                                    ++i;
                                    special = true;
                                } else if (ch == '}' || ch == ']') {
                                    special = true;
                                }
                            }
                            if (special) {
                                int colotIndex = i % colors.length;
                                sb.append(colors[colotIndex]);
                            }
                            sb.appendCodePoint(ch);
                            if (special) {
                                sb.append(TextFormatting.WHITE);
                                if (ch == '}' || ch == ']') {
                                    --i;
                                }
                            }
                        }
                        return new StringTextComponent(sb.toString());
                    };
                    break;
                case "vanilla":
                    formatter = tag -> stack.getTag().toFormattedComponent();
                    break;
                default:
                    formatter = tag -> new StringTextComponent(tag.toString());
                    break;
                }

                lastNBT = stack.getTag();
                lastFormatted = formatter.apply(lastNBT).copyRaw()./*applyTextStyle*/func_240699_a_(TextFormatting.RESET);
            }
            tooltip.add(lastFormatted);
        } else {
            stack.getItem().getTags().stream().map(Object::toString).forEach(id -> {
                tooltip.add(new StringTextComponent("#" + id)./*applyTextStyle*/func_240699_a_(TextFormatting.DARK_GRAY));
            });
        }
    }
}
