package com.bkromhout.ruqus;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

/**
 * RealmQueryView
 * @author bkromhout
 */
public class RealmQueryView extends RelativeLayout {
    // Views.
    private RQVCard queryableChooser;
    private ScrollView scrollView;
    private LinearLayout conditionsCont;
    private RQVCard sortChooser;

    /**
     * Current theme type.
     */
    private RuqusTheme theme = null;
    /**
     * Current user query.
     */
    private RealmUserQuery ruq;

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
        queryableChooser = (RQVCard) findViewById(R.id.queryable_type);
        scrollView = (ScrollView) findViewById(R.id.rqv_scroll_view);
        conditionsCont = (LinearLayout) findViewById(R.id.rqv_content);
        sortChooser = (RQVCard) findViewById(R.id.sort_type);
        initAttrs(context, attrs);

        // Initialize UI.
        initUi();
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

        // TODO
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

    private void onQueryableChooserClicked() {

    }

    private void onAddOperatorClicked() {

    }

    private void onAddConditionClicked() {

    }

    private void onSortChooserClicked() {

    }
}
