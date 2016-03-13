package com.bkromhout.ruqus;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import com.bkromhout.rqv.R;

/**
 * RealmQueryView
 * @author bkromhout
 */
public class RealmQueryView extends ScrollView {
    /**
     * Contains query UI.
     */
    private LinearLayout rqvContent;

    public RealmQueryView(Context context) {
        super(context);
        init(context, null);
    }

    public RealmQueryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RealmQueryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Initialize our view.
     * @param context Context to use.
     * @param attrs   Attributes.
     */
    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.realm_query_view, this);

        rqvContent = (LinearLayout) findViewById(R.id.rqv_content);
    }
}
