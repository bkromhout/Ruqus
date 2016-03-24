package com.bkromhout.ruqus.sample.models;

import com.bkromhout.ruqus.Hide;
import com.bkromhout.ruqus.Queryable;
import com.bkromhout.ruqus.VisibleAs;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;

@Queryable(name = "Person")
public class Person extends RealmObject {
    @VisibleAs(string = "Name")
    private String name;

    @VisibleAs(string = "Age")
    private int age;

    @VisibleAs(string = "Dog")
    private Dog dog;

    @VisibleAs(string = "Cat")
    private RealmList<Cat> cats;

    @Ignore
    private int tempReference;

    @Hide
    private long id;

    public Person() {}

    public Person(String name, int age, Dog dog, long id) {
        this.name = name;
        this.age = age;
        this.dog = dog;
        this.cats = new RealmList<>();
        this.id = id;
    }

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
