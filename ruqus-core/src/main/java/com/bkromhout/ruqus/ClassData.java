package com.bkromhout.ruqus;

import io.realm.RealmObject;

import java.util.ArrayList;

/**
 * Holds information about all classes which extend {@link io.realm.RealmObject}.
 */
public interface ClassData {
    /*
    // Set of real names of classes which were annotated with @Queryable.
    private static final HashSet<String> queryable;

    // Maps real class names to class objects.
    private static final HashMap<String, Class<? extends RealmObject>> classMap;

    // Maps real class names to human-readable class names.
    private static final HashMap<String, String> visibleNames;

    private static final 
     */

    /**
     * Get a list of real class names.
     * @return List of real class names.
     */
    ArrayList<String> getClassNames();

    /**
     * Get a list of human-readable names for classes.
     * @param queryableOnly If true, only include names of classes which were annotated with {@link Queryable}.
     * @return List of human-readable class names.
     */
    ArrayList<String> getVisibleNames(boolean queryableOnly);

    /**
     * Get the actual class object for this class.
     * @return Class object.
     */
    Class<? extends RealmObject> getClassObj(String realName);

    /**
     * Get the human-readable name for this class.
     * @param clazz Class.
     * @return Human-readable name.
     */
    String visibleNameOf(Class<? extends RealmObject> clazz);

    /**
     * Get the human-readable name for this class.
     * @param realName Real class name.
     * @return Human-readable name.
     */
    String visibleNameOf(String realName);

    /**
     * @param clazz Class
     * @return Whether or not the class was annotated with {@link Queryable}.
     */
    boolean isQueryable(Class<? extends RealmObject> clazz);

    /**
     * @param realName Real class name.
     * @return Whether or not a class was annotated with {@link Queryable}.
     */
    boolean isQueryable(String realName);

    /**
     * Get FieldData for a class.
     * @param clazz Class to get field data for.
     * @return Class's field data.
     */
    FieldData getFieldData(Class<? extends RealmObject> clazz);

    /**
     * Get FieldData for a class.
     * @param realName Real class name.
     * @return Class's field data.
     */
    FieldData getFieldData(String realName);
}
