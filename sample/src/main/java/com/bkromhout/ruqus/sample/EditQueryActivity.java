package com.bkromhout.ruqus.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bkromhout.ruqus.RealmQueryView;
import com.bkromhout.ruqus.RealmUserQuery;

public class EditQueryActivity extends AppCompatActivity {
    @Bind(R.id.rqv)
    RealmQueryView rqv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_edit_query);
        ButterKnife.bind(this);

        Bundle b = getIntent().getExtras();
        if (b != null && b.containsKey("RUQ")) rqv.setRealmUserQuery((RealmUserQuery) b.getParcelable("RUQ"));
    }

    @OnClick(R.id.save)
    void save() {
        if (!rqv.isQueryValid()) Toast.makeText(this, R.string.err_invalid_query, Toast.LENGTH_LONG).show();
        else {
            setResult(RESULT_OK, new Intent().putExtra("RQV", rqv.getRealmUserQuery()));
            finish();
        }
    }
}
