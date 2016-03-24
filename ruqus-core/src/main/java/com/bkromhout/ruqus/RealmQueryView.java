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
import android.view.View;
import android.widget.*;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import io.realm.Sort;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * RealmQueryView
 * @author bkromhout
 */
public class RealmQueryView extends FrameLayout implements DatePickerDialog.OnDateSetListener {
    private enum Mode {
        MAIN, C_BUILD, S_BUILD
    }

    /* Views for main mode. */
    private RelativeLayout mainCont;
    private RQVCard queryableChooser;
    private ScrollView scrollView;
    private LinearLayout conditionsCont;
    private RQVCard sortChooser;

    /* Views for either builder mode. */
    private RelativeLayout builderCont;
    private TextView builderHeader;
    private LinearLayout builderParts;
    private Button cancelButton;
    private Button saveButton;

    /* Views for condition builder mode. */
    private Spinner fieldChooser;
    private Spinner conditionalChooser;

    /* Views for sort builder mode. */
    private Button addSortField;

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
     * List of current visible flat field names; changes when {@link #currClassName} changes.
     */
    private ArrayList<String> currVisibleFlatFieldNames;

    /* Variables for the condition builder. */

    /**
     * Index of the query part currently being worked on.
     */
    private int currPartIdx;
    /**
     * Real name of the field currently selected in the condition builder.
     */
    private String currFieldName;
    /**
     * {@link FieldType} of the field currently selected in the condition builder; changes when {@link #currFieldName}
     * changes.
     */
    private FieldType currFieldType;
    /**
     * Real name of the transformer/conditional currently selected in the condition builder.
     */
    private String currTransName;
    /**
     * Holds IDs of views added to the {@link #builderParts} view group which we check to get arguments.
     */
    private ArrayList<Integer> argViewIds;

    /* Variables for the sort builder. */



    /* Constructors. */

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

        // Find main mode views.
        mainCont = (RelativeLayout) findViewById(R.id.main);
        queryableChooser = (RQVCard) findViewById(R.id.queryable_type);
        scrollView = (ScrollView) findViewById(R.id.rqv_scroll_view);
        conditionsCont = (LinearLayout) findViewById(R.id.rqv_content);
        sortChooser = (RQVCard) findViewById(R.id.sort_type);

        // Find common builder mode views.
        builderCont = (RelativeLayout) findViewById(R.id.builder);
        builderHeader = (TextView) findViewById(R.id.builder_header);
        builderParts = (LinearLayout) findViewById(R.id.builder_parts);
        cancelButton = (Button) findViewById(R.id.cancel);
        saveButton = (Button) findViewById(R.id.save);

        // Find condition builder views.
        fieldChooser = (Spinner) findViewById(R.id.field_chooser);
        conditionalChooser = (Spinner) findViewById(R.id.conditional_chooser);

        // Find sort builder views.
        addSortField = (Button) findViewById(R.id.add_sort_field);

        // Read attributes.
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
        // Set up main mode views.
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

        // Set up common builder views. TODO
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switchMode(Mode.MAIN);
            }
        });
        saveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClicked();
            }
        });

        // Set up condition builder views. TODO
        fieldChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Make sure user didn't select nothing.
                String selStr = (String) parent.getItemAtPosition(position);
                if (Ruqus.CHOOSE_FIELD.equals(selStr)) {
                    currFieldName = null;
                    onSelCondFieldChanged();
                    return;
                }
                // It's a real field.
                String realFieldName = Ruqus.fieldFromVisibleField(currClassName, selStr);
                if (currFieldName == null || currFieldName.equals(realFieldName)) {
                    // We only care if if was changed.
                    currFieldName = realFieldName;
                    onSelCondFieldChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currFieldName = null;
                onSelCondFieldChanged();
            }
        });
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

        // Set up sort builder views. TODO


        // Finish setup.
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
        // TODO theme, mode, currPartIdx, ruq string
        // TODO add holder to builderParts tag??

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

    /**
     * Switches the view between main and various builder modes.
     * @param mode Mode to switch to.
     */
    private void switchMode(Mode mode) {
        // Make sure we clean up if coming from a builder mode, or hide the main container if going to one.
        if (this.mode == Mode.C_BUILD && mode == Mode.MAIN) tearDownConditionBuilderMode();
        else if (this.mode == Mode.S_BUILD && mode == Mode.MAIN) tearDownSortBuilderMode();
        else if (this.mode == Mode.MAIN && mode != this.mode) mainCont.setVisibility(GONE);
        // Switch mode and UI.
        switch (mode) {
            case MAIN:
                mainCont.setVisibility(VISIBLE);
                break;
            case C_BUILD: {
                initConditionBuilderMode(currPartIdx >= conditionsCont.getChildCount() - 1 ? null
                        : ruq.getConditions().get(currPartIdx));
                builderCont.setVisibility(VISIBLE);
                break;
            }
            case S_BUILD:
                initSortBuilderMode(ruq.getSortFields(), ruq.getSortDirs());
                builderCont.setVisibility(VISIBLE);
                break;
        }
    }

    /**
     * Sets the theme of the view and any child views.
     * @param theme Theme to switch to.
     */
    public void setTheme(RuqusTheme theme) {
        this.theme = theme;

        // Set theme on queryable and sort choosers.
        queryableChooser.setTheme(theme);
        sortChooser.setTheme(theme);

        // Set theme on all condition cards.
        for (int i = 0; i < conditionsCont.getChildCount(); i++)
            ((RQVCard2) conditionsCont.getChildAt(i)).setTheme(theme);

        // TODO Set for builder modes.
    }

    /**
     * Sets the "enabled" state of the query parts container and the sort chooser.
     * @param enabled If true, enable views. Otherwise disable them.
     */
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
     * Clears the view back to its initial state and sets {@link #ruq} to a new instance of {@link RealmUserQuery}.
     */
    private void reset() {
        // Switch to main mode (will tear down builder modes if necessary).
        switchMode(Mode.MAIN);
        // New RUQ.
        ruq = new RealmUserQuery();
        currClassName = null;
        currVisibleFlatFieldNames = null;
        currPartIdx = -1;
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

        // Set condition builder field chooser's adapter.
        fieldChooser.setAdapter(makeFieldAdapter());

        // Set queryable chooser's card text and mode.
        queryableChooser.setCardText(visibleName);
        queryableChooser.setMode(RQVCard.Mode.CARD);

        // Append an add view to the conditions container, then enable the conditions container and sort chooser.
        appendAddView();
        setConditionsAndSortEnabled(true);
    }

    private ArrayAdapter<String> makeFieldAdapter() {
        return new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                currVisibleFlatFieldNames);
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
     * Called when an {@link RQVCard2}'s outline text view which reads "Add Condition", or when a card which has been
     * filled in with a real condition, is clicked. Switches to condition builder mode.
     * @param index Index of the card in the conditions container.
     */
    private void onConditionClicked(final int index) {
        this.currPartIdx = index;
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
     * Called when the builder's save button is clicked (in either builder mode).
     */
    private void onSaveClicked() {
        switch (mode) {
            case C_BUILD: {
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
                RQVCard2 card = (RQVCard2) conditionsCont.getChildAt(currPartIdx);
                // Create or get condition.
                Condition condition = currPartIdx == conditionsCont.getChildCount() - 1 ? new Condition()
                        : ruq.getConditions().get(currPartIdx);
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
                break;
            }
            case S_BUILD: {
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
                break;
            }
        }
        // Switch back to main container.
        switchMode(Mode.MAIN);
    }

    /* Methods for Condition builder mode. */

    /**
     * Called to set up the builder views for condition builder mode.
     * @param condition Condition to use to pre-fill views.
     */
    private void initConditionBuilderMode(Condition condition) {
        // Make sure currPartIdx is set.
        if (currPartIdx == -1) throw new IllegalArgumentException("Must set currPartIdx for C_BUILD mode.");

        // Set up views.
        builderHeader.setText(R.string.edit_condition_title);
        fieldChooser.setVisibility(VISIBLE);

        // Set up instance vars.
        argViewIds = new ArrayList<>();

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
     * Called to clean up the builder views when finishing condition builder mode.
     */
    private void tearDownConditionBuilderMode() {
        // Clean up views.
        fieldChooser.setVisibility(GONE);
        conditionalChooser.setVisibility(GONE);
        builderParts.removeAllViews();

        // Clean up vars.
        currPartIdx = -1;
        currFieldName = null;
        currFieldType = null;
        currTransName = null;
        argViewIds = null;
    }

    /**
     * Called when the selection in {@link #fieldChooser} changes. Sets up other views based on value of {@link
     * #currFieldName}.
     */
    private void onSelCondFieldChanged() {
        // If currFieldName is null, we should tear some things down.
        if (currFieldName == null) {
            currFieldType = null;
            updateArgViews();
            conditionalChooser.setVisibility(GONE);
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

    /**
     * Update the views in {@link #builderParts} so that they allow the user to input the correct type of data based on
     * the current {@link #currFieldName}, {@link #currFieldType}, and {@link #currTransName}.
     */
    private void updateArgViews() {
        builderParts.removeAllViews();
        if (currFieldName == null || currFieldType == null || currTransName == null) {
            currTransName = null;
            return;
        }

        // Add views based on the field type and the number of arguments that the transformer accepts.
        int numArgs = Ruqus.numberOfArgsFor(currTransName);
        for (int i = 0; i < numArgs; i++) {
            final int id = Util.getUniqueViewId();
            switch (currFieldType) {
                case BOOLEAN:
                    RadioGroup rgFalseTrue = (RadioGroup) View.inflate(getContext(), R.layout.rg_false_true, null);
                    rgFalseTrue.setId(id);
                    builderParts.addView(rgFalseTrue);
                    break;
                case DATE:
                    LinearLayout tvDateCont = (LinearLayout) View.inflate(getContext(), R.layout.tv_date, null);
                    // Set up date button to open date picker dialog.
                    tvDateCont.findViewById(R.id.choose_date).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            TextView tvDate = (TextView) builderParts.findViewById(id).findViewById(R.id.tv_date);
                            makeDatePickerDialog(Util.calFromString(tvDate.getText().toString()), id)
                                    .show(((Activity) getContext()).getFragmentManager(), "RuqusDPD");
                        }
                    });
                    tvDateCont.setId(id);
                    builderParts.addView(tvDateCont);
                    break;
                case DOUBLE:
                case FLOAT:
                    EditText etDecimal = (EditText) View.inflate(getContext(), R.layout.et_decimal, null);
                    etDecimal.setId(id);
                    builderParts.addView(etDecimal);
                    break;
                case INTEGER:
                case LONG:
                case SHORT:
                    EditText etWholeNumber = (EditText) View.inflate(getContext(), R.layout.et_whole_number, null);
                    etWholeNumber.setId(id);
                    builderParts.addView(etWholeNumber);
                    break;
                case STRING:
                    EditText etString = (EditText) View.inflate(getContext(), R.layout.et_string, null);
                    etString.setId(id);
                    builderParts.addView(etString);
                    break;
            }
            argViewIds.add(id);
        }
    }

    /**
     * Make the date picker dialog to show for date inputs.
     * @param c  Calendar instance to use to set initially selected date.
     * @param id ID of view to modify upon callback.
     * @return The newly-created date picker dialog.
     */
    private DatePickerDialog makeDatePickerDialog(Calendar c, int id) {
        DatePickerDialog dpd = DatePickerDialog.newInstance(RealmQueryView.this,
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dpd.setThemeDark(theme == RuqusTheme.DARK);
        Bundle b = new Bundle();
        b.putInt("ID", id);
        dpd.setArguments(b);
        dpd.autoDismiss(true);
        return dpd;
    }

    /**
     * Fill in argument views in condition builder using the values passed in {@code args}.
     * @param args Values retrieved using {@link Condition#getArgs()}.
     */
    private void fillArgViews(Object[] args) {
        for (int i = 0; i < argViewIds.size(); i++) {
            View view = builderCont.findViewById(argViewIds.get(i));

            switch (currFieldType) {
                case BOOLEAN:
                    ((RadioGroup) view).check((Boolean) args[i] ? R.id.rb_true : R.id.rb_false);
                    break;
                case DATE:
                    ((TextView) view.findViewById(R.id.tv_date)).setText(Util.dateFormat.format(args[i]));
                    break;
                case DOUBLE:
                case FLOAT:
                case INTEGER:
                case LONG:
                case SHORT:
                    ((TextView) view).setText(String.valueOf(args[i]));
                    break;
                case STRING:
                    ((TextView) view).setText((String) args[i]);
                    break;
            }
        }
    }

    /**
     * Called when a date was picked in the date picker dialog.
     */
    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        int index = view.getArguments().getInt("IDX", -1);
        if (index == -1) throw new IllegalArgumentException("Bad index!");
        ((TextView) builderCont.findViewById(argViewIds.get(index)).findViewById(R.id.tv_date)).setText(
                Util.stringFromDateInts(year, monthOfYear, dayOfMonth));
    }

    /**
     * Attempts to validates the values that the user has provided to the condition builder, and returns them if they
     * pass.
     * @return Array of Obkect
     */
    private Object[] getArgsIfValid() {
        Object[] args = new Object[argViewIds.size()];
        for (int i = 0; i < argViewIds.size(); i++) {
            switch (currFieldType) {
                case BOOLEAN:
                    // There's no way that neither of the radio buttons are checked :)
                    RadioGroup rgFalseTrue = (RadioGroup) argViewIds.get(i);
                    args[i] = rgFalseTrue.getCheckedRadioButtonId() != R.id.rb_false;
                    continue;
                case DATE:
                    TextView tvDate = (TextView) argViewIds.get(i).findViewById(R.id.tv_date);
                    if (tvDate.length() == 0) {
                        tvDate.setError(getContext().getString(R.string.error_empty_input));
                        return null;
                    }
                    args[i] = Util.calFromString(tvDate.getText().toString()).getTime();
                    continue;
                case DOUBLE:
                    EditText etDouble = (EditText) argViewIds.get(i);
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
                    EditText etFloat = (EditText) argViewIds.get(i);
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
                    EditText etInteger = (EditText) argViewIds.get(i);
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
                    EditText etLong = (EditText) argViewIds.get(i);
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
                    EditText etShort = (EditText) argViewIds.get(i);
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
                    EditText etString = (EditText) argViewIds.get(i);
                    args[i] = etString.getText().toString();
                    if (((String) args[i]).isEmpty()) {
                        etString.setError(getContext().getString(R.string.error_empty_input));
                        return null;
                    }
            }
        }
        return args;
    }

    /* Methods for sort builder mode. */

    /**
     * Called to set up the builder views for sort builder mode.
     * @param sortFields Current sort fields.
     * @param sortDirs   Current sort directions.
     */
    private void initSortBuilderMode(ArrayList<String> sortFields, ArrayList<Sort> sortDirs) {

    }

    /**
     * Called to clean up the builder views when finishing sort builder mode.
     */
    private void tearDownSortBuilderMode() {
        // TODO
    }

    /**
     * Helps handle views while in sort builder mode.
     * <p/>
     * TODO make sure all views get a custom ID.
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
            builderParts.addView(sortPart, idx + 1);
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
            builderParts.removeViewAt(index + 1);

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
    }

    /**
     * Helps us easily save and restore our view's state. TODO need to move builder helper methods out of their helper
     * classes to use this.
     */
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
