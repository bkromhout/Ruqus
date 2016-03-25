package com.bkromhout.ruqus.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bkromhout.ruqus.RealmUserQuery;

public class MainActivity extends AppCompatActivity {
    @Bind(R.id.query)
    TextView currQuery;
    @Bind(R.id.edit_query)
    Button editQuery;

    RealmUserQuery realmUserQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        updateUi();
    }

    private void updateUi() {
        currQuery.setText(realmUserQuery == null ? "No query." : realmUserQuery.toString());
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
