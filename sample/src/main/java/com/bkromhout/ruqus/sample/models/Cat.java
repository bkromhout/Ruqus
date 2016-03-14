package com.bkromhout.ruqus.sample.models;

import com.bkromhout.rqv.sample.R;
import com.bkromhout.ruqus.Queryable;
import io.realm.RealmObject;

@Queryable(name = R.string.cat_name)
public class Cat extends RealmObject {
    public String name;
}
