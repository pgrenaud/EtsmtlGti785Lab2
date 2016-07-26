package ca.etsmtl.gti785.peer.util;

import java.util.Locale;

public class LocationUtil {

    public static String getPrintableDistance(float distance) {
        if (distance < 1000) {
            return String.format(Locale.getDefault(), "%.0fm", distance);
        } else {
            return String.format(Locale.getDefault(), "%.3fkm", distance / 1000);
        }
    }
}
