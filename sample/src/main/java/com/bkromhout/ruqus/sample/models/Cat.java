package com.bkromhout.ruqus.sample.models;

import com.bkromhout.ruqus.Queryable;
import com.bkromhout.ruqus.VisibleAs;
import io.realm.RealmObject;

@Queryable(name = "Cat")
public class Cat extends RealmObject {
    public String name;

    @VisibleAs(string = "Least Favorite Dog")
    public Dog leastFavorite;

    public Cat() {}

    public Cat(String name) {
        this.name = name;
        this.leastFavorite = null;
    }

    public Cat(String name, Dog leastFavorite) {
        this.name = name;
        this.leastFavorite = leastFavorite;
    }

    public String toString(String indent) {
        return indent + "Name: " + name + ",\n" +
                indent + "Least Favorite Dog: {\n" +
                (leastFavorite != null ? leastFavorite.toString(indent + "\t") + indent : "") + "}\n";
    }
}
