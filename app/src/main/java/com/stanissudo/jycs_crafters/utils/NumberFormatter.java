package com.stanissudo.jycs_crafters.utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * * @author Stan Permiakov
 * * created: 8/12/2025
 * * @project JYCS-Crafters
 */
public final class NumberFormatter {
    private NumberFormatter() {
    }

    // Thread-safe DecimalFormats via ThreadLocal
    private static final ThreadLocal<DecimalFormat> DF2_UPTO = ThreadLocal.withInitial(() -> {
        DecimalFormat df = new DecimalFormat("0.##");        // up to 2 decimals
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
        df.setGroupingUsed(false);
        return df;
    });

    private static final ThreadLocal<DecimalFormat> DF3_UPTO = ThreadLocal.withInitial(() -> {
        DecimalFormat df = new DecimalFormat("0.###");       // up to 3 decimals
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
        df.setGroupingUsed(false);
        return df;
    });

    private static final ThreadLocal<DecimalFormat> DF2_FIXED = ThreadLocal.withInitial(() -> {
        DecimalFormat df = new DecimalFormat("0.00");        // exactly 2 decimals
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
        df.setGroupingUsed(false);
        return df;
    });

    private static final ThreadLocal<DecimalFormat> DF3_FIXED = ThreadLocal.withInitial(() -> {
        DecimalFormat df = new DecimalFormat("0.000");       // exactly 3 decimals
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
        df.setGroupingUsed(false);
        return df;
    });

    // ---- Public helpers ----

    /**
     * e.g., 2 → "2", 2.5 → "2.5", 2.567 → "2.57"
     */
    public static String upTo2(Double v) {
        return v == null ? "" : DF2_UPTO.get().format(v);
    }

    /**
     * e.g., 2 → "2", 2.5 → "2.5", 2.5678 → "2.568"
     */
    public static String upTo3(Double v) {
        return v == null ? "" : DF3_UPTO.get().format(v);
    }

    /**
     * e.g., 2 → "2.00", 2.5 → "2.50"
     */
    public static String fixed2(Double v) {
        return v == null ? "" : DF2_FIXED.get().format(v);
    }

    /**
     * e.g., 2 → "2.000", 2.5 → "2.500"
     */
    public static String fixed3(Double v) {
        return v == null ? "" : DF3_FIXED.get().format(v);
    }
}
