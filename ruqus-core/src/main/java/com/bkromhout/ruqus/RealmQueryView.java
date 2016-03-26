package com.bkromhout.ruqus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import io.realm.Sort;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * RealmQueryView
 * @author bkromhout
 */
public class RealmQueryView extends FrameLayout {
    private static final String ARG_STR_SEP = ";";
    private static final Pattern ARG_STR_SEP_PATTERN = Pattern.compile("\\Q" + ARG_STR_SEP + "\\E");

    private enum Mode {
        MAIN, C_BUILD, S_BUILD
    }

    /* Views for main mode. */
    private RelativeLayout mainCont;
    private RQVCard queryableChooser;
    private ScrollView scrollView;
    private LinearLayout partsCont;
    private RQVCard sortChooser;

    /* Views for either builder mode. */
    private ScrollView builderScrollView;
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
    private RuqusTheme theme;
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
    /**
     * List of View IDs for views in {@link #partsCont}.
     */
    private ArrayList<Integer> partIds;

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
    /**
     * Holds IDs of sort field spinners.
     */
    private ArrayList<Integer> sortSpinnerIds;
    /**
     * Holds IDs of buttons which remove sort fields.
     */
    private ArrayList<Integer> removeSortBtnIds;
    /**
     * Holds IDs of radio groups which set sort directions.
     */
    private ArrayList<Integer> sortDirRgIds;

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

    /* Public methods. */

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
        for (int i = 0; i < partsCont.getChildCount(); i++)
            ((RQVCard2) partsCont.getChildAt(i)).setTheme(theme);

        // TODO Set for builder modes.
    }

    /**
     * Check whether the {@link RealmUserQuery} that this {@link RealmQueryView} currently has is fully-formed.
     * @return True if query is fully-formed, otherwise false.
     */
    public boolean isQueryValid() {
        return ruq.isQueryValid();
    }

    /**
     * Get the {@link RealmUserQuery} which this {@link RealmQueryView} currently has. Note that {@link RealmUserQuery}
     * implements {@link Parcelable}, which allows it to be passed around quickly and easily.
     * <p/>
     * This method does not guarantee that the returned query will be fully-formed and valid. Call {@link
     * RealmUserQuery#isQueryValid()} to check for validity before calling {@link RealmUserQuery#execute()}.
     * @return Realm user query object.
     */
    public RealmUserQuery getRealmUserQuery() {
        return this.ruq;
    }

    /**
     * Set up this {@link RealmQueryView} using the given {@link RealmUserQuery}.
     * @param ruq Realm user query to use to set up this {@link RealmQueryView}. Must be fully-formed or this method
     *            will do nothing.
     */
    public void setRealmUserQuery(RealmUserQuery ruq) {
        if (!ruq.isQueryValid()) return;
        this.ruq = ruq;
        setupUsingRUQ();
    }

    /* Non-public methods. */

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
        scrollView = (ScrollView) findViewById(R.id.main_scroll_view);
        partsCont = (LinearLayout) findViewById(R.id.query_parts);
        sortChooser = (RQVCard) findViewById(R.id.sort_type);

        // Find common builder mode views.
        builderScrollView = (ScrollView) findViewById(R.id.builder_scroll_view);
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
            theme = typedArray.getInt(R.styleable.RealmQueryView_ruqus_theme, RuqusTheme.LIGHT.ordinal()) == 0
                    ? RuqusTheme.LIGHT : RuqusTheme.DARK;
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

        // Set up common builder views.
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

        // Set up condition builder views.
        fieldChooser.setOnTouchListener(fieldChooserListener);
        fieldChooser.setOnItemSelectedListener(fieldChooserListener);
        conditionalChooser.setOnTouchListener(conditionalChooserListener);
        conditionalChooser.setOnItemSelectedListener(conditionalChooserListener);

        // Set up sort builder views.
        addSortField.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addSortFieldView(-1, null);
            }
        });

        // Finish setup.
        if (ruq == null) {
            // If we don't have a realm user query already, setup is very minimal, we just disable the scrollview and
            // sort choosers.
            setConditionsAndSortEnabled(false);
        } else {
            // If we have a RUQ already, we need to draw our view accordingly. Unless it's invalid, in which case we
            // want to get rid of it.
            if (ruq.isQueryValid()) setupUsingRUQ();
            else ruq = null;
        }

    }

    @Override
    protected Parcelable onSaveInstanceState() {
        // Allow parent classes to save state.
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        // Save our state.
        ss.theme = this.theme;
        ss.ruq = this.ruq;
        ss.mode = this.mode;
        if (this.mode == Mode.C_BUILD) {
            // Only save condition builder variables if we're in that mode.
            ss.currPartIdx = this.currPartIdx;
            ss.currFieldName = this.currFieldName;
            ss.currTransName = this.currTransName;
            ss.currArgsString = stashableArgsString();
        } else if (this.mode == Mode.S_BUILD) {
            // Only save sort builder variables if we're in that mode.
            ss.currSortFields = stashableSortFields();
            ss.currSortDirs = stashableSortDirections();
        }

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
        this.theme = ss.theme;
        this.ruq = ss.ruq;
        this.mode = ss.mode;
        setupUsingRUQ();
        if (this.mode == Mode.C_BUILD) {
            // Only try to restore condition builder if we were in that mode.
            this.currPartIdx = ss.currPartIdx;
            this.currFieldName = ss.currFieldName;
            this.currTransName = ss.currTransName;
            restoreConditionBuilderMode(ss.currArgsString);
        } else if (this.mode == Mode.S_BUILD) {
            // Only try to restore sort builder if we were in that mode.
            restoreSortBuilderMode(ss.currSortFields, ss.currSortDirs);
        }
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
                initConditionBuilderMode(currPartIdx >= partsCont.getChildCount() - 1 ? null
                        : ruq.getConditions().get(currPartIdx));
                builderScrollView.setVisibility(VISIBLE);
                break;
            }
            case S_BUILD:
                initSortBuilderMode(ruq.getSortFields(), ruq.getSortDirs());
                builderScrollView.setVisibility(VISIBLE);
                break;
        }
        this.mode = mode;
    }

    /**
     * Set up the view using the current value of {@link #ruq}.
     */
    private void setupUsingRUQ() {
        if (ruq == null || ruq.getQueryClass() == null) return;
        // Set queryable class.
        String realName = ruq.getQueryClass().getSimpleName();
        setQueryable(realName, Ruqus.getClassData().visibleNameOf(realName));

        // Set sort fields (if present).
        if (ruq.getSortFields().size() > 0) {
            sortChooser.setMode(RQVCard.Mode.CARD);
            sortChooser.setCardText("Sorted by " + ruq.getSortString());
        } else sortChooser.setMode(RQVCard.Mode.OUTLINE);

        // Add part cards.
        partsCont.removeAllViews(); // Make sure parts container is empty first!
        partIds = new ArrayList<>();
        for (Condition condition : ruq.getConditions()) appendPartView(condition);

        // Append an add part view.
        appendAddPartView();
    }

    /**
     * Helps us convert part view IDs to their indices in {@link #partsCont}.
     * @param viewId ID of an {@link RQVCard2} in the {@link #partsCont}.
     * @return Index of view with {@code viewId}.
     */
    private int idxFromId(@IdRes int viewId) {
        return partIds.indexOf(viewId);
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
     * Creates an {@link RQVCard2} and sets it to card mode, filling it in using the given {@code condition}. The
     * condition doesn't need to be valid, this method is used when restoring the view's state after a configuration
     * change.
     * @param condition Condition to use to fill the card's text.
     */
    private void appendPartView(Condition condition) {
        if (condition == null) throw new IllegalArgumentException("Must provide a Condition.");
        // Get visible condition string.
        String visCondString = condition.toString();
        // Create a new card.
        RQVCard2 cond = new RQVCard2(getContext(), theme);
        cond.setMode(RQVCard2.Mode.CARD);
        // Get a unique view ID.
        int vid = Util.getUniqueViewId();
        // Set index tag to the current child count of the conditions container, since that will be this item's index
        // once it is added to the end of it. Also set content tag to the same as the current content.
        cond.setTag(R.id.ruqus_id, vid);
        cond.setTag(R.id.ruqus_curr_val, visCondString);
        // Set the card's text to the visible condition string.
        cond.setCardText(visCondString);
        // Set the card's click listener based on the type of condition.
        if (condition.getType() == Condition.Type.NORMAL) {
            // Normal condition listener.
            cond.setCardClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onConditionClicked(idxFromId((Integer) v.getTag(R.id.ruqus_id)));
                }
            });
        } else {
            // Operator listener.
            cond.setCardClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onOperatorClicked(idxFromId((Integer) v.getTag(R.id.ruqus_id)),
                            (String) v.getTag(R.id.ruqus_curr_val));
                }
            });
        }
        // Set the card's long click listener.
        cond.setCardLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onPartLongClicked(idxFromId((Integer) v.getTag(R.id.ruqus_id)));
                return true;
            }
        });
        // Set a unique view ID.
        cond.setId(vid);
        // Add to the parts container and add the ID.
        partsCont.addView(cond);
        partIds.add(vid);
    }

    /**
     * Creates an {@link RQVCard2} and sets it to outlines mode with the texts "Add Operator" and "Add Condition", then
     * adds it to the end of {@link #partsCont}.
     */
    private void appendAddPartView() {
        // Only add the view if we have the same number of views and conditions currently (indicates each view is
        // tied to a condition.
        if (ruq != null && partsCont.getChildCount() == ruq.conditionCount()) {
            RQVCard2 add = new RQVCard2(getContext(), theme);
            add.setMode(RQVCard2.Mode.OUTLINES);
            int vid = Util.getUniqueViewId();
            add.setOutlineText(R.string.ruqus_add_operator_nl, R.string.ruqus_add_condition_nl);
            // Set tag to the current child count of the conditions container, since that will be this item's index
            // once it is added to the end of it.
            add.setTag(R.id.ruqus_id, vid);
            // Set the outline text views' OnClickListeners.
            add.setOutline1ClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onOperatorClicked(idxFromId((Integer) v.getTag(R.id.ruqus_id)), null);
                }
            });
            add.setOutline2ClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onConditionClicked(idxFromId((Integer) v.getTag(R.id.ruqus_id)));
                }
            });
            // Set a unique view ID.
            add.setId(vid);
            // Add to the parts container.
            partsCont.addView(add);
            partIds.add(vid);
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
        partsCont.removeAllViews();
        partIds = new ArrayList<>();
        // Disable conditions container and sort chooser.
        setConditionsAndSortEnabled(false);
    }

    /**
     * Show a dialog with the visible names of all classes annotated with {@link Queryable}.
     */
    private void onQueryableChooserClicked() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.ruqus_choose_queryable_title)
                .items(Ruqus.getClassData().getVisibleNames(true))
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        String realName = Ruqus.classNameFromVisibleName(text.toString());
                        reset();
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
        // Set instance vars.
        partIds = new ArrayList<>();
        currClassName = realName;
        currVisibleFlatFieldNames = Ruqus.visibleFlatFieldsForClass(currClassName);
        currVisibleFlatFieldNames.add(0, Ruqus.CHOOSE_FIELD);

        // Set condition builder field chooser's adapter.
        fieldChooser.setAdapter(makeFieldAdapter());

        // Set queryable chooser's card text and mode.
        queryableChooser.setCardText(visibleName);
        queryableChooser.setMode(RQVCard.Mode.CARD);

        // Append an add view to the conditions container, then enable the conditions container and sort chooser.
        appendAddPartView();
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
                .title(index == partsCont.getChildCount() - 1 ? R.string.ruqus_add_operator :
                        R.string.ruqus_change_operator)
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
        RQVCard2 card = (RQVCard2) partsCont.getChildAt(index);
        card.setTag(R.id.ruqus_curr_val, visibleName);
        if (card.getMode() == RQVCard2.Mode.OUTLINES) {
            // This was an outline-mode card before this, and ruq doesn't have a condition for it.
            // Set the card's card listener and long click listener.
            card.setCardClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onOperatorClicked(idxFromId((Integer) v.getTag(R.id.ruqus_id)),
                            (String) v.getTag(R.id.ruqus_curr_val));
                }
            });
            card.setCardLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onPartLongClicked(idxFromId((Integer) v.getTag(R.id.ruqus_id)));
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
            appendAddPartView();
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
                .title(R.string.ruqus_remove_operator)
                .negativeText(R.string.ruqus_no)
                .positiveText(R.string.ruqus_yes)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // Remove from RUQ.
                        ruq.getConditions().remove(index);
                        // Remove from conditions container.
                        partsCont.removeViewAt(index);
                        partIds.remove(index);
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
                    Toast.makeText(getContext(), R.string.ruqus_error_must_set_field, Toast.LENGTH_LONG).show();
                    return;
                }
                // Validate conditional.
                if (currTransName == null) {
                    Toast.makeText(getContext(), R.string.ruqus_error_must_set_conditional, Toast.LENGTH_LONG).show();
                    return;
                }
                // Validate and get args.
                Object[] args = getArgsIfValid();
                if (args == null) return;

                // Get card.
                RQVCard2 card = (RQVCard2) partsCont.getChildAt(currPartIdx);
                // Create or get condition.
                Condition condition = currPartIdx == partsCont.getChildCount() - 1 ? new Condition()
                        : ruq.getConditions().get(currPartIdx);
                // Fill in/update the condition.
                if (condition.getRealmClass() == null) condition.setRealmClass(currClassName);
                condition.setField(currFieldName);
                condition.setTransformer(currTransName);
                condition.setArgs(args);
                // Get the visible condition string.
                String visCondString = condition.toString();
                // Set the card's text (and its tag).
                card.setTag(R.id.ruqus_curr_val, visCondString);
                card.setCardText(visCondString);

                // If the card is still in OUTLINES mode, we know this is a new Condition, and that we need to do a bit
                // more setup for the card prior to adding the Condition to the query and switching back to MAIN mode.
                if (card.getMode() == RQVCard2.Mode.OUTLINES) {
                    // New condition, we need to set the card up a bit more too.
                    // Set the card's listener and long click listener.
                    card.setCardClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onConditionClicked(idxFromId((Integer) v.getTag(R.id.ruqus_id)));
                        }
                    });
                    card.setCardLongClickListener(new OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            onPartLongClicked(idxFromId((Integer) v.getTag(R.id.ruqus_id)));
                            return true;
                        }
                    });
                    // Set the card's mode to CARD.
                    card.setMode(RQVCard2.Mode.CARD);

                    // Add the condition to the query.
                    ruq.getConditions().add(condition);

                    // Finally, append another add view to the conditions container.
                    appendAddPartView();
                }
                break;
            }
            case S_BUILD: {
                ArrayList<String> sortFields = new ArrayList<>();
                ArrayList<Sort> sortDirs = new ArrayList<>();

                // Get sort fields.
                for (Integer sortSpinnerId : sortSpinnerIds)
                    sortFields.add(Ruqus.fieldFromVisibleField(currClassName,
                            (String) ((Spinner) builderParts.findViewById(sortSpinnerId)).getSelectedItem()));

                // Ensure none of the sort fields are the default "Choose Field" string.
                if (sortFields.contains(Ruqus.CHOOSE_FIELD)) {
                    Toast.makeText(getContext(), R.string.ruqus_error_some_sort_fields_not_chosen, Toast.LENGTH_LONG)
                         .show();
                    return;
                }

                // Get sort dirs.
                for (Integer sortDirRgId : sortDirRgIds)
                    sortDirs.add(((RadioGroup) builderParts.findViewById(sortDirRgId))
                            .getCheckedRadioButtonId() == R.id.asc ? Sort.ASCENDING : Sort.DESCENDING);

                // Set ruq sort fields.
                ruq.setSorts(sortFields, sortDirs);

                // Set sort chooser mode and/or card text.
                if (sortFields.size() > 0) {
                    sortChooser.setMode(RQVCard.Mode.CARD);
                    sortChooser.setCardText("Sorted by " + ruq.getSortString());
                } else sortChooser.setMode(RQVCard.Mode.OUTLINE);
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
        builderHeader.setText(R.string.ruqus_edit_condition_title);
        fieldChooser.setVisibility(VISIBLE);

        // Set up vars.
        argViewIds = new ArrayList<>();

        // Set up from condition.
        if (condition != null) {
            // Select correct value in field chooser.
            //currFieldName = condition.getField();
            fieldChooser.setSelection(currVisibleFlatFieldNames.indexOf(
                    Ruqus.visibleFieldFromField(currClassName, condition.getField())));
            // Set up views based on it.
            onFieldChooserItemSelected(Ruqus.visibleFieldFromField(currClassName, condition.getField()), false);

            // Select correct transformer.
            //currTransName = condition.getTransformer();
            conditionalChooser.setSelection(1 + Ruqus.getTransformerData().getVisibleNames(
                    currFieldType.getClazz()).indexOf(Ruqus.getTransformerData().visibleNameOf(
                    condition.getTransformer())));
            // Set up views based on it.
            onConditionalChooserItemSelected(Ruqus.getTransformerData().visibleNameOf(condition.getTransformer()),
                    false);

            // Fill in argument views.
            fillArgViews(condition.getArgs());
        }
    }

    /**
     * Called to restore the view hierarchy state if we were in condition builder mode prior to a configuration change.
     * @param argsString String containing values to put in arg views.
     */
    private void restoreConditionBuilderMode(String argsString) {
        // Make sure currPartIdx is set.
        if (currClassName == null) throw new IllegalArgumentException("Cannot restore without currClassName");
        if (currPartIdx == -1) throw new IllegalArgumentException("Must set currPartIdx for C_BUILD mode.");

        // Set up views.
        builderHeader.setText(R.string.ruqus_edit_condition_title);
        fieldChooser.setVisibility(VISIBLE);

        // Set up vars.
        argViewIds = new ArrayList<>();

        // Try to restore chosen field.
        if (currFieldName != null) {
            fieldChooser.setSelection(currVisibleFlatFieldNames.indexOf(
                    Ruqus.visibleFieldFromField(currClassName, currFieldName)));
            // Set up views based on it.
            onFieldChooserItemSelected(Ruqus.visibleFieldFromField(currClassName, currFieldName), true);
        }

        // Try to restore chosen transformer.
        if (currTransName != null) {
            conditionalChooser.setSelection(1 + Ruqus.getTransformerData().getVisibleNames(
                    currFieldType.getClazz()).indexOf(Ruqus.getTransformerData().visibleNameOf(currTransName)));
            // Set up views based on it.
            onConditionalChooserItemSelected(Ruqus.getTransformerData().visibleNameOf(currTransName), true);
        }

        // Try to restore inputs.
        restoreArgViews(argsString);

        // Hide main view and show builder view.
        mainCont.setVisibility(GONE);
        builderScrollView.setVisibility(VISIBLE);
    }

    /**
     * Called to clean up the builder views when finishing condition builder mode.
     */
    private void tearDownConditionBuilderMode() {
        // Clean up views.
        builderParts.removeAllViews();
        fieldChooser.setSelection(0);
        fieldChooser.setVisibility(GONE);
        conditionalChooser.setVisibility(GONE);
        builderScrollView.setVisibility(GONE);

        // Clean up vars.
        currPartIdx = -1;
        currFieldName = null;
        currFieldType = null;
        currTransName = null;
        argViewIds = null;
    }

    private SpinnerInteractionListener fieldChooserListener = new SpinnerInteractionListener() {
        boolean isTouching = false;

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (isTouching) {
                onFieldChooserItemSelected((String) parent.getItemAtPosition(position), false);
                isTouching = false;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            if (isTouching) {
                onFieldChooserItemSelected(Ruqus.CHOOSE_FIELD, false);
                isTouching = false;
            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            isTouching = true;
            return false;
        }
    };

    private SpinnerInteractionListener conditionalChooserListener = new SpinnerInteractionListener() {
        boolean isTouching = false;

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (isTouching) {
                onConditionalChooserItemSelected((String) parent.getItemAtPosition(position), false);
                isTouching = false;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            if (isTouching) {
                currTransName = null;
                updateArgViews();
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
     * Called when an item is selected in the condition builder field chooser.
     * @param selStr Selected item string.
     */
    private void onFieldChooserItemSelected(String selStr, boolean manual) {
        if (Ruqus.CHOOSE_FIELD.equals(selStr)) {
            currFieldName = null;
            currFieldType = null;
            updateArgViews();
            conditionalChooser.setVisibility(GONE);
            return;
        }
        // It's a real field.
        String realFieldName = Ruqus.fieldFromVisibleField(currClassName, selStr);
        if (manual || currFieldName == null || !currFieldName.equals(realFieldName)) {
            // We only care if if was changed.
            currFieldName = realFieldName;

            // Reset arg views.
            builderParts.removeAllViews();
            argViewIds = new ArrayList<>();

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
    }


    /**
     * Called when an item is selected in the condition builder conditional chooser.
     * @param selStr Selected item string.
     */
    private void onConditionalChooserItemSelected(String selStr, boolean manual) {
        if (Ruqus.CHOOSE_CONDITIONAL.equals(selStr)) {
            currTransName = null;
            updateArgViews();
            return;
        }
        // It's a real transformer.
        String realTransName = Ruqus.transformerNameFromVisibleName(selStr, false);
        if (manual || currTransName == null || !currTransName.equals(realTransName)) {
            // We only care if it was changed.
            currTransName = realTransName;
            updateArgViews();
        }
    }

    /**
     * Update the views in {@link #builderParts} so that they allow the user to input the correct type of data based on
     * the current {@link #currFieldName}, {@link #currFieldType}, and {@link #currTransName}.
     */
    private void updateArgViews() {
        builderParts.removeAllViews();
        argViewIds = new ArrayList<>();
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
                    DateInputView dateInputView = new DateInputView(getContext(), theme);
                    // Set up date button to open date picker dialog.
                    dateInputView.setId(id);
                    builderParts.addView(dateInputView);
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
     * Fill in argument views in condition builder using the values passed in {@code args}.
     * @param args Values retrieved using {@link Condition#getArgs()}.
     */
    private void fillArgViews(Object[] args) {
        if (args == null) return;
        for (int i = 0; i < args.length && i < argViewIds.size(); i++) {
            View view = builderParts.findViewById(argViewIds.get(i));

            switch (currFieldType) {
                case BOOLEAN:
                    ((RadioGroup) view).check((Boolean) args[i] ? R.id.rb_true : R.id.rb_false);
                    break;
                case DATE:
                    ((DateInputView) view).setDate((Date) args[i]);
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
     * Restore the condition builder arg views's contents.
     * @param argsString String containing values to put in arg views.
     * @see #stashableArgsString()
     */
    private void restoreArgViews(String argsString) {
        if (argsString == null || argsString.isEmpty()) return;
        String[] args = ARG_STR_SEP_PATTERN.split(argsString);
        for (int i = 0; i < args.length && i < argViewIds.size(); i++) {
            View view = builderParts.findViewById(argViewIds.get(i));
            switch (currFieldType) {
                case BOOLEAN:
                    ((RadioGroup) view).check("true".equals(args[i]) ? R.id.rb_true : R.id.rb_false);
                    break;
                case DATE:
                    ((DateInputView) view).setText(args[i]);
                    break;
                case DOUBLE:
                case FLOAT:
                case INTEGER:
                case LONG:
                case SHORT:
                case STRING:
                    ((TextView) view).setText(args[i]);
                    break;
            }
        }
    }

    /**
     * Attempts to validates the values that the user has provided to the condition builder, and returns them if they
     * pass.
     * @return Array of input values as Objects, or null if any input values are invalid.
     */
    private Object[] getArgsIfValid() {
        Object[] args = new Object[argViewIds.size()];
        for (int i = 0; i < argViewIds.size(); i++) {
            View argView = builderParts.findViewById(argViewIds.get(i));
            switch (currFieldType) {
                case BOOLEAN:
                    // There's no way that neither of the radio buttons are checked :)
                    RadioGroup rgFalseTrue = (RadioGroup) argView;
                    args[i] = rgFalseTrue.getCheckedRadioButtonId() != R.id.rb_false;
                    continue;
                case DATE:
                    DateInputView dateInputView = (DateInputView) argView;
                    if (!dateInputView.hasDate()) {
                        dateInputView.setError(getContext().getString(R.string.ruqus_error_empty_date));
                        return null;
                    }
                    args[i] = dateInputView.getDate();
                    continue;
                case DOUBLE:
                    EditText etDouble = (EditText) argView;
                    if (etDouble.length() == 0) {
                        etDouble.setError(getContext().getString(R.string.ruqus_error_empty_input));
                        return null;
                    }
                    args[i] = FieldType.parseNumberIfPossible(currFieldType, etDouble.getText().toString());
                    if (args[i] == null) {
                        etDouble.setError(getContext().getString(R.string.ruqus_error_out_of_range_double));
                        return null;
                    }
                    continue;
                case FLOAT:
                    EditText etFloat = (EditText) argView;
                    if (etFloat.length() == 0) {
                        etFloat.setError(getContext().getString(R.string.ruqus_error_empty_input));
                        return null;
                    }
                    args[i] = FieldType.parseNumberIfPossible(currFieldType, etFloat.getText().toString());
                    if (args[i] == null) {
                        etFloat.setError(getContext().getString(R.string.ruqus_error_out_of_range_float));
                        return null;
                    }
                    continue;
                case INTEGER:
                    EditText etInteger = (EditText) argView;
                    if (etInteger.length() == 0) {
                        etInteger.setError(getContext().getString(R.string.ruqus_error_empty_input));
                        return null;
                    }
                    args[i] = FieldType.parseNumberIfPossible(currFieldType, etInteger.getText().toString());
                    if (args[i] == null) {
                        etInteger.setError(getContext().getString(R.string.ruqus_error_out_of_range_integer));
                        return null;
                    }
                    continue;
                case LONG:
                    EditText etLong = (EditText) argView;
                    if (etLong.length() == 0) {
                        etLong.setError(getContext().getString(R.string.ruqus_error_empty_input));
                        return null;
                    }
                    args[i] = FieldType.parseNumberIfPossible(currFieldType, etLong.getText().toString());
                    if (args[i] == null) {
                        etLong.setError(getContext().getString(R.string.ruqus_error_out_of_range_long));
                        return null;
                    }
                    continue;
                case SHORT:
                    EditText etShort = (EditText) argView;
                    if (etShort.length() == 0) {
                        etShort.setError(getContext().getString(R.string.ruqus_error_empty_input));
                        return null;
                    }
                    args[i] = FieldType.parseNumberIfPossible(currFieldType, etShort.getText().toString());
                    if (args[i] == null) {
                        etShort.setError(getContext().getString(R.string.ruqus_error_out_of_range_short));
                        return null;
                    }
                    continue;
                case STRING:
                    EditText etString = (EditText) argView;
                    args[i] = etString.getText().toString();
                    if (((String) args[i]).isEmpty()) {
                        etString.setError(getContext().getString(R.string.ruqus_error_empty_input));
                        return null;
                    }
            }
        }
        return args;
    }

    /**
     * Get the condition builder arg views' contents as a string so that we can restore them later.
     * @return String which we have stashed our arg views' contents in.
     * @see #restoreArgViews(String)
     */
    private String stashableArgsString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < argViewIds.size(); i++) {
            View argView = builderParts.findViewById(argViewIds.get(i));
            switch (currFieldType) {
                case BOOLEAN:
                    builder.append(((RadioGroup) argView).getCheckedRadioButtonId() == R.id.rb_false
                            ? "false" : "true");
                    continue;
                case DATE:
                    builder.append(((DateInputView) argView).getText());
                    continue;
                case DOUBLE:
                case FLOAT:
                case INTEGER:
                case LONG:
                case SHORT:
                case STRING:
                    builder.append(((EditText) argView).getText().toString());
            }
            builder.append(ARG_STR_SEP);
        }
        if (builder.length() > 0) builder.delete(builder.lastIndexOf(ARG_STR_SEP), builder.length());
        return builder.toString();
    }

    /* Methods for sort builder mode. */

    /**
     * Called to set up the builder views for sort builder mode.
     * @param sortFields Current sort fields.
     * @param sortDirs   Current sort directions.
     */
    private void initSortBuilderMode(ArrayList<String> sortFields, ArrayList<Sort> sortDirs) {
        // Set up views.
        builderHeader.setText(R.string.ruqus_choose_sort_fields_title);
        addSortField.setEnabled(true);
        addSortField.setVisibility(VISIBLE);

        // Set up vars.
        sortSpinnerIds = new ArrayList<>();
        removeSortBtnIds = new ArrayList<>();
        sortDirRgIds = new ArrayList<>();

        // If present, add current sort fields.
        for (int i = 0; i < sortFields.size(); i++)
            addSortFieldView(currVisibleFlatFieldNames.indexOf(Ruqus.visibleFieldFromField(currClassName,
                    sortFields.get(i))), sortDirs.get(i));
    }

    /**
     * Called to restore the view hierarchy state if we were in sort builder mode prior to a configuration change.
     * <p/>
     * TODO doesn't correctly restore sort dirs, but does get the fields right...
     * @param sortFields Sort fields to restore.
     * @param sortDirs   Sort directions to restore.
     */
    private void restoreSortBuilderMode(ArrayList<String> sortFields, ArrayList<Sort> sortDirs) {
        initSortBuilderMode(sortFields, sortDirs);

        // Hide main view and show builder view.
        mainCont.setVisibility(GONE);
        builderScrollView.setVisibility(VISIBLE);
    }

    /**
     * Called to clean up the builder views when finishing sort builder mode.
     */
    private void tearDownSortBuilderMode() {
        // Clean up views.
        addSortField.setVisibility(GONE);
        builderParts.removeAllViews();
        builderScrollView.setVisibility(GONE);

        // Clean up vars.
        sortSpinnerIds = null;
        removeSortBtnIds = null;
        sortDirRgIds = null;
    }

    /**
     * Called to add a sort field view to {@link #builderParts} (and optionally pre-fill it).
     * @param selectedFieldPos Position in spinner to pre-select, or -1.
     * @param sortDir          Sort direction to pre-select, or null.
     */
    private void addSortFieldView(int selectedFieldPos, Sort sortDir) {
        final int idx = sortSpinnerIds.size();
        RelativeLayout sortPart = (RelativeLayout) View.inflate(getContext(), R.layout.sort_part, null);

        // Set label text.
        ((TextView) sortPart.findViewById(R.id.sort_field_label)).setText(
                getContext().getString(R.string.ruqus_sort_field_label, idx + 1));

        // Set up spinner.
        Spinner fieldSpinner = (Spinner) sortPart.findViewById(R.id.sort_field);
        fieldSpinner.setAdapter(makeFieldAdapter());
        fieldSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Make sure the user didn't select nothing.
                String selStr = (String) parent.getItemAtPosition(position);
                if (Ruqus.CHOOSE_FIELD.equals(selStr))
                    builderParts.findViewById(sortDirRgIds.get(idx)).setVisibility(GONE);
                else setSortDirOptions(idx, selStr);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                builderParts.findViewById(sortDirRgIds.get(idx)).setVisibility(GONE);
            }
        });

        // Set up remove button.
        ImageButton removeButton = (ImageButton) sortPart.findViewById(R.id.remove_field);
        DrawableCompat.setTint(removeButton.getDrawable(), theme == RuqusTheme.LIGHT ? Ruqus.DARK_TEXT_COLOR :
                Ruqus.LIGHT_TEXT_COLOR);
        removeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeSortField(idx);
            }
        });

        // Set up radio group.
        RadioGroup sortDirRg = (RadioGroup) sortPart.findViewById(R.id.rg_sort_dir);

        // Generate unique view IDs for the spinner, button, and radio group and set them.
        int fieldSpinnerId = Util.getUniqueViewId();
        int removeButtonId = Util.getUniqueViewId();
        int sortDirRgId = Util.getUniqueViewId();
        fieldSpinner.setId(fieldSpinnerId);
        removeButton.setId(removeButtonId);
        sortDirRg.setId(sortDirRgId);

        // Add IDs to lists.
        sortSpinnerIds.add(fieldSpinnerId);
        removeSortBtnIds.add(removeButtonId);
        sortDirRgIds.add(sortDirRgId);

        // If that was our third sort field, we disable the button that adds them.
        if (sortSpinnerIds.size() == 3) addSortField.setEnabled(false);

        // Add this to the builder container (Add one, since we want it added after the header view).
        builderParts.addView(sortPart);

        // Fill this sort field layout's views in if necessary.
        if (selectedFieldPos != -1) {
            // Select the correct item in the spinner.
            fieldSpinner.setSelection(selectedFieldPos);
            // Manually update the radio buttons' text if need be.
            String selStr = (String) fieldSpinner.getItemAtPosition(selectedFieldPos);
            if (Ruqus.CHOOSE_FIELD.equals(selStr)) sortDirRg.setVisibility(GONE);
            else setSortDirOptions(idx, selStr);
            // Select the correct radio button.
            sortDirRg.check(sortDir == Sort.ASCENDING ? R.id.asc : R.id.desc);
        }
    }

    /**
     * Called when a remove sort field button is clicked.
     */
    private void removeSortField(int index) {
        // Remove IDs from lists.
        sortSpinnerIds.remove(index);
        removeSortBtnIds.remove(index);
        sortDirRgIds.remove(index);

        // Remove from builder container (Add one to the index, since the header view is there).
        builderParts.removeViewAt(index);

        // Enable add button.
        addSortField.setEnabled(true);
    }

    /**
     * Called when the selection on a sort field spinner is changed.
     */
    private void setSortDirOptions(int index, String visibleFieldName) {
        String[] pretty = Ruqus.typeEnumForField(currClassName, Ruqus.fieldFromVisibleField(
                currClassName, visibleFieldName)).getPrettySortStrings();
        RadioGroup rg = (RadioGroup) builderParts.findViewById(sortDirRgIds.get(index));
        ((RadioButton) rg.findViewById(R.id.asc)).setText(pretty[0]);
        ((RadioButton) rg.findViewById(R.id.desc)).setText(pretty[1]);
        rg.setVisibility(VISIBLE);
    }

    /**
     * Get a list of sort fields which are currently chosen so we can restore them later.
     * @return List of currently selected sort fields.
     */
    private ArrayList<String> stashableSortFields() {
        ArrayList<String> sortFields = new ArrayList<>();
        for (Integer sortSpinnerId : sortSpinnerIds)
            sortFields.add(Ruqus.fieldFromVisibleField(currClassName,
                    (String) ((Spinner) builderParts.findViewById(sortSpinnerId)).getSelectedItem()));
        return sortFields;
    }

    /**
     * Get a list of sort directions which are currently chosen so we can restore them later.
     * @return List of currently selected sort directions.
     */
    private ArrayList<Sort> stashableSortDirections() {
        ArrayList<Sort> sortDirs = new ArrayList<>();
        for (Integer sortDirRgId : sortDirRgIds)
            sortDirs.add(((RadioGroup) builderParts.findViewById(sortDirRgId))
                    .getCheckedRadioButtonId() == R.id.asc ? Sort.ASCENDING : Sort.DESCENDING);
        return sortDirs;
    }

    /* State persistence. */

    /**
     * Helps us easily save and restore our view's state.
     */
    static class SavedState extends BaseSavedState {
        // General variables. Will always be written/read.
        RuqusTheme theme;
        RealmUserQuery ruq;
        Mode mode;

        // Condition builder variables. Will only be written/read if we're in condition builder mode.
        int currPartIdx;
        String currFieldName;
        String currTransName;
        String currArgsString;

        // Sort builder variables. Will only be written/read if we're in sort builder mode.
        ArrayList<String> currSortFields;
        ArrayList<Sort> currSortDirs;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            // Read general variables' values back.
            int tmpTheme = in.readInt();
            this.theme = tmpTheme == -1 ? null : RuqusTheme.values()[tmpTheme];
            this.ruq = in.readParcelable(RealmUserQuery.class.getClassLoader());
            int tmpMode = in.readInt();
            this.mode = tmpMode == -1 ? null : Mode.values()[tmpMode];
            if (this.mode == Mode.C_BUILD) {
                // If we were in condition builder mode, read those variables' values back.
                this.currPartIdx = in.readInt();
                this.currFieldName = in.readString();
                this.currTransName = in.readString();
                this.currArgsString = in.readString();
            } else if (this.mode == Mode.S_BUILD) {
                // If we were in sort builder mode, read those variables' values back.
                this.currSortFields = in.createStringArrayList();
                this.currSortDirs = new ArrayList<>();
                ArrayList<Integer> temp = new ArrayList<>();
                in.readList(temp, null);
                for (Integer sortOrdinal : temp) this.currSortDirs.add(Sort.values()[sortOrdinal]);
            }
        }

        @Override
        public int describeContents() { return 0; }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            // Write general variables' values.
            out.writeInt(this.theme == null ? -1 : this.theme.ordinal());
            out.writeParcelable(this.ruq, flags);
            out.writeInt(this.mode == null ? -1 : this.mode.ordinal());
            if (this.mode == Mode.C_BUILD) {
                // If we're in condition builder mode, write those variables' values.
                out.writeInt(this.currPartIdx);
                out.writeString(this.currFieldName);
                out.writeString(this.currTransName);
                out.writeString(this.currArgsString);
            } else if (this.mode == Mode.S_BUILD) {
                // If we're in sort builder mode, write those variables' values.
                out.writeStringList(this.currSortFields);
                ArrayList<Integer> temp = new ArrayList<>();
                for (Sort sortDir : this.currSortDirs) temp.add(sortDir.ordinal());
                out.writeList(temp);
            }
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {return new SavedState(in);}

            @Override
            public SavedState[] newArray(int size) {return new SavedState[size];}
        };
    }
}
