package com.bkromhout.ruqus.sample.models;

import io.realm.RealmObject;

/**
 * Bones. You know, for {@link Dog}s.
 */
public class Bone extends RealmObject {
    public String boneName;

    public boolean isFavorite;

    public Bone() {}

    public Bone(String boneName, boolean isFavorite) {
        this.boneName = boneName;
        this.isFavorite = isFavorite;
    }

    public String toString(String indent) {
        return indent + "Bone Name: " + boneName + ",\n" +
                indent + "Is Favorite: " + isFavorite + "\n";
    }
}
