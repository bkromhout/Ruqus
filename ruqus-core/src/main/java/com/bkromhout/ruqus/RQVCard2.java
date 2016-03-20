package com.bkromhout.ruqus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Similar to {@link RQVCard}, but has two text views in outline mode.
 */
public class RQVCard2 extends FrameLayout {
    /**
     * View modes. Can either be the card outline or the actual card.
     */
    public enum Mode {
        OUTLINES, CARD
    }

    private LinearLayout outlinesView;
    private TextView outline1TextView;
    private TextView outline2TextView;
    private CardView cardView;
    private TextView cardTextView;

    /**
     * Current mode.
     */
    private Mode mode;

    public RQVCard2(Context context) {
        this(context, null, 0, RuqusTheme.LIGHT);
    }

    public RQVCard2(Context context, RuqusTheme theme) {
        this(context, null, 0, theme);
    }

    public RQVCard2(Context context, AttributeSet attrs) {
        this(context, attrs, 0, RuqusTheme.LIGHT);
    }

    public RQVCard2(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, RuqusTheme.LIGHT);
    }

    public RQVCard2(Context context, AttributeSet attrs, int defStyleAttr, RuqusTheme theme) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, theme);
    }

    /**
     * Initialize our view.
     * @param context Context to use.
     * @param attrs   Attributes.
     */
    private void init(Context context, AttributeSet attrs, RuqusTheme theme) {
        // Inflate and bind views.
        inflate(context, R.layout.rqv_card2, this);
        outlinesView = (LinearLayout) findViewById(R.id.outlines);
        outline1TextView = (TextView) findViewById(R.id.outline1_text);
        outline2TextView = (TextView) findViewById(R.id.outline2_text);
        cardView = (CardView) findViewById(R.id.card);
        cardTextView = (TextView) findViewById(R.id.card_text);
        setTheme(theme);

        // Read attributes.
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RQVCard2);
        // Get card mode, default to outline.
        mode = typedArray.getResourceId(R.styleable.RQVCard2_rqv_card2_mode, 0) == 0 ? Mode.OUTLINES : Mode.CARD;
        // Set outlines text.
        outline1TextView.setText(typedArray.getString(R.styleable.RQVCard2_rqv_outline1_text));
        outline2TextView.setText(typedArray.getString(R.styleable.RQVCard2_rqv_outline2_text));
        // Set card text.
        cardTextView.setText(typedArray.getString(R.styleable.RQVCard2_rqv_card2_text));

        typedArray.recycle();
    }

    /**
     * Change the view based on the mode.
     */
    private void updateMode() {
        // Change visibility outline/card views.
        outlinesView.setVisibility(mode == Mode.OUTLINES ? VISIBLE : GONE);
        cardView.setVisibility(mode == Mode.CARD ? VISIBLE : GONE);
    }

    /**
     * Change the way the card looks based on the given {@link RuqusTheme} value.
     * @param theme Theme.
     */
    void setTheme(RuqusTheme theme) {
        // Set card background color.
        cardView.setCardBackgroundColor(theme == RuqusTheme.LIGHT ? R.color.cardview_light_background :
                R.color.cardview_dark_background);

        // Set text view text color.
        int textColor;
        if (Build.VERSION.SDK_INT < 23) //noinspection deprecation
            textColor = getContext().getResources().getColor(theme == RuqusTheme.LIGHT ? R.color.textColorPrimaryDark :
                    R.color.textColorPrimaryLight);
        else textColor = getContext().getColor(theme == RuqusTheme.LIGHT ? R.color.textColorPrimaryDark :
                R.color.textColorPrimaryLight);
        cardTextView.setTextColor(textColor);
    }

    /**
     * Get the mode of the view.
     * @return {@link Mode}.
     */
    Mode getMode() {
        return mode;
    }

    /**
     * Set the mode of the view.
     * @param mode {@link Mode}.
     */
    void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * Set the texts shown in outline mode.
     * @param strRes1 String resource ID.
     * @param strRes2 String resource ID.
     */
    void setOutlineText(@StringRes int strRes1, @StringRes int strRes2) {
        outline1TextView.setText(strRes1);
        outline2TextView.setText(strRes2);
    }

    /**
     * Set the texts shown in outline mode.
     * @param str1 String.
     * @param str2 String.
     */
    void setOutlineText(String str1, String str2) {
        outline1TextView.setText(str1);
        outline2TextView.setText(str2);
    }

    /**
     * Set the OnClickListener for the first outline text view.
     * @param listener OnClickListener.
     */
    void setOutline1ClickListener(@Nullable OnClickListener listener) {
        outline1TextView.setOnClickListener(listener);
    }

    /**
     * Set the OnClickListener for the second outline text view.
     * @param listener OnClickListener.
     */
    void setOutline2ClickListener(@Nullable OnClickListener listener) {
        outline2TextView.setOnClickListener(listener);
    }

    /**
     * Set the text shown in card mode.
     * @param strRes String resource ID.
     */
    void setCardText(@StringRes int strRes) {
        cardTextView.setText(strRes);
    }

    /**
     * Set the text shown in card mode.
     * @param str String.
     */
    void setCardText(String str) {
        cardTextView.setText(str);
    }

    /**
     * Set the OnClickListener for the card view.
     * @param listener OnClickListener.
     */
    void setCardClickListener(@Nullable OnClickListener listener) {
        cardView.setOnClickListener(listener);
    }
}
