package com.bkromhout.ruqus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import com.afollestad.materialdialogs.MaterialDialog;

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

    /**
     * Call to save the state of this view.
     * @param outState Bundle to save state to.
     */
    public void saveInstanceState(Bundle outState) {
        if (outState == null) return;
        // TODO theme, mode, currIdx, ruq string
    }

    /**
     * Call to restore the state of this view.
     * @param inState Bundle to restore state to.
     */
    public void restoreInstanceState(Bundle inState) {
        if (inState == null) return;
        // TODO
    }

    private void switchMode(Mode mode) {
        if ((this.mode == Mode.C_BUILD || this.mode == Mode.S_BUILD) && mode == Mode.MAIN) {
            // If we're switching back to main mode, remove all views from the builder container.
            builderCont.removeAllViews();
        }

        switch (mode) {
            case MAIN:

                break;
            case C_BUILD:

                break;
            case S_BUILD:

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
            add.setOutline1ClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAddOperatorClicked();
                }
            });
            add.setOutline2ClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAddConditionClicked();
                }
            });
            conditionsCont.addView(add);
        }
    }

    /**
     * Clears the view back to its initial state and sets {@link #ruq} to a new instance of {@link RealmUserQuery}.
     */
    private void reset() {
        // New RUQ.
        ruq = new RealmUserQuery();
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
                        setQueryable(text.toString());
                        ruq.setQueryClass(Ruqus.classNameFromVisibleName(text.toString()));
                    }
                })
                .show();
    }

    private void onAddOperatorClicked() {

    }

    private void onAddConditionClicked() {

    }

    private void onSortChooserClicked() {

    }

    /**
     * Called when the queryable class has been set. Only affects the view, not {@link #ruq}.
     * @param visibleName Visible name of the queryable class.
     */
    private void setQueryable(String visibleName) {
        // Reset.
        reset();
        // Set queryable chooser's card text and mode.
        queryableChooser.setCardText(visibleName);
        queryableChooser.setMode(RQVCard.Mode.CARD);
        // Append an add view to the conditions container.
        appendAddView();
        // Enable the conditions container and sort chooser.
        setConditionsAndSortEnabled(true);
    }

    private void setSorts() {
        // TODO.
    }
}
