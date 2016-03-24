package com.bkromhout.ruqus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
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
    /**
     * Current theme.
     */
    private RuqusTheme theme;

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

    private RQVCard2(Context context, AttributeSet attrs, int defStyleAttr, RuqusTheme theme) {
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
        mode = typedArray.getInt(R.styleable.RQVCard2_rqv_card2_mode, 0) == 0 ? Mode.OUTLINES : Mode.CARD;
        // Set outlines text.
        outline1TextView.setText(typedArray.getString(R.styleable.RQVCard2_rqv_outline1_text));
        outline2TextView.setText(typedArray.getString(R.styleable.RQVCard2_rqv_outline2_text));
        // Set card text.
        cardTextView.setText(typedArray.getString(R.styleable.RQVCard2_rqv_card2_text));

        typedArray.recycle();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        // Allow parent classes to save state.
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        // Save our state.
        ss.mode = this.mode;
        ss.theme = this.theme;

        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        //Allow parent classes to restore state.
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        // Restore our state.
        setTheme(ss.theme);
        setMode(ss.mode);
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
        this.theme = theme;
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

    /**
     * Set the OnLongClickListener for the card view.
     * @param listener OnLongClickListener.
     */
    void setCardLongClickListener(@Nullable OnLongClickListener listener) {
        cardView.setOnLongClickListener(listener);
    }

    @Override
    public void setTag(Object tag) {
        super.setTag(tag);
        // Also set the tags on the other views this holds that can be assigned click listeners.
        cardView.setTag(tag);
        outline1TextView.setTag(tag);
        outline2TextView.setTag(tag);
    }

    @Override
    public void setTag(int key, Object tag) {
        super.setTag(key, tag);
        // Also set the tags on the other views this holds that can be assigned click listeners.
        cardView.setTag(key, tag);
        outline1TextView.setTag(key, tag);
        outline2TextView.setTag(key, tag);
    }

    static class SavedState extends BaseSavedState {
        Mode mode;
        RuqusTheme theme;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            int tmpMode = in.readInt();
            this.mode = tmpMode == -1 ? null : Mode.values()[tmpMode];
            int tmpTheme = in.readInt();
            this.theme = tmpTheme == -1 ? null : RuqusTheme.values()[tmpTheme];
        }

        @Override
        public int describeContents() { return 0; }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mode == null ? -1 : this.mode.ordinal());
            dest.writeInt(this.theme == null ? -1 : this.theme.ordinal());
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {return new SavedState(in);}

            @Override
            public SavedState[] newArray(int size) {return new SavedState[size];}
        };
    }
}
