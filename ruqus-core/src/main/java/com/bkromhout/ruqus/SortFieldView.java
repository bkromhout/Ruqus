package com.bkromhout.ruqus;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import io.realm.Sort;

/**
 * View which allows choosing sort fields. Contains a TextView label, a Spinner, and a remove Button.
 */
public class SortFieldView extends LinearLayout {
    private TextView label;
    private Spinner sortFieldChooser;
    private ImageButton removeButton;
    private RadioGroup sortDirRg;
    private RadioButton ascRb;
    private RadioButton descRb;

    /**
     * Name of the Queryable class that the owning {@link RealmQueryView} is currently set up for.
     */
    private String currClassName;
    /**
     * Current theme.
     */
    private RuqusTheme theme;

    public SortFieldView(Context context) {
        this(context, null, 0, RuqusTheme.LIGHT, null);
    }

    public SortFieldView(Context context, RuqusTheme theme, String currClassName) {
        this(context, null, 0, theme, currClassName);
    }

    public SortFieldView(Context context, AttributeSet attrs) {
        this(context, attrs, 0, RuqusTheme.LIGHT, null);
    }

    public SortFieldView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, RuqusTheme.LIGHT, null);
    }

    public SortFieldView(Context context, AttributeSet attrs, int defStyleAttr, RuqusTheme theme,
                         String currClassName) {
        super(context, attrs, defStyleAttr);
        init(context, theme, currClassName);
    }

    /**
     * Initialize our view.
     * @param context Context to use.
     */
    private void init(Context context, RuqusTheme theme, String currClassName) {
        this.currClassName = currClassName;
        // Inflate and bind views.
        inflate(context, R.layout.sort_field_view, this);
        setOrientation(VERTICAL);

        label = (TextView) findViewById(R.id.sort_field_label);
        sortFieldChooser = (Spinner) findViewById(R.id.sort_field);
        removeButton = (ImageButton) findViewById(R.id.remove_field);
        sortDirRg = (RadioGroup) findViewById(R.id.rg_sort_dir);
        ascRb = (RadioButton) findViewById(R.id.asc);
        descRb = (RadioButton) findViewById(R.id.desc);

        setTheme(theme);

        // Set up spinner listeners.
        sortFieldChooser.setOnTouchListener(sortFieldChooserListener);
        sortFieldChooser.setOnItemSelectedListener(sortFieldChooserListener);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        // Allow parent classes to save state.
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        // Save our state.
        ss.currClassName = this.currClassName;
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
        this.currClassName = ss.currClassName;
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
        Util.tintImageButtonIcon(removeButton, theme);
    }

    private SpinnerInteractionListener sortFieldChooserListener = new SpinnerInteractionListener() {
        boolean isTouching = false;

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (isTouching) {
                // Make sure the user didn't select nothing.
                String selStr = (String) parent.getItemAtPosition(position);
                if (Ruqus.CHOOSE_FIELD.equals(selStr)) sortDirRg.setVisibility(GONE);
                else setSortDirOptions(selStr);
                isTouching = false;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            if (isTouching) {
                sortDirRg.setVisibility(GONE);
                isTouching = false;
            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            isTouching = true;
            return false;
        }
    };

    /**
     * Called when the selection on a sort field spinner is changed.
     */
    private void setSortDirOptions(String visibleFieldName) {
        String[] pretty = Ruqus.typeEnumForField(currClassName, Ruqus.fieldFromVisibleField(
                currClassName, visibleFieldName)).getPrettySortStrings();
        ascRb.setText(pretty[0]);
        descRb.setText(pretty[1]);
        sortDirRg.setVisibility(VISIBLE);
    }

    String getVisField() {
        return (String) sortFieldChooser.getSelectedItem();
    }

    /**
     * Gets real name of selected field. If selected item is {@link Ruqus#CHOOSE_FIELD}, just returns that.
     * @return Real name of selected field, or {@link Ruqus#CHOOSE_FIELD}.
     */
    String getRealField() {
        String vis = getVisField();
        return Ruqus.CHOOSE_FIELD.equals(vis) ? vis : Ruqus.fieldFromVisibleField(currClassName, getVisField());
    }

    Sort getSortDir() {
        return sortDirRg.getCheckedRadioButtonId() == R.id.asc ? Sort.ASCENDING : Sort.DESCENDING;
    }

    void setLabelText(String string) {
        label.setText(string);
    }

    void setRemoveBtnClickListener(OnClickListener listener) {
        removeButton.setOnClickListener(listener);
    }

    void setSpinnerAdapter(SpinnerAdapter adapter) {
        sortFieldChooser.setAdapter(adapter);
    }

    void setSelectedPos(int posToSelect) {
        if (posToSelect == -1) return;
        sortFieldChooser.setSelection(posToSelect);
        // Manually update radio buttons' text if need be.
        String selStr = (String) sortFieldChooser.getItemAtPosition(posToSelect);
        if (Ruqus.CHOOSE_FIELD.equals(selStr)) sortDirRg.setVisibility(GONE);
        else setSortDirOptions(selStr);
    }

    void setSortDir(Sort sortDir) {
        if (sortDir == null) return;
        sortDirRg.check(sortDir == Sort.ASCENDING ? R.id.asc : R.id.desc);
    }

    /**
     * Helps us easily save and restore our view's state.
     */
    static class SavedState extends BaseSavedState {
        String currClassName;
        RuqusTheme theme;
        SparseArray childStates;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in, ClassLoader loader) {
            super(in);
            this.currClassName = in.readString();
            int tmpTheme = in.readInt();
            this.theme = tmpTheme == -1 ? null : RuqusTheme.values()[tmpTheme];
            this.childStates = in.readSparseArray(loader);
        }

        @Override
        public int describeContents() { return 0; }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.currClassName);
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
