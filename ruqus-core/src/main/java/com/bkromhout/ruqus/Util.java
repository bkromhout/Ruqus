package com.bkromhout.ruqus;

import android.graphics.PorterDuff;
import android.os.Build;
import android.view.View;
import android.widget.ImageButton;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Static utility methods which don't belong in {@link Ruqus}.
 */
class Util {
    /**
     * For generating unique view IDs.
     */
    private static final AtomicInteger nextGeneratedId = new AtomicInteger(1);
    /**
     * Formatting for date fields.
     */
    static final DateFormat dateFormat = SimpleDateFormat.getDateInstance();

    static Calendar calFromString(String dateString) {
        Calendar c = Calendar.getInstance();
        if (dateString == null || dateString.isEmpty()) return c;
        try {
            c.setTime(dateFormat.parse(dateString));
            return c;
        } catch (ParseException e) {
            return c;
        }
    }

    static String stringFromDateInts(int year, int monthOfYear, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(year, monthOfYear, dayOfMonth);
        return dateFormat.format(c.getTime());
    }

    /**
     * Generate a value suitable for use in {@link View#setId(int)}. This value will not collide with ID values
     * generated at build time by aapt for R.id.
     * <p/>
     * This is literally just a copy of API >= 17's {@link View#generateViewId()}.
     * @return a generated ID value
     */
    private static int generateViewId() {
        for (; ; ) {
            final int result = nextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (nextGeneratedId.compareAndSet(result, newValue)) return result;
        }
    }

    /**
     * Get a unique ID to use for a view. If API level is >= 17, will use {@link View#generateViewId()}, otherwise will
     * use {@link #generateViewId()}.
     * @return Unique view ID.
     */
    static int getUniqueViewId() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) return generateViewId();
        else return View.generateViewId();
    }

    /**
     * Tints the color of an image button's icon based on the given {@link RuqusTheme}.
     * @param imageButton ImageButton whose icon should be tinted.
     * @param theme       RuqusTheme.
     */
    static void tintImageButtonIcon(ImageButton imageButton, RuqusTheme theme) {
        imageButton.getDrawable().setColorFilter(
                theme == RuqusTheme.LIGHT ? Ruqus.LIGHT_TEXT_COLOR : Ruqus.DARK_TEXT_COLOR, PorterDuff.Mode.SRC_IN);
    }
}
