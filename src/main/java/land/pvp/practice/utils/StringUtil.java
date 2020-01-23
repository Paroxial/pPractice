package land.pvp.practice.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtil {
    public static String formatNumberWithCommas(int elo) {
        return String.format("%,d", elo);
    }
}
