package com.bkromhout.ruqus.sample.models;

import com.bkromhout.ruqus.Queryable;
import com.bkromhout.ruqus.VisibleAs;
import io.realm.RealmObject;

@Queryable(name = "Cat")
public class Cat extends RealmObject {
    @VisibleAs(string = "Name")
    public String name;
}
