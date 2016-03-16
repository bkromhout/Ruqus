package com.bkromhout.ruqus.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bkromhout.ruqus.Ruqus;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.edit_query)
    Button editQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.edit_query)
    void onEditQClick() {
        Log.d("Ruqus Test Call", String.format("Size of class data: %d, Size of normal transformer data: %d, " +
                "Size of no-arg transformer data: %d\n", Ruqus.getClassData().getNames().size(),
                Ruqus.getTransformerData().getNames().size(), Ruqus.getTransformerData().getNoArgNames().size()));
        startActivity(new Intent(this, EditQueryActivity.class));
    }
}
