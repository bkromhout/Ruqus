package com.bkromhout.ruqus.sample.models;

import com.bkromhout.rqv.sample.R;
import com.bkromhout.ruqus.Queryable;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;

@Queryable(name = R.string.person_name)
public class Person extends RealmObject {
    private String name;
    private int age;

    private Dog dog;

    private RealmList<Cat> cats;

    @Ignore
    private int tempReference;

    private long id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Dog getDog() {
        return dog;
    }

    public void setDog(Dog dog) {
        this.dog = dog;
    }

    public RealmList<Cat> getCats() {
        return cats;
    }

    public void setCats(RealmList<Cat> cats) {
        this.cats = cats;
    }

    public int getTempReference() {
        return tempReference;
    }

    public void setTempReference(int tempReference) {
        this.tempReference = tempReference;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
