package com.stanissudo.jycs_crafters.utils;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Pattern;

/**
 *  * @author Stan Permiakov
 *  * created: 8/12/2025
 *  * @project JYCS-Crafters
 *  * file: VehicleActivity.java
 *  *
 */
public class DecimalDigitsInputFilter implements InputFilter {
    private final Pattern pattern;
    private final int maxBefore;
    private final int maxAfter;

    public DecimalDigitsInputFilter(int maxBeforeDecimal, int maxAfterDecimal) {
        this.maxBefore = maxBeforeDecimal;
        this.maxAfter  = maxAfterDecimal;
        // Only '.' as decimal separator (matches your parsing). Example: ^\d{0,5}(\.\d{0,3})?$
        this.pattern = Pattern.compile("^\\d{0," + maxBefore + "}(?:\\.\\d{0," + maxAfter + "})?$");
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {

        String newString =
                dest.subSequence(0, dstart).toString() +
                        source.subSequence(start, end).toString() +
                        dest.subSequence(dend, dest.length()).toString();

        // Always allow deletions
        if (newString.isEmpty()) return null;

        // If first char is '.', auto-prefix 0.
        if (dest.length() == 0 && ".".contentEquals(source)) return "0.";

        // Enforce limits
        return pattern.matcher(newString).matches() ? null : "";
    }
}
