package com.bkromhout.ruqus;

import io.realm.RealmObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Holds information about all classes which extend {@link io.realm.RealmObject}.
 */
public abstract class ClassData {
    /**
     * Set of real names of classes which extend {@link RealmObject}.
     */
    protected static HashSet<String> realNames = new HashSet<>();
    /**
     * Set of real names of classes which were annotated with {@link Queryable}.
     */
    protected static HashSet<String> queryable = new HashSet<>();
    /**
     * Maps real class names to class objects.
     */
    protected static HashMap<String, Class<? extends RealmObject>> classMap = new HashMap<>();
    /**
     * Maps real class names to human-readable class names.
     */
    protected static HashMap<String, String> visibleNames = new HashMap<>();
    /**
     * Maps real class names to FieldData objects.
     */
    protected static HashMap<String, FieldData> fieldDatas = new HashMap<>();

    /**
     * Get a list of real class names.
     * @return List of real class names.
     */
    public ArrayList<String> getNames() {
        return new ArrayList<>(realNames);
    }

    /**
     * Get a list of human-readable names for classes.
     * @param queryableOnly If true, only include names of classes which were annotated with {@link Queryable}.
     * @return List of human-readable class names.
     */
    public ArrayList<String> getVisibleNames(boolean queryableOnly) {
        if (queryableOnly) {
            ArrayList<String> vNames = new ArrayList<>(queryable.size());
            for (String string : queryable) vNames.add(visibleNameOf(string));
            return vNames;
        } else {
            return new ArrayList<>(visibleNames.values());
        }
    }

    /**
     * Check that Ruqus recognizes and has data for a RealmObject subclass called {@code realName}.
     * @param realName Real name of a RealmObject subclass.
     * @return True if we know about the class with the given name, otherwise false.
     */
    public boolean isValidName(String realName) {
        return realNames.contains(realName);
    }

    /**
     * Get the actual class object for this class.
     * @return Class object.
     */
    public Class<? extends RealmObject> getClassObj(String realName) {
        return classMap.get(realName);
    }

    /**
     * Get the human-readable name for this class.
     * @param clazz Class.
     * @return Human-readable name.
     */
    public String visibleNameOf(Class<? extends RealmObject> clazz) {
        return visibleNameOf(clazz.getSimpleName());
    }

    /**
     * Get the human-readable name for this class.
     * @param realName Real class name.
     * @return Human-readable name.
     */
    public String visibleNameOf(String realName) {
        return visibleNames.get(realName);
    }

    /**
     * @param clazz Class
     * @return Whether or not the class was annotated with {@link Queryable}.
     */
    public boolean isQueryable(Class<? extends RealmObject> clazz) {
        return isQueryable(clazz.getSimpleName());
    }

    /**
     * @param realName Real class name.
     * @return Whether or not a class was annotated with {@link Queryable}.
     */
    public boolean isQueryable(String realName) {
        return queryable.contains(realName);
    }

    /**
     * Get FieldData for a class.
     * @param clazz Class to get field data for.
     * @return Class's field data.
     */
    public FieldData getFieldData(Class<? extends RealmObject> clazz) {
        return getFieldData(clazz.getSimpleName());
    }

    /**
     * Get FieldData for a class.
     * @param realName Real class name.
     * @return Class's field data.
     */
    public FieldData getFieldData(String realName) {
        return fieldDatas.get(realName);
    }
}
