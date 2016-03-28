package com.bkromhout.ruqus.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bkromhout.ruqus.RealmUserQuery;
import com.bkromhout.ruqus.sample.models.Cat;
import com.bkromhout.ruqus.sample.models.Person;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {
    @Bind(R.id.query)
    TextView currQuery;
    @Bind(R.id.edit_query)
    Button editQuery;
    @Bind(R.id.results)
    LinearLayout resultsView;

    RealmUserQuery realmUserQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if (savedInstanceState != null && savedInstanceState.containsKey("RUQ"))
            realmUserQuery = savedInstanceState.getParcelable("RUQ");
        updateUi();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (realmUserQuery != null) outState.putParcelable("RUQ", realmUserQuery);
    }

    private void updateUi() {
        currQuery.setText(realmUserQuery == null ? "No query." : realmUserQuery.toString());
        if (realmUserQuery != null) {
            resultsView.removeAllViews();
            displayResults(realmUserQuery.execute(), realmUserQuery.getQueryClass());
        }
    }

    private <E extends RealmObject> void displayResults(RealmResults<E> results, Class<? extends RealmObject> clazz) {
        // TODO only adding one??
        String displayStr = "";
        if (Person.class.getCanonicalName().equals(clazz.getCanonicalName())) {
            for (int i = results.size() - 1; i >= 0; i--) {
                Person person = (Person) results.get(i);
                displayStr = String.valueOf(i + 1) + ":\n" + person.toString("");
            }
        } else if (Cat.class.getCanonicalName().equals(clazz.getCanonicalName())) {
            for (int i = results.size() - 1; i >= 0; i--) {
                Cat person = (Cat) results.get(i);
                displayStr = String.valueOf(i + 1) + ":\n" + person.toString("");
            }
        }
        TextView textView = new TextView(this);
        textView.setText(displayStr);
        resultsView.addView(textView, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            realmUserQuery = data.getParcelableExtra("RUQ");
            updateUi();
        }
    }

    @OnClick(R.id.edit_query)
    void onEditQClick() {
        if (realmUserQuery != null)
            startActivityForResult(new Intent(this, EditQueryActivity.class).putExtra("RUQ", realmUserQuery), 1);
        else startActivityForResult(new Intent(this, EditQueryActivity.class), 1);
    }
}
