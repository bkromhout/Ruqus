package com.bkromhout.ruqus;

import android.app.Activity;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;
import java.util.Date;

/**
 * Simple view which has a button and a text view and allows for picking the date.
 */
class DateInputView extends LinearLayout implements DatePickerDialog.OnDateSetListener {
    private TextView tvDate;

    /**
     * Current theme.
     */
    private RuqusTheme theme;

    public DateInputView(Context context) {
        this(context, null, 0, RuqusTheme.LIGHT);
    }

    public DateInputView(Context context, RuqusTheme theme) {
        this(context, null, 0, theme);
    }

    public DateInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0, RuqusTheme.LIGHT);
    }

    public DateInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, RuqusTheme.LIGHT);
    }

    public DateInputView(Context context, AttributeSet attrs, int defStyleAttr, RuqusTheme theme) {
        super(context, attrs, defStyleAttr);
        init(context, theme);
    }

    /**
     * Initialize our view.
     * @param context Context to use.
     */
    private void init(Context context, RuqusTheme theme) {
        // Inflate and bind views.
        inflate(context, R.layout.date_input_view, this);
        setOrientation(HORIZONTAL);

        tvDate = (TextView) findViewById(R.id.tv_date);
        findViewById(R.id.choose_date).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        setTheme(theme);
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        // Allow parent classes to save state.
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        // Save our state.
        ss.theme = this.theme;
        ss.childStates = new SparseArray<>();
        for (int i = 0; i < getChildCount(); i++) //noinspection unchecked
            getChildAt(i).saveHierarchyState(ss.childStates);

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
        for (int i = 0; i < getChildCount(); i++) //noinspection unchecked
            getChildAt(i).restoreHierarchyState(ss.childStates);
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }


    /**
     * Change the way the view looks based on the given {@link RuqusTheme} value.
     * @param theme Theme.
     */
    void setTheme(RuqusTheme theme) {
        this.theme = theme;
        Util.tintImageButtonIcon((ImageButton) findViewById(R.id.choose_date), theme);
    }

    /**
     * Show the date picker dialog to show for date inputs.
     */
    private void showDatePickerDialog() {
        Calendar c = Util.calFromString(tvDate.getText().toString());
        DatePickerDialog dpd = DatePickerDialog.newInstance(this, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));
        dpd.setThemeDark(theme == RuqusTheme.DARK);
        dpd.autoDismiss(true);
        dpd.show(((Activity) getContext()).getFragmentManager(), "RuqusDPD");
    }

    /**
     * Called when a date was picked in the date picker dialog.
     */
    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        tvDate.setText(Util.stringFromDateInts(year, monthOfYear, dayOfMonth));
    }

    boolean hasDate() {
        return tvDate.length() != 0;
    }

    Date getDate() {
        return tvDate.length() == 0 ? null : Util.calFromString(tvDate.getText().toString()).getTime();
    }

    void setDate(Date date) {
        tvDate.setText(Util.dateFormat.format(date));
    }

    String getText() {
        return tvDate.getText().toString();
    }

    void setText(String text) {
        tvDate.setText(text);
    }

    void setError(String error) {
        tvDate.setError(error);
    }

    /**
     * Helps us easily save and restore our view's state.
     */
    static class SavedState extends BaseSavedState {
        RuqusTheme theme;
        SparseArray childStates;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in, ClassLoader loader) {
            super(in);
            int tmpTheme = in.readInt();
            this.theme = tmpTheme == -1 ? null : RuqusTheme.values()[tmpTheme];
            this.childStates = in.readSparseArray(loader);
        }

        @Override
        public int describeContents() { return 0; }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.theme == null ? -1 : this.theme.ordinal());
            //noinspection unchecked
            dest.writeSparseArray(childStates);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = ParcelableCompat
                .newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                        return new SavedState(in, loader);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                });
    }
}
