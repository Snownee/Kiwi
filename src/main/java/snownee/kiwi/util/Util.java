package snownee.kiwi.util;

import java.text.DecimalFormat;

public class Util
{
    public static String color(int color)
    {
        return String.format("§x%06x", color & 0x00FFFFFF);
    }

    public static String formatComma(long number)
    {
        return new DecimalFormat("###,###").format(number);
    }

    public static String formatCompact(long number)
    {
        int unit = 1000;
        if (number < unit)
        {
            return Long.toString(number);
        }
        int exp = (int) (Math.log(number) / Math.log(unit));
        if (exp - 2 >= 0 && exp - 2 < 6)
        {
            char pre = "kMGTPE".charAt(exp - 2);
            return String.format("%.1f %s", number / Math.pow(unit, exp), pre);
        }
        return Long.toString(number);
    }
}
