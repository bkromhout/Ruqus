package com.bkromhout.ruqus;

import io.realm.RealmModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Holds information about all classes which extend {@link io.realm.RealmModel}.
 */
abstract class ClassData {
    /**
     * Set of real names of classes which extend {@link RealmModel}.
     */
    protected static HashSet<String> realNames = new HashSet<>();
    /**
     * Set of real names of classes which were annotated with {@link Queryable}.
     */
    protected static HashSet<String> queryable = new HashSet<>();
    /**
     * Maps real class names to class objects.
     */
    protected static HashMap<String, Class<? extends RealmModel>> classMap = new HashMap<>();
    /**
     * Maps real class names to human-readable class names.
     */
    protected static HashMap<String, String> visibleNames = new HashMap<>();
    /**
     * Maps real class names to FieldData objects.
     */
    protected static HashMap<String, FieldData> fieldData = new HashMap<>();

    /**
     * Get a list of real class names.
     * @return List of real class names.
     */
    ArrayList<String> getNames() {
        return new ArrayList<>(realNames);
    }

    /**
     * Get a list of human-readable names for classes.
     * @param queryableOnly If true, only include names of classes which were annotated with {@link Queryable}.
     * @return List of human-readable class names.
     */
    ArrayList<String> getVisibleNames(boolean queryableOnly) {
        if (queryableOnly) {
            ArrayList<String> vNames = new ArrayList<>(queryable.size());
            for (String string : queryable) vNames.add(visibleNameOf(string));
            return vNames;
        } else {
            return new ArrayList<>(visibleNames.values());
        }
    }

    /**
     * Check that Ruqus recognizes and has data for a RealmModel subclass called {@code realName}.
     * @param realName Real name of a RealmModel subclass.
     * @return True if we know about the class with the given name, otherwise false.
     */
    boolean isValidName(String realName) {
        return realNames.contains(realName);
    }

    /**
     * Check that Ruqus recognizes and has data for a RealmModel subclass {@code clazz}.
     * @param clazz A RealmModel subclass.
     * @return True if we know about the class, otherwise false.
     */
    boolean isValidClass(Class<? extends RealmModel> clazz) {
        return classMap.values().contains(clazz);
    }

    /**
     * Get the actual class object for this class.
     * @return Class object.
     */
    Class<? extends RealmModel> getClassObj(String realName) {
        return classMap.get(realName);
    }

    /**
     * Get the human-readable name for this class.
     * @param clazz Class.
     * @return Human-readable name.
     */
    String visibleNameOf(Class<? extends RealmModel> clazz) {
        return visibleNameOf(clazz.getSimpleName());
    }

    /**
     * Get the human-readable name for this class.
     * @param realName Real class name.
     * @return Human-readable name.
     */
    String visibleNameOf(String realName) {
        return visibleNames.get(realName);
    }

    /**
     * @param clazz Class
     * @return Whether or not the class was annotated with {@link Queryable}.
     */
    boolean isQueryable(Class<? extends RealmModel> clazz) {
        return isQueryable(clazz.getSimpleName());
    }

    /**
     * @param realName Real class name.
     * @return Whether or not a class was annotated with {@link Queryable}.
     */
    boolean isQueryable(String realName) {
        return queryable.contains(realName);
    }

    /**
     * Get FieldData for a class.
     * @param clazz Class to get field data for.
     * @return Class's field data.
     */
    FieldData getFieldData(Class<? extends RealmModel> clazz) {
        return getFieldData(clazz.getSimpleName());
    }

    /**
     * Get FieldData for a class.
     * @param realName Real class name.
     * @return Class's field data.
     */
    FieldData getFieldData(String realName) {
        return fieldData.get(realName);
    }
}
