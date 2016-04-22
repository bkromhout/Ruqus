package com.bkromhout.ruqus.sample;

import android.app.Application;
import com.bkromhout.ruqus.Ruqus;
import com.bkromhout.ruqus.sample.models.Bone;
import com.bkromhout.ruqus.sample.models.Cat;
import com.bkromhout.ruqus.sample.models.Dog;
import com.bkromhout.ruqus.sample.models.Person;
import io.realm.Realm;
import io.realm.RealmConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Application Class.
 */
public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder(this)
                .deleteRealmIfMigrationNeeded()
                .initialData(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        createDefaultRealmData(realm);
                    }
                })
                .build());
        Ruqus.init(this);
    }

    /**
     * Fill in Realm with some default data.
     */
    private void createDefaultRealmData(Realm realm) {
        List<Bone> bones1 = new ArrayList<Bone>() {{
            add(new Bone("Geoff", false));
            add(new Bone("Larry", true));
            add(new Bone("Alice", true));
            add(new Bone("Bone", false));
        }};
        List<Bone> bones2 = new ArrayList<Bone>() {{
            add(new Bone("Bob", false));
            add(new Bone("Huh?", true));
            add(new Bone("What?", false));
        }};

        Dog dog1 = new Dog("Good Boy", 7, bones1);
        Dog dog2 = new Dog("Freddy", 2, bones2);
        Dog dog3 = new Dog("Such Name, Much Excite, Wow", 900, null, true);
        Dog dog4 = new Dog("Otie", 4);

        Cat cat1 = new Cat("Grumpy Cat", dog3);
        Cat cat2 = new Cat("Tiger", null);
        Cat cat3 = new Cat("Mary-Sue", dog1);
        Cat cat4 = new Cat("Garfield", dog4);
        Cat cat5 = new Cat("Schrodinger's Cat");
        Cat cat6 = new Cat("I Can Has Cheezburger", dog3);

        Person person1 = new Person("Jon Arbuckle", 30, dog4, 1);
        person1.getCats().add(cat4);

        Person person2 = new Person("Erwin Schrodinger", 128, dog2, 2);
        person2.getCats().add(cat5);

        Person person3 = new Person("Le Meme", 9001, dog3, 3);
        person3.getCats().add(cat1);
        person3.getCats().add(cat6);

        Person person4 = new Person("Jane Harrison", 28, dog1, 4);
        person4.getCats().add(cat2);
        person4.getCats().add(cat3);

        Person person5 = new Person("Alex Harrison", 29, dog1, 5);
        person5.getCats().add(cat2);
        person5.getCats().add(cat3);

        ArrayList<Person> people = new ArrayList<>();
        people.add(person1);
        people.add(person2);
        people.add(person3);
        people.add(person4);
        people.add(person5);

        realm.copyToRealm(people);
    }
}
