package com.dm.notes.helpers;

import android.text.Html;
import android.text.SpannableString;

public class Utils {
    public static SpannableString fromHtml(String text) {
        SpannableString spannableString = SpannableString.valueOf(Html.fromHtml(text));
        if (spannableString.toString().endsWith("\n\n"))
            spannableString = (SpannableString) spannableString.subSequence(0, spannableString.length() - 2);
        return spannableString;
    }
}
