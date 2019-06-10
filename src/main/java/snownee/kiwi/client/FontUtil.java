//package snownee.kiwi.client;
//
//import com.google.common.collect.Lists;
//import com.ibm.icu.text.ArabicShaping;
//import com.ibm.icu.text.ArabicShapingException;
//import com.ibm.icu.text.Bidi;
//import net.minecraft.client.gui.FontRenderer;
//
//import java.util.List;
//
//public class FontUtil
//{
//    public static List<String> drawSplitStringOverflow(FontRenderer fontRenderer, String str, int x, int y, int wrapWidth, int threshold, int textColor, boolean addShadow)
//    {
//        List<String> strs = fontRenderer.listFormattedStringToWidth(str, wrapWidth);
//        return drawSplitStringOverflow(fontRenderer, strs, x, y, wrapWidth, threshold, textColor, addShadow);
//    }
//
//    public static List<String> drawSplitStringOverflow(FontRenderer fontRenderer, List<String> strs, int x, int y, int wrapWidth, int threshold, int textColor, boolean addShadow)
//    {
//        threshold += y;
//        for (int i = 0; i < strs.size(); i++)
//        {
//            y += fontRenderer.FONT_HEIGHT;
//            if (y >= threshold && i < strs.size() - 1)
//            {
//                return strs.subList(i, strs.size());
//            }
//            renderStringAligned(fontRenderer, strs.get(i), x, y, wrapWidth, textColor, addShadow);
//        }
//        return Lists.newArrayList();
//    }
//
//    private static String trimStringNewline(String text)
//    {
//        while (text != null && text.endsWith("\n"))
//        {
//            text = text.substring(0, text.length() - 1);
//        }
//
//        return text;
//    }
//
//    private static int renderStringAligned(FontRenderer fontRenderer, String text, int x, int y, int width, int color, boolean dropShadow)
//    {
//        if (fontRenderer.getBidiFlag())
//        {
//            int i = fontRenderer.getStringWidth(bidiReorder(text));
//            x = x + width - i;
//        }
//
//        return fontRenderer.drawString(text, x, y, color, dropShadow);
//    }
//
//    private static String bidiReorder(String text)
//    {
//        try
//        {
//            Bidi bidi = new Bidi((new ArabicShaping(8)).shape(text), 127);
//            bidi.setReorderingMode(0);
//            return bidi.writeReordered(2);
//        }
//        catch (ArabicShapingException var3)
//        {
//            return text;
//        }
//    }
//}
