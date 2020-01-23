package land.pvp.practice.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MathUtil {
    public static double roundToHalves(double d) {
        return Math.ceil(d) / 2.0;
    }

    public static boolean isWithin(int i, int j, int range) {
        return Math.abs(i - j) <= range;
    }
}
