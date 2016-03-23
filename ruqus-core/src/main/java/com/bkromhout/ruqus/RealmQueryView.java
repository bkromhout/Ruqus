package com.bkromhout.ruqus;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import io.realm.Sort;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * RealmQueryView
 * @author bkromhout
 */
public class RealmQueryView extends FrameLayout {
    private enum Mode {
        MAIN, C_BUILD, S_BUILD
    }

    // Views.
    private RelativeLayout mainCont;
    private RQVCard queryableChooser;
    private ScrollView scrollView;
    private LinearLayout conditionsCont;
    private RQVCard sortChooser;
    private LinearLayout builderCont;

    // View holders.
    private ConditionBuilderHelper conditionBuilder;
    private SortBuilderHelper sortBuilder;

    /**
     * Current theme type.
     */
    private RuqusTheme theme = null;
    /**
     * Current user query.
     */
    private RealmUserQuery ruq;
    /**
     * Current mode.
     */
    private Mode mode;
    /**
     * Simple name of the current {@link Queryable} class.
     */
    private String currClassName;
    /**
     * List of current visible flat field names.
     */
    private ArrayList<String> currVisibleFlatFieldNames;
    /**
     * Index of the query part currently being worked on.
     */
    private int currIdx;

    public RealmQueryView(Context context) {
        this(context, null, null, null);
    }

    public RealmQueryView(Context context, RealmUserQuery ruq) {
        this(context, null, ruq, null);
    }

    public RealmQueryView(Context context, RuqusTheme theme) {
        this(context, null, null, theme);
    }

    public RealmQueryView(Context context, RealmUserQuery ruq, RuqusTheme theme) {
        this(context, null, ruq, theme);
    }

    public RealmQueryView(Context context, AttributeSet attrs) {
        this(context, attrs, null, null);
    }

    public RealmQueryView(Context context, AttributeSet attrs, RealmUserQuery ruq, RuqusTheme theme) {
        super(context, attrs);
        this.ruq = ruq;
        this.theme = theme;
        init(context, attrs);
    }

    /**
     * Initialize our view.
     * @param context Context to use.
     * @param attrs   Attributes.
     */
    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.realm_query_view, this);

        // Bind views and read attributes.
        mainCont = (RelativeLayout) findViewById(R.id.mainCont);
        queryableChooser = (RQVCard) findViewById(R.id.queryable_type);
        scrollView = (ScrollView) findViewById(R.id.rqv_scroll_view);
        conditionsCont = (LinearLayout) findViewById(R.id.rqv_content);
        sortChooser = (RQVCard) findViewById(R.id.sort_type);
        builderCont = (LinearLayout) findViewById(R.id.builderCont);
        initAttrs(context, attrs);

        // Initialize UI.
        mode = Mode.MAIN;
        initUi();

        // Create a new RealmUserQuery if we weren't given one.
        if (ruq == null) ruq = new RealmUserQuery();
    }

    /**
     * Initializes the view using the given attributes.
     * @param context Context to use.
     * @param attrs   Attributes.
     */
    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RealmQueryView);

        if (theme == null) {
            // Get theme, default to light.
            theme = typedArray.getResourceId(R.styleable.RealmQueryView_ruqus_theme, 0) == 0 ? RuqusTheme.LIGHT :
                    RuqusTheme.DARK;
        }

        typedArray.recycle();
    }

    /**
     * Initialize the UI.
     */
    private void initUi() {
        setTheme(theme);
        // Set click handlers for queryable and sort choosers.
        queryableChooser.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onQueryableChooserClicked();
            }
        });
        sortChooser.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onSortChooserClicked();
            }
        });

        if (ruq == null) {
            // If we don't have a realm user query already, setup is very minimal, we just disable the scrollview and
            // sort choosers.
            setConditionsAndSortEnabled(false);
            return;
        }

        // If we have a RUQ already, we need to draw our view accordingly.
        setupViewUsingRUQ();
    }

    /**
     * Draw the view correctly based on the current RUQ as long as it is present and valid.
     */
    private void setupViewUsingRUQ() {
        if (ruq == null || !ruq.isQueryValid()) return;
        // TODO
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        // Allow parent classes to save state.
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        // TODO Save our state.
        // TODO theme, mode, currIdx, ruq string
        // TODO add holder to builderCont tag??

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

        // TODO Restore our state.
    }

    private void switchMode(Mode mode) {
        if ((this.mode == Mode.C_BUILD || this.mode == Mode.S_BUILD) && mode == Mode.MAIN) {
            // If we're switching back to main mode, remove holders and all views from the builder container.
            conditionBuilder = null;
            sortBuilder = null;
            builderCont.removeAllViews();
        }
        mainCont.setVisibility(GONE);
        builderCont.setVisibility(GONE);
        // Switch mode and UI.
        switch (mode) {
            case MAIN:
                mainCont.setVisibility(VISIBLE);
                currIdx = -1;
                break;
            case C_BUILD:
                if (currIdx == -1)
                    throw new IllegalArgumentException("Must set currIdx to switch to the condition builder.");
                initConditionBuilder(currIdx >= conditionsCont.getChildCount() - 1 ? null
                        : ruq.getConditions().get(currIdx));
                builderCont.setVisibility(VISIBLE);
                break;
            case S_BUILD:
                initSortBuilder(ruq.getSortFields(), ruq.getSortDirs());
                builderCont.setVisibility(VISIBLE);
                break;
        }
    }

    public void setTheme(RuqusTheme theme) {
        this.theme = theme;

        // Set theme on queryable and sort choosers.
        queryableChooser.setTheme(theme);
        sortChooser.setTheme(theme);

        // Set theme on all condition cards.
        for (int i = 0; i < conditionsCont.getChildCount(); i++)
            ((RQVCard2) conditionsCont.getChildAt(i)).setTheme(theme);
    }

    private void setConditionsAndSortEnabled(boolean enabled) {
        scrollView.setEnabled(enabled);
        sortChooser.setEnabled(enabled);
    }

    /**
     * Creates an {@link RQVCard2} and sets it to outlines mode with the texts "Add Operator" and "Add Condition", then
     * adds it to the end of {@link #conditionsCont}.
     */
    private void appendAddView() {
        // Only add the view if we have the same number of views and conditions currently (indicates each view is
        // tied to a condition.
        if (ruq != null && conditionsCont.getChildCount() == ruq.conditionCount()) {
            RQVCard2 add = new RQVCard2(getContext(), theme);
            add.setMode(RQVCard2.Mode.OUTLINES);
            add.setOutlineText(R.string.add_operator_nl, R.string.add_condition_nl);
            // Set tag to the current child count of the conditions container, since that will be this item's index
            // once it is added to the end of it.
            add.setTag(R.id.index, conditionsCont.getChildCount());
            // Set the outline text views' OnClickListeners.
            add.setOutline1ClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onOperatorClicked((Integer) v.getTag(R.id.index), null);
                }
            });
            add.setOutline2ClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onConditionClicked((Integer) v.getTag(R.id.index));
                }
            });
            // Add to the conditions container.
            conditionsCont.addView(add);
        }
    }

    /**
     * Sets up the builder container to start building/modifying a condition.
     * @param condition The condition to modify, or null if we wish to build a new condition.
     */
    private void initConditionBuilder(Condition condition) {
        conditionBuilder = new ConditionBuilderHelper(condition);
    }

    /**
     * Sets up the builder container to add/change/remove sort fields.
     * @param sortFields Current sort field.
     * @param sortDirs   Current sort directions.
     */
    private void initSortBuilder(ArrayList<String> sortFields, ArrayList<Sort> sortDirs) {
        sortBuilder = new SortBuilderHelper(sortFields, sortDirs);
    }

    /**
     * Clears the view back to its initial state and sets {@link #ruq} to a new instance of {@link RealmUserQuery}.
     */
    private void reset() {
        // New RUQ.
        ruq = new RealmUserQuery();
        currClassName = null;
        currVisibleFlatFieldNames = null;
        currIdx = -1;
        // Reset choosers back to outline mode.
        queryableChooser.setMode(RQVCard.Mode.OUTLINE);
        sortChooser.setMode(RQVCard.Mode.OUTLINE);
        // Clear all children from condition container.
        conditionsCont.removeAllViews();
        // Disable conditions container and sort chooser.
        setConditionsAndSortEnabled(false);
    }

    /**
     * Show a dialog with the visible names of all classes annotated with {@link Queryable}.
     */
    private void onQueryableChooserClicked() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.choose_queryable_title)
                .items(Ruqus.getClassData().getVisibleNames(true))
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        String realName = Ruqus.classNameFromVisibleName(text.toString());
                        setQueryable(realName, text.toString());
                        ruq.setQueryClass(realName);
                    }
                })
                .show();
    }

    /**
     * Called when an {@link RQVCard2}'s outline text view which reads "Add Operator", or a card which has been filled
     * in with a real operator, is clicked. Shows a dialog of visible names of all no-args transformers (AKA,
     * "Operators").
     * @param index   Index of the card in the conditions container.
     * @param currVal The text which is currently on the card, or null if the card is in outline mode.
     */
    private void onOperatorClicked(final int index, final String currVal) {
        new MaterialDialog.Builder(getContext())
                .title(index == conditionsCont.getChildCount() - 1 ? R.string.add_operator : R.string.change_operator)
                .items(Ruqus.getTransformerData().getVisibleNoArgNames())
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        if (currVal == null || !currVal.equals(text.toString())) setOperator(index, text.toString());
                    }
                })
                .show();
    }

    /**
     * Called when an {@link RQVCard2}'s outline text view which reads "Add Condition", or when a card which has been
     * filled in with a real condition, is clicked. Switches to condition builder mode.
     * @param index Index of the card in the conditions container.
     */
    private void onConditionClicked(final int index) {
        this.currIdx = index;
        switchMode(Mode.C_BUILD);
    }

    /**
     * Called when the sort mode chooser is clicked. Switches to sort builder mode.
     */
    private void onSortChooserClicked() {
        switchMode(Mode.S_BUILD);
    }

    /**
     * Show a dialog asking if we want to delete the long-clicked card.
     * @param index Index of the card in the conditions container.
     */
    private void onPartLongClicked(final int index) {
        new MaterialDialog.Builder(getContext())
                .title(R.string.remove_operator)
                .negativeText(R.string.no)
                .positiveText(R.string.yes)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // Remove from RUQ.
                        ruq.getConditions().remove(index);
                        // Remove from conditions container.
                        conditionsCont.removeViewAt(index);
                    }
                })
                .show();
    }

    /**
     * Called when the queryable class has been set. Only affects the view, not {@link #ruq}.
     * @param visibleName Visible name of the queryable class.
     */
    private void setQueryable(String realName, String visibleName) {
        // Reset.
        reset();
        // Set instance vars.
        currClassName = realName;
        currVisibleFlatFieldNames = Ruqus.visibleFlatFieldsForClass(currClassName);
        currVisibleFlatFieldNames.add(0, Ruqus.CHOOSE_FIELD);
        // Set queryable chooser's card text and mode.
        queryableChooser.setCardText(visibleName);
        queryableChooser.setMode(RQVCard.Mode.CARD);
        // Append an add view to the conditions container.
        appendAddView();
        // Enable the conditions container and sort chooser.
        setConditionsAndSortEnabled(true);
    }

    /**
     * Called when a card has been set as an operator card.
     * @param index       Index of the card in the conditions container.
     * @param visibleName String to put on the card.
     */
    private void setOperator(int index, String visibleName) {
        String realName = Ruqus.transformerNameFromVisibleName(visibleName, true);
        RQVCard2 card = (RQVCard2) conditionsCont.getChildAt(index);
        card.setTag(R.id.curr_val, visibleName);
        if (index == conditionsCont.getChildCount() - 1) {
            // This was an outline-mode card before this, and ruq doesn't have a condition for it.
            // Set the card's card listener and long click listener.
            card.setCardClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onOperatorClicked((Integer) v.getTag(R.id.index), (String) v.getTag(R.id.curr_val));
                }
            });
            card.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onPartLongClicked((Integer) v.getTag(R.id.index));
                    return true;
                }
            });
            // Set the card's text.
            card.setCardText(visibleName);
            // Set the card's mode to CARD.
            card.setMode(RQVCard2.Mode.CARD);

            // Create a new condition; we just need to set the transformer's real name and the realm class's name
            // since it's an no-args condition.
            Condition condition = new Condition();
            condition.setTransformer(realName);
            condition.setRealmClass(currClassName);

            // Add the condition to the query.
            ruq.getConditions().add(condition);

            // Finally, append another add view to the conditions container.
            appendAddView();
        } else {
            // This was a card-mode card already, ruq already had a condition for it.
            // Update card text.
            card.setCardText(visibleName);

            // Update condition.
            Condition condition = ruq.getConditions().get(index);
            condition.setTransformer(realName);
            condition.setRealmClass(currClassName);
        }
    }

    /**
     * Common views for builder mode.
     */
    private class BuilderHelper {
        public TextView header;
        public LinearLayout buttons;
        private Button cancelButton;
        protected Button saveButton;

        BuilderHelper() {
            // Bind common views.
            header = (TextView) View.inflate(getContext(), R.layout.header_text_view, null);
            buttons = (LinearLayout) View.inflate(getContext(), R.layout.cancel_save_buttons, null);
            cancelButton = (Button) buttons.findViewById(R.id.cancel);
            saveButton = (Button) buttons.findViewById(R.id.save);
            // Set up cancel button to simply switch back to MAIN mode.
            cancelButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchMode(Mode.MAIN);
                }
            });
        }
    }

    /**
     * Helps keep track of views while in condition builder mode.
     */
    private class ConditionBuilderHelper extends BuilderHelper implements DatePickerDialog.OnDateSetListener {
        public Spinner fieldChooser;
        public LinearLayout conditionalChooserCont;
        public Spinner conditionalChooser;
        public ArrayList<View> argViews;

        private LinearLayout.LayoutParams lps;
        private DateFormat dateFormat;

        private String currFieldName;
        private FieldType currFieldType;
        private String currTransName;

        private ConditionBuilderHelper(Condition condition) {
            super();
            // Set up layout params.
            lps = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lps.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
                    getResources().getDisplayMetrics());

            // Set up date format.
            dateFormat = SimpleDateFormat.getDateInstance();

            // Set header text.
            header.setText(R.string.edit_condition_title);

            // Create array.
            argViews = new ArrayList<>();

            // Set up field chooser.
            fieldChooser = new Spinner(getContext(), Spinner.MODE_DROPDOWN);
            fieldChooser.setLayoutParams(lps);

            // Set up field chooser spinner.
            ArrayAdapter<String> fieldAdapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_dropdown_item, currVisibleFlatFieldNames);
            fieldChooser.setAdapter(fieldAdapter);
            fieldChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Make sure user didn't select nothing.
                    String selStr = (String) parent.getItemAtPosition(position);
                    if (Ruqus.CHOOSE_FIELD.equals(selStr)) {
                        currFieldName = null;
                        updateConditionalChooser();
                        return;
                    }
                    // It's a real field.
                    String realFieldName = Ruqus.fieldFromVisibleField(currClassName, selStr);
                    if (currFieldName == null || currFieldName.equals(realFieldName)) {
                        // We only care if if was changed.
                        currFieldName = realFieldName;
                        updateConditionalChooser();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    currFieldName = null;
                    updateConditionalChooser();
                }
            });

            // Set up conditional chooser.
            conditionalChooser = new Spinner(getContext(), Spinner.MODE_DROPDOWN);
            conditionalChooser.setLayoutParams(lps);
            conditionalChooser.setVisibility(GONE);

            // Set up conditional chooser spinner.
            conditionalChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Make sure user didn't select nothing.
                    String selStr = (String) parent.getItemAtPosition(position);
                    if (Ruqus.CHOOSE_CONDITIONAL.equals(selStr)) {
                        currTransName = null;
                        updateArgViews();
                        return;
                    }
                    // It's a real transformer.
                    String realTransName = Ruqus.transformerNameFromVisibleName(selStr, false);
                    if (currTransName == null || currTransName.equals(realTransName)) {
                        // We only care if it was changed.
                        currTransName = realTransName;
                        updateArgViews();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    currTransName = null;
                    updateArgViews();
                }
            });

            // Set up save button.
            saveButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveClicked();
                }
            });

            // Add views.
            builderCont.addView(header);
            builderCont.addView(fieldChooser);
            builderCont.addView(conditionalChooser);
            builderCont.addView(buttons);

            // Set up from condition.
            if (condition != null) {
                // Select correct value in field chooser. TODO need to manually set up next part?
                currFieldName = condition.getField();
                fieldChooser.setSelection(currVisibleFlatFieldNames.indexOf(
                        Ruqus.visibleFieldFromField(currClassName, currFieldName)));
                // Select correct transformer. TODO ditto?
                currTransName = condition.getTransformer();
                conditionalChooser.setSelection(Ruqus.getTransformerData().getVisibleNames().indexOf(
                        Ruqus.getTransformerData().visibleNameOf(currTransName)));
                // Fill in argument views.
                fillArgViews(condition.getArgs());
            }
        }

        /**
         * Called when an item in the field chooser spinner is selected.
         */
        private void updateConditionalChooser() {
            // If currFieldName is null, we should tear some things down.
            if (currFieldName == null) {
                currFieldType = null;
                updateArgViews();
                conditionalChooserCont.setVisibility(GONE);
                return;
            }
            // Get field type from field.
            currFieldType = Ruqus.typeEnumForField(currClassName, currFieldName);

            // Get the list of visible names for all transformers which accept the given field type.
            ArrayList<String> conditionals = Ruqus.getTransformerData().getVisibleNames(currFieldType.getClazz());
            conditionals.add(0, Ruqus.CHOOSE_CONDITIONAL);

            // Create an array adapter from it.
            ArrayAdapter<String> conditionalAdapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_dropdown_item, conditionals);

            // Bind the adapter to the spinner.
            conditionalChooser.setAdapter(conditionalAdapter);

            // Make sure conditional chooser is visible.
            conditionalChooser.setVisibility(VISIBLE);
        }

        private void updateArgViews() {
            removeArgViews();
            if (currFieldName == null || currFieldType == null || currTransName == null) {
                currTransName = null;
                return;
            }

            // Add views based on the field type and the number of arguments that the transformer accepts.
            int numArgs = Ruqus.numberOfArgsFor(currTransName);
            for (int i = 0; i < numArgs; i++) {
                switch (currFieldType) {
                    case BOOLEAN:
                        RadioGroup rgFalseTrue = (RadioGroup) View.inflate(getContext(), R.layout.rg_false_true, null);
                        builderCont.addView(rgFalseTrue, builderCont.getChildCount() - 1);
                        argViews.add(rgFalseTrue);
                        break;
                    case DATE:
                        LinearLayout tvDateCont = (LinearLayout) View.inflate(getContext(), R.layout.tv_date, null);
                        // Set up date button to open date picker dialog.
                        final TextView tvDate = (TextView) tvDateCont.findViewById(R.id.tv_date);
                        ImageButton btnChooseDate = (ImageButton) tvDateCont.findViewById(R.id.choose_date);
                        final int finalI = i;
                        btnChooseDate.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Calendar c = calFromString(tvDate.getText().toString());
                                DatePickerDialog dpd = DatePickerDialog.newInstance(ConditionBuilderHelper.this,
                                        c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                                dpd.setThemeDark(theme == RuqusTheme.DARK);
                                Bundle b = new Bundle();
                                b.putInt("IDX", finalI);
                                dpd.setArguments(b);
                                dpd.autoDismiss(true);
                                dpd.show(((Activity) getContext()).getFragmentManager(), "RuqusDPD");
                            }
                        });
                        builderCont.addView(tvDateCont, builderCont.getChildCount() - 1);
                        argViews.add(tvDateCont);
                        break;
                    case DOUBLE:
                    case FLOAT:
                        EditText etDecimal = (EditText) View.inflate(getContext(), R.layout.et_decimal, null);
                        builderCont.addView(etDecimal, builderCont.getChildCount() - 1);
                        argViews.add(etDecimal);
                        break;
                    case INTEGER:
                    case LONG:
                    case SHORT:
                        EditText etWholeNumber = (EditText) View.inflate(getContext(), R.layout.et_whole_number, null);
                        builderCont.addView(etWholeNumber, builderCont.getChildCount() - 1);
                        argViews.add(etWholeNumber);
                        break;
                    case STRING:
                        EditText etString = (EditText) View.inflate(getContext(), R.layout.et_string, null);
                        builderCont.addView(etString, builderCont.getChildCount() - 1);
                        argViews.add(etString);
                        break;
                }
            }
        }

        private void fillArgViews(Object[] args) {
            for (int i = 0; i < argViews.size(); i++) {
                switch (currFieldType) {
                    case BOOLEAN:
                        ((RadioGroup) argViews.get(i)).check((Boolean) args[i] ? R.id.rb_true : R.id.rb_false);
                        break;
                    case DATE:
                        ((TextView) argViews.get(i).findViewById(R.id.tv_date)).setText(dateFormat.format(args[i]));
                        break;
                    case DOUBLE:
                    case FLOAT:
                    case INTEGER:
                    case LONG:
                    case SHORT:
                        ((TextView) argViews.get(i)).setText(String.valueOf(args[i]));
                        break;
                    case STRING:
                        ((TextView) argViews.get(i)).setText((String) args[i]);
                        break;
                }
            }
        }

        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            int index = view.getArguments().getInt("IDX", -1);
            if (index == -1) throw new IllegalArgumentException("Bad index!");
            ((TextView) argViews.get(index).findViewById(R.id.tv_date)).setText(
                    stringFromDateInts(year, monthOfYear, dayOfMonth));
        }

        private Calendar calFromString(String dateString) {
            Calendar c = Calendar.getInstance();
            if (dateString == null || dateString.isEmpty()) return c;
            try {
                c.setTime(dateFormat.parse(dateString));
                return c;
            } catch (ParseException e) {
                return c;
            }
        }

        private String stringFromDateInts(int year, int monthOfYear, int dayOfMonth) {
            Calendar c = Calendar.getInstance();
            c.set(year, monthOfYear, dayOfMonth);
            return dateFormat.format(c.getTime());
        }

        private void removeArgViews() {
            int total = builderCont.getChildCount();
            if (total <= 4) return;
            for (int i = 3; i <= total - 2; i++) builderCont.removeViewAt(i);
        }

        private void saveClicked() {
            // Validate field.
            if (currFieldName == null || currFieldType == null) {
                Toast.makeText(getContext(), R.string.error_must_set_field, Toast.LENGTH_LONG).show();
                return;
            }
            // Validate conditional.
            if (currTransName == null) {
                Toast.makeText(getContext(), R.string.error_must_set_conditional, Toast.LENGTH_LONG).show();
                return;
            }
            // Validate and get args.
            Object[] args = getArgsIfValid();
            if (args == null) return;

            // Get card.
            RQVCard2 card = (RQVCard2) conditionsCont.getChildAt(currIdx);
            // Create or get condition.
            Condition condition = currIdx == conditionsCont.getChildCount() - 1 ? new Condition()
                    : ruq.getConditions().get(currIdx);
            // Fill in/update the condition.
            if (condition.getRealmClass() == null) condition.setRealmClass(currClassName);
            condition.setField(currFieldName);
            condition.setTransformer(currTransName);
            condition.setArgs(args);
            // Get the visible condition string.
            String visCondString = condition.toString();

            // If the card is still in OUTLINES mode, we know this is a new Condition, and that we need to do a bit
            // more setup for the card prior to adding the Condition to the query and switching back to MAIN mode.
            if (card.getMode() == RQVCard2.Mode.OUTLINES) {
                // New condition, we need to set the card up a bit more too.
                // Set the card's listener and long click listener.
                card.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onConditionClicked((Integer) v.getTag(R.id.index));
                    }
                });
                card.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        onPartLongClicked((Integer) v.getTag(R.id.index));
                        return true;
                    }
                });
                // Set the card's text (and its tag).
                card.setTag(R.id.curr_val, visCondString);
                card.setCardText(visCondString);
                // Set the card's mode to CARD.
                card.setMode(RQVCard2.Mode.CARD);

                // Add the condition to the query.
                ruq.getConditions().add(condition);

                // Finally, append another add view to the conditions container.
                appendAddView();
            }

            // Switch back to MAIN mode.
            switchMode(Mode.MAIN);
        }

        private Object[] getArgsIfValid() {
            Object[] args = new Object[argViews.size()];
            for (int i = 0; i < argViews.size(); i++) {
                switch (currFieldType) {
                    case BOOLEAN:
                        // There's no way that neither of the radio buttons are checked :)
                        RadioGroup rgFalseTrue = (RadioGroup) argViews.get(i);
                        args[i] = rgFalseTrue.getCheckedRadioButtonId() != R.id.rb_false;
                        continue;
                    case DATE:
                        TextView tvDate = (TextView) argViews.get(i).findViewById(R.id.tv_date);
                        if (tvDate.length() == 0) {
                            tvDate.setError(getContext().getString(R.string.error_empty_input));
                            return null;
                        }
                        args[i] = calFromString(tvDate.getText().toString()).getTime();
                        continue;
                    case DOUBLE:
                        EditText etDouble = (EditText) argViews.get(i);
                        if (etDouble.length() == 0) {
                            etDouble.setError(getContext().getString(R.string.error_empty_input));
                            return null;
                        }
                        args[i] = FieldType.parseNumberIfPossible(currFieldType, etDouble.getText().toString());
                        if (args[i] == null) {
                            etDouble.setError(getContext().getString(R.string.error_out_of_range_double));
                            return null;
                        }
                        continue;
                    case FLOAT:
                        EditText etFloat = (EditText) argViews.get(i);
                        if (etFloat.length() == 0) {
                            etFloat.setError(getContext().getString(R.string.error_empty_input));
                            return null;
                        }
                        args[i] = FieldType.parseNumberIfPossible(currFieldType, etFloat.getText().toString());
                        if (args[i] == null) {
                            etFloat.setError(getContext().getString(R.string.error_out_of_range_float));
                            return null;
                        }
                        continue;
                    case INTEGER:
                        EditText etInteger = (EditText) argViews.get(i);
                        if (etInteger.length() == 0) {
                            etInteger.setError(getContext().getString(R.string.error_empty_input));
                            return null;
                        }
                        args[i] = FieldType.parseNumberIfPossible(currFieldType, etInteger.getText().toString());
                        if (args[i] == null) {
                            etInteger.setError(getContext().getString(R.string.error_out_of_range_integer));
                            return null;
                        }
                        continue;
                    case LONG:
                        EditText etLong = (EditText) argViews.get(i);
                        if (etLong.length() == 0) {
                            etLong.setError(getContext().getString(R.string.error_empty_input));
                            return null;
                        }
                        args[i] = FieldType.parseNumberIfPossible(currFieldType, etLong.getText().toString());
                        if (args[i] == null) {
                            etLong.setError(getContext().getString(R.string.error_out_of_range_long));
                            return null;
                        }
                        continue;
                    case SHORT:
                        EditText etShort = (EditText) argViews.get(i);
                        if (etShort.length() == 0) {
                            etShort.setError(getContext().getString(R.string.error_empty_input));
                            return null;
                        }
                        args[i] = FieldType.parseNumberIfPossible(currFieldType, etShort.getText().toString());
                        if (args[i] == null) {
                            etShort.setError(getContext().getString(R.string.error_out_of_range_short));
                            return null;
                        }
                        continue;
                    case STRING:
                        EditText etString = (EditText) argViews.get(i);
                        args[i] = etString.getText().toString();
                        if (((String) args[i]).isEmpty()) {
                            etString.setError(getContext().getString(R.string.error_empty_input));
                            return null;
                        }
                }
            }
            return args;
        }
    }

    /**
     * Helps handle views while in sort builder mode.
     */
    private class SortBuilderHelper extends BuilderHelper {
        public ArrayList<Spinner> spinners;
        public ArrayList<ImageButton> removeButtons;
        public ArrayList<RadioGroup> sortDirOptions;
        public Button addSortField;

        /**
         * Create a new {@link SortBuilderHelper}. This will set everything up for the sort builder view, but won't
         * switch to it.
         */
        SortBuilderHelper(ArrayList<String> sortFields, ArrayList<Sort> sortDirs) {
            super();
            // Set header text.
            header.setText(R.string.choose_sort_fields_title);

            // Create arrays.
            spinners = new ArrayList<>();
            removeButtons = new ArrayList<>();
            sortDirOptions = new ArrayList<>();

            // Inflate add sort field button.
            addSortField = (Button) View.inflate(getContext(), R.layout.add_sort_field_button, null);

            // Set up button click handlers.
            addSortField.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSortField();
                }
            });
            saveButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveClicked();
                }
            });

            // Add views to the builder container.
            builderCont.addView(header);
            builderCont.addView(addSortField);
            builderCont.addView(buttons);

            // If present, add current sort fields.
            for (int i = 0; i < sortFields.size(); i++) {
                // Add the sort field layout.
                addSortField();
                // Select the correct item in the spinner. TODO do we need to manually update the radio buttons' text?
                spinners.get(i).setSelection(currVisibleFlatFieldNames.indexOf(
                        Ruqus.visibleFieldFromField(currClassName, sortFields.get(i))));
                // Select the correct radio button.
                sortDirOptions.get(i).check(sortDirs.get(i) == Sort.ASCENDING ? R.id.asc : R.id.desc);
            }
        }

        /**
         * Called to add a sort field layout.
         */
        private void addSortField() {
            final int idx = spinners.size();
            RelativeLayout sortPart = (RelativeLayout) View.inflate(getContext(), R.layout.sort_part, null);

            // Set label text.
            ((TextView) sortPart.findViewById(R.id.sort_field_label)).setText(
                    getContext().getString(R.string.sort_field_label, idx + 1));

            // Set up spinner.
            Spinner spinner = (Spinner) sortPart.findViewById(R.id.sort_field);
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_dropdown_item, currVisibleFlatFieldNames);
            spinner.setAdapter(spinnerAdapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    setSortDirOptions(idx, (String) parent.getItemAtPosition(position));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    sortDirOptions.get(idx).setVisibility(GONE);
                }
            });

            // Set up remove button.
            ImageButton remove = (ImageButton) sortPart.findViewById(R.id.remove_field);
            DrawableCompat.setTint(remove.getDrawable(), theme == RuqusTheme.LIGHT ? Ruqus.DARK_TEXT_COLOR :
                    Ruqus.LIGHT_TEXT_COLOR);
            remove.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSortField(idx);
                }
            });

            // Set up radio group.
            RadioGroup sortDir = (RadioGroup) sortPart.findViewById(R.id.rg_sort_dir);

            // Add views to lists.
            spinners.add(spinner);
            removeButtons.add(remove);
            sortDirOptions.add(sortDir);

            // If that was our third sort field, we disable the button to add more of them.
            if (spinners.size() == 3) addSortField.setEnabled(false);

            // Finally, add this to the builder container (Add one, since we want it added after the header view).
            builderCont.addView(sortPart, idx + 1);
        }

        /**
         * Called when a remove button is clicked.
         */
        private void removeSortField(int index) {
            // Remove from holder.
            spinners.remove(index);
            removeButtons.remove(index);
            sortDirOptions.remove(index);

            // Remove from builder container (Add one to the index, since the header view is there).
            builderCont.removeViewAt(index + 1);

            // Enable add button.
            addSortField.setEnabled(true);
        }

        /**
         * Called when the selected spinner item is changed.
         */
        private void setSortDirOptions(int index, String visibleFieldName) {
            String[] pretty = Ruqus.typeEnumForField(currClassName, Ruqus.fieldFromVisibleField(
                    currClassName, visibleFieldName)).getPrettySortStrings();
            RadioGroup rg = sortDirOptions.get(index);
            ((RadioButton) rg.findViewById(R.id.asc)).setText(pretty[0]);
            ((RadioButton) rg.findViewById(R.id.desc)).setText(pretty[1]);
            rg.setVisibility(VISIBLE);
        }

        /**
         * Called when the save button is clicked.
         */
        private void saveClicked() {
            ArrayList<String> sortFields = new ArrayList<>();
            ArrayList<Sort> sortDirs = new ArrayList<>();

            // Get sort fields.
            for (Spinner spinner : spinners)
                sortFields.add(Ruqus.fieldFromVisibleField(currClassName, (String) spinner.getSelectedItem()));
            for (RadioGroup radioGroup : sortDirOptions)
                sortDirs.add(radioGroup.getCheckedRadioButtonId() == R.id.asc ? Sort.ASCENDING : Sort.DESCENDING);

            // Set ruq sort fields.
            ruq.setSorts(sortFields, sortDirs);

            // Set sort chooser mode and/or card text.
            sortChooser.setMode(sortFields.size() > 0 ? RQVCard.Mode.CARD : RQVCard.Mode.OUTLINE);
            sortChooser.setCardText("Sorted by " + ruq.getSortString());

            // Switch back to main container.
            switchMode(Mode.MAIN);
        }
    }

    static class SavedState extends BaseSavedState {

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            // TODO
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            // TODO
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
