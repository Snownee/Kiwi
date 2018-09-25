package snownee.kiwi.client;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiLanguage;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import snownee.kiwi.Kiwi;

@Mod.EventBusSubscriber(modid = Kiwi.MODID, value = Side.CLIENT)
public class AdvancedFontRenderer extends FontRenderer implements ISelectiveResourceReloadListener
{
    public static final AdvancedFontRenderer INSTANCE = new AdvancedFontRenderer();

    @SubscribeEvent
    public static void onLanguageGuiOpening(GuiScreenEvent.ActionPerformedEvent.Post event)
    {
        if (event.getGui() instanceof GuiLanguage)
        {
            if (event.getButton() instanceof GuiOptionButton)
            {
                if (((GuiOptionButton) event.getButton()).getOption() == GameSettings.Options.FORCE_UNICODE_FONT)
                {
                    Minecraft mc = Minecraft.getMinecraft();
                    AdvancedFontRenderer.INSTANCE.setUnicodeFlag(mc.gameSettings.forceUnicodeFont || mc.getLanguageManager().isCurrentLocaleUnicode());
                }
            }
        }
    }

    public AdvancedFontRenderer()
    {
        super(Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"), Minecraft.getMinecraft().renderEngine, Minecraft.getMinecraft().fontRenderer.getUnicodeFlag());
        if (Minecraft.getMinecraft().gameSettings.language != null)
        {
            this.setUnicodeFlag(Minecraft.getMinecraft().isUnicode());
            this.setBidiFlag(Minecraft.getMinecraft().getLanguageManager().isCurrentLanguageBidirectional());
        }
        IReloadableResourceManager manager = (IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
        manager.registerReloadListener(this);
    }

    // Copy of renderStringAtPos func_78255_a with changes
    // Changes:
    //   1. Extended formatting code `§x`, allows arbitrary hex color code
    // TODO (3TUSK): Character.toLowerCase(text.charAt(i + 1)), line 50
    @Override
    public void renderStringAtPos(String text, boolean shadow)
    {
        for (int i = 0; i < text.length(); ++i)
        {
            char c0 = text.charAt(i);

            if (c0 == 167 && i + 1 < text.length())
            {
                int i1 = "0123456789abcdefklmnorx".indexOf(String.valueOf(text.charAt(i + 1)).toLowerCase(Locale.ROOT).charAt(0));

                if (i1 < 16)
                {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;

                    if (i1 < 0 || i1 > 15)
                    {
                        i1 = 15;
                    }

                    if (shadow)
                    {
                        i1 += 16;
                    }

                    int j1 = this.colorCode[i1];
                    this.textColor = j1;
                    setColor((j1 >> 16) / 255.0F, (j1 >> 8 & 255) / 255.0F, (j1 & 255) / 255.0F, this.alpha);
                }
                else if (i1 == 16)
                {
                    this.randomStyle = true;
                }
                else if (i1 == 17)
                {
                    this.boldStyle = true;
                }
                else if (i1 == 18)
                {
                    this.strikethroughStyle = true;
                }
                else if (i1 == 19)
                {
                    this.underlineStyle = true;
                }
                else if (i1 == 20)
                {
                    this.italicStyle = true;
                }
                else if (i1 == 21)
                {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;
                    setColor(this.red, this.blue, this.green, this.alpha);
                }
                else if (i1 == 22 && i + 7 < text.length()) // x patch
                {
                    if (text.substring(i + 2, i + 8).matches("[0-9a-fA-F]{6}"))
                    {
                        float r = Integer.parseInt(text.substring(i + 2, i + 4), 16) / 255F;
                        float g = Integer.parseInt(text.substring(i + 4, i + 6), 16) / 255F;
                        float b = Integer.parseInt(text.substring(i + 6, i + 8), 16) / 255F;
                        if (shadow)
                        {
                            r = r / 3;
                            g = g / 3;
                            b = b / 3;
                        }
                        setColor(r, g, b, this.alpha);
                    }
                    i += 6;
                }

                ++i;
            }
            else
            {
                int j = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".indexOf(c0);

                if (this.randomStyle && j != -1)
                {
                    int k = this.getCharWidth(c0);
                    char c1;

                    while (true)
                    {
                        j = this.fontRandom.nextInt("\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".length());
                        c1 = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".charAt(j);

                        if (k == this.getCharWidth(c1))
                        {
                            break;
                        }
                    }

                    c0 = c1;
                }

                float f1 = j == -1 || getUnicodeFlag() ? 0.5f : 1f;
                boolean flag = (c0 == 0 || j == -1 || getUnicodeFlag()) && shadow;

                if (flag)
                {
                    this.posX -= f1;
                    this.posY -= f1;
                }

                float f = this.renderChar(c0, this.italicStyle);
                if (c0 == '，')
                {
                    f = 5;
                }

                if (flag)
                {
                    this.posX += f1;
                    this.posY += f1;
                }

                if (this.boldStyle)
                {
                    this.posX += f1;

                    if (flag)
                    {
                        this.posX -= f1;
                        this.posY -= f1;
                    }

                    this.renderChar(c0, this.italicStyle);
                    this.posX -= f1;

                    if (flag)
                    {
                        this.posX += f1;
                        this.posY += f1;
                    }

                    ++f;
                }
                doDraw(f);
            }
        }
    }

    @Override
    public int getCharWidth(char character)
    {
        return character == '，' ? 9 : super.getCharWidth(character);
    }

    private static final String COLOR_CODE = "123456789abcdef";
    private static final String FORMATTING_CODE = "klmno";

    // Adapted form https://github.com/3TUSK/PanI18n/blob/ada872f7f191b7d40aaeb25d1e2821c12e973049/src/main/java/info/tritusk/pani18n/FormattingEngine.java
    // with changes to support extended formatting code §x{hex_color}
    @Nonnull
    @Override
    public List<String> listFormattedStringToWidth(String str, int wrapWidth)
    {
        BreakIterator lineBreakEngine = BreakIterator.getLineInstance(MinecraftForgeClient.getLocale());
        lineBreakEngine.setText(str);
        ArrayList<String> lines = new ArrayList<>(8);
        String cachedFormat = "";
        char color = '0', format = 'r'; // 0 is format code for black-colored-text; r is format code to reset format to default
        int start = 0; // Position of first character of each line, in terms of source string, i.e. 1st param of substring call
        int width = 0; // Width tracker
        boolean boldMode = false; // Bold font occupies extra width of one unit. Set up a tracker to track it
        String hexColor = ""; // Used for §x support
        for (int index = 0; index < str.length(); index++)
        {
            char c = str.charAt(index);

            if (c == '\n')
            { // Unconditionally cut string when there is new line
                lines.add(cachedFormat + str.substring(start, index));
                // Set start to appropriate position before next String::substring call
                start = index + 1;
                width = 0; // Clear width counter
                // And now, cache the current format for next line
                cachedFormat = determineFormat(color, format, hexColor);
                continue;
            }
            else if (c == '\u00A7') // a.k.a. '§'. Used by Minecraft to denote special format, don't count it
            {
                index++;
                char f = Character.toLowerCase(str.charAt(index));
                if (f == 'r' || f == 'R')
                {
                    color = '0';
                    format = 'r';
                }
                else if (f == 'x')
                {
                    color = '0';
                    hexColor = str.substring(index, index + 7);
                    index += 6;
                    format = 'r';
                }
                else if (FORMATTING_CODE.indexOf(f) != -1)
                {
                    format = f;
                    boldMode = f == 'l';
                }
                else if (COLOR_CODE.indexOf(f) != -1)
                {
                    color = f;
                    hexColor = ""; // Color changed
                    format = 'r'; // Reset format when new color code appears
                    boldMode = false; // Reset special format anyway, so we turn bold mode off
                }
                continue;
            }
            else
            {
                // Regular content, add its width to the tracker
                width += this.getCharWidth(c);
                if (boldMode)
                {
                    width++; // If we are bold font, occupy one more unit
                }
            }

            if (width > wrapWidth)
            {
                int end = lineBreakEngine.preceding(index);
                if (lineBreakEngine.isBoundary(index))
                {
                    // Greedy approach: try to include as many characters as possible in one line,
                    // while not violating the rules set by BreakIterator
                    end = Math.max(end, index);
                }
                String result;
                if (end <= start)
                {
                    // If the closest valid line break is before the starting point,
                    // we just take the line as it is, in order to avoid infinite loop.
                    result = cachedFormat + str.substring(start, index);
                    start = index;
                }
                else
                {
                    // If the closest valid line break is after the starting point,
                    // we will insert line break there.
                    result = cachedFormat + str.substring(start, end);
                    start = end; // substring call excludes the char at position of `end', we need to track it
                    index = start;
                }
                lines.add(result);
                index--; // Shift 1 left, so that we don't forget to count any character's width.
                width = 0; // Reset width tracker
                // And now, cache the current format for next line
                cachedFormat = determineFormat(color, format, hexColor);
            }
        }

        // Add the last piece, if exists
        String lastPiece = str.substring(start);
        if (!lastPiece.isEmpty())
        {
            lines.add(cachedFormat + str.substring(start));
        }

        return lines;
    }

    private static String determineFormat(char color, char format, String hexColor)
    {
        if (format != 'r')
        {
            if (hexColor.isEmpty())
            {
                return new String(new char[] { '\u00A7', color, '\u00A7', format });
            }
            else
            {
                return "\u00A7" + hexColor + '\u00A7' + format;
            }
        }
        else
        {
            if (hexColor.isEmpty())
            {
                return new String(new char[] { '\u00A7', color });
            }
            else
            {
                return "\u00A7" + hexColor;
            }
        }
    }

    @Override
    public void onResourceManagerReload(IResourceManager manager, Predicate<IResourceType> predicate)
    {
        if (predicate.test(VanillaResourceType.TEXTURES))
        {
            super.onResourceManagerReload(manager);
        }
        else if (predicate.test(VanillaResourceType.LANGUAGES))
        {
            LanguageManager languageManager = Minecraft.getMinecraft().getLanguageManager();
            setUnicodeFlag(languageManager.isCurrentLocaleUnicode() || Minecraft.getMinecraft().gameSettings.forceUnicodeFont);
            setBidiFlag(languageManager.isCurrentLanguageBidirectional());
        }
    }
}
