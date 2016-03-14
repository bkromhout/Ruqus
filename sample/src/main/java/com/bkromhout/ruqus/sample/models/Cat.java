package com.bkromhout.ruqus.sample.models;

import com.bkromhout.ruqus.Queryable;
import io.realm.RealmObject;

@Queryable(name = "Cat")
public class Cat extends RealmObject {
    public String name;
}
