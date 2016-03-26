package com.bkromhout.ruqus;

import android.view.View;
import android.widget.AdapterView;

/**
 * Stub interface which allows us to implement two listeners on one object.
 */
interface SpinnerInteractionListener extends AdapterView.OnItemSelectedListener, View.OnTouchListener {
}
