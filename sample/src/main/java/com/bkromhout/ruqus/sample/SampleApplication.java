package com.bkromhout.ruqus.sample;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
    private static SampleApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder(this).build());
        Ruqus.init(this);

        // Populate Realm if we haven't yet.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("hasPopulatedRealm", false)) {
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm tRealm) {
                    createDefaultRealmData(tRealm);
                }
            });
            realm.close();
            prefs.edit().putBoolean("hasPopulatedRealm", true).apply();
        }
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

        realm.copyToRealm(person1);
        realm.copyToRealm(person2);
        realm.copyToRealm(person3);
        realm.copyToRealm(person4);
        realm.copyToRealm(person5);
    }

    public static Context getAppCtx() {
        return instance.getApplicationContext();
    }
}
