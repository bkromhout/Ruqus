package com.bkromhout.ruqus;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Custom Ruqus CardView.
 */
class RQVCard extends FrameLayout {
    /**
     * View modes. Can either be the card outline or the actual card.
     */
    public enum Mode {
        OUTLINE, CARD
    }

    private FrameLayout outlineView;
    private TextView outlineTextView;
    private CardView cardView;
    private TextView cardTextView;

    /**
     * Current mode.
     */
    private Mode mode;

    public RQVCard(Context context) {
        this(context, null, 0, RuqusTheme.LIGHT);
    }

    public RQVCard(Context context, AttributeSet attrs) {
        this(context, attrs, 0, RuqusTheme.LIGHT);
    }

    public RQVCard(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, RuqusTheme.LIGHT);
    }

    private RQVCard(Context context, AttributeSet attrs, int defStyleAttr, RuqusTheme theme) {
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
        inflate(context, R.layout.rqv_card, this);
        outlineView = (FrameLayout) findViewById(R.id.outline);
        outlineTextView = (TextView) findViewById(R.id.outline_text);
        cardView = (CardView) findViewById(R.id.card);
        cardTextView = (TextView) findViewById(R.id.card_text);
        setTheme(theme);

        // Read attributes.
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RQVCard);
        // Get card mode, default to outline.
        mode = typedArray.getInt(R.styleable.RQVCard_rqv_card_mode, 0) == 0 ? Mode.OUTLINE : Mode.CARD;
        // Set outline text.
        outlineTextView.setText(typedArray.getString(R.styleable.RQVCard_rqv_outline_text));
        // Set card text.
        cardTextView.setText(typedArray.getString(R.styleable.RQVCard_rqv_card_text));

        typedArray.recycle();
    }

    /**
     * Change the view based on the mode.
     */
    private void updateMode() {
        // Change visibility outline/card views.
        outlineView.setVisibility(mode == Mode.OUTLINE ? VISIBLE : GONE);
        cardView.setVisibility(mode == Mode.CARD ? VISIBLE : GONE);
    }

    /**
     * Change the way the card looks based on the given {@link RuqusTheme} value.
     * @param theme Theme.
     */
    void setTheme(RuqusTheme theme) {
        // Set card background color.
        cardView.setCardBackgroundColor(theme == RuqusTheme.LIGHT ? Ruqus.LIGHT_CARD_COLOR : Ruqus.DARK_CARD_COLOR);

        // Set text view text color.
        cardTextView.setTextColor(theme == RuqusTheme.LIGHT ? Ruqus.DARK_TEXT_COLOR : Ruqus.LIGHT_TEXT_COLOR);
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
        updateMode();
    }

    /**
     * Set the text shown in outline mode.
     * @param strRes String resource ID.
     */
    void setOutlineText(@StringRes int strRes) {
        outlineTextView.setText(strRes);
    }

    /**
     * Set the text shown in outline mode.
     * @param str String.
     */
    void setOutlineText(String str) {
        outlineTextView.setText(str);
    }

    /**
     * Set the OnClickListener for the outline text view.
     * @param listener OnClickListener.
     */
    void setOutlineClickListener(@Nullable OnClickListener listener) {
        outlineTextView.setOnClickListener(listener);
    }

    /**
     * Set the text shown in card mode.
     * @param strRes String resource ID.
     */
    void setCardText(@StringRes int strRes) {
        cardTextView.setText(strRes);
    }

    /**
     * Get the text being shown on the card.
     * @return Card text
     */
    String getCardText() {
        return cardTextView.getText().toString();
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

    @Override
    public void setTag(Object tag) {
        super.setTag(tag);
        // Also set the tags on the other views this holds that can be assigned click listeners.
        cardView.setTag(tag);
        outlineTextView.setTag(tag);
    }

    @Override
    public void setTag(int key, Object tag) {
        super.setTag(key, tag);
        // Also set the tags on the other views this holds that can be assigned click listeners.
        cardView.setTag(key, tag);
        outlineTextView.setTag(key, tag);
    }
}
