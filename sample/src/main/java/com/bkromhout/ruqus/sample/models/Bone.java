package com.bkromhout.ruqus.sample.models;

import com.bkromhout.ruqus.VisibleAs;
import io.realm.RealmObject;

/**
 * Bones. You know, for {@link Dog}s.
 */
public class Bone extends RealmObject {
    @VisibleAs(string = "Bone Name")
    public String boneName;

    @VisibleAs(string = "Is Favorite")
    public boolean isFavorite;

    public Bone() {}

    public Bone(String boneName, boolean isFavorite) {
        this.boneName = boneName;
        this.isFavorite = isFavorite;
    }
}
