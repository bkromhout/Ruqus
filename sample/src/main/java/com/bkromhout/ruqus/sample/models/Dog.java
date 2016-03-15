package com.bkromhout.ruqus.sample.models;

import com.bkromhout.ruqus.VisibleAs;
import io.realm.RealmObject;

public class Dog extends RealmObject {
    @VisibleAs(string = "Name")
    public String name;

    @VisibleAs(string = "Age")
    public int age;
}
