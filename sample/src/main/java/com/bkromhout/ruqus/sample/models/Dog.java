package com.bkromhout.ruqus.sample.models;

import io.realm.RealmList;
import io.realm.RealmObject;

import java.util.List;

public class Dog extends RealmObject {
    public String name;

    public int age;

    public RealmList<Bone> bones;

    public boolean isDoge;

    public Dog() {}

    public Dog(String name, int age) {
        this(name, age, null, false);
    }

    public Dog(String name, int age, List<Bone> bones) {
        this(name, age, bones, false);
    }

    public Dog(String name, int age, List<Bone> bones, boolean isDoge) {
        this.name = name;
        this.age = age;
        this.bones = new RealmList<>();
        if (bones != null) this.bones.addAll(bones);
        this.isDoge = isDoge;
    }

    public String toString(String indent) {
        String s = indent + "Name: " + name + ",\n" +
                indent + "Age: " + age + ",\n" +
                indent + "Bones: [\n";
        for (Bone bone : bones) {
            s += indent + "\t" + "{\n" + bone.toString(indent + "\t") + indent + "\t" + "},\n";
        }
        s += indent + "],\n" +
                indent + "Is Doge: " + isDoge + "\n";
        return s;
    }
}
