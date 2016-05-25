package com.bkromhout.ruqus.sample;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.bkromhout.ruqus.RealmQueryView;
import com.bkromhout.ruqus.RealmUserQuery;

public class EditQueryActivity extends AppCompatActivity implements RealmQueryView.ModeListener {
    @Bind(R.id.rqv)
    RealmQueryView rqv;

    RealmQueryView.Mode rqvMode = RealmQueryView.Mode.MAIN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_edit_query);
        ButterKnife.bind(this);

        Bundle b = getIntent().getExtras();
        if (b != null && b.containsKey("RUQ")) rqv.setRealmUserQuery((RealmUserQuery) b.getParcelable("RUQ"));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++)
            menu.getItem(i).getIcon().setColorFilter(ContextCompat.getColor(this, android.R.color.white),
                    PorterDuff.Mode.SRC_IN);
        menu.findItem(R.id.action_save).setVisible(rqvMode == RealmQueryView.Mode.MAIN);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_query, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (!rqv.isQueryValid()) Toast.makeText(this, R.string.err_invalid_query, Toast.LENGTH_LONG).show();
                else {
                    setResult(RESULT_OK, new Intent().putExtra("RUQ", rqv.getRealmUserQuery()));
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        rqv.setModeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        rqv.clearModeListener();
    }

    @Override
    public void onBackPressed() {
        if (!rqv.leaveBuilderMode()) super.onBackPressed();
    }

    @Override
    public void rqvModeChanged(RealmQueryView.Mode newMode) {
        rqvMode = newMode;
        invalidateOptionsMenu();
    }
}
