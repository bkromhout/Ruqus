package com.bkromhout.ruqus;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.StringRes;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.bkromhout.rqv.R;

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
        this(context, null, 0);
    }

    public RQVCard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RQVCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Initialize our view.
     * @param context Context to use.
     * @param attrs   Attributes.
     */
    private void init(Context context, AttributeSet attrs) {
        // Inflate and bind views.
        inflate(context, R.layout.rqv_card, this);
        outlineView = (FrameLayout) findViewById(R.id.outline);
        outlineTextView = (TextView) findViewById(R.id.outline_text);
        cardView = (CardView) findViewById(R.id.card);
        cardTextView = (TextView) findViewById(R.id.card_text);

        // Read attributes.
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RQVCard);
        // Get card mode, default to outline.
        mode = typedArray.getResourceId(R.styleable.RQVCard_rqv_card_mode, 0) == 0 ? Mode.OUTLINE : Mode.CARD;
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
}
