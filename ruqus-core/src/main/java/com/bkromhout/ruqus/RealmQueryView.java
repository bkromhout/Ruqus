package com.bkromhout.ruqus;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.bkromhout.rqv.R;

/**
 * RealmQueryView
 * @author bkromhout
 */
public class RealmQueryView extends RelativeLayout {
    enum RuqusTheme {
        LIGHT, DARK
    }

    /**
     * Contains query UI.
     */
    private LinearLayout rqvContent;

    private RuqusTheme themeRes;

    public RealmQueryView(Context context) {
        this(context, null);
    }

    public RealmQueryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * Initialize our view.
     * @param context Context to use.
     * @param attrs   Attributes.
     */
    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.realm_query_view, this);
        initAttrs(context, attrs);

        rqvContent = (LinearLayout) findViewById(R.id.rqv_content);
    }

    /**
     * Initializes the view using the given attributes.
     * @param context Context to use.
     * @param attrs   Attributes.
     */
    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RealmQueryView);

        // Get theme, default to light.
        themeRes = typedArray.getResourceId(R.styleable.RealmQueryView_ruqus_theme, 0) == 0 ? RuqusTheme.LIGHT :
                RuqusTheme.DARK;

        typedArray.recycle();
    }
}
