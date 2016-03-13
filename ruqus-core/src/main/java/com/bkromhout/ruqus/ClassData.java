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

    /*
    [Occurs in second round so that we can use FieldData objects.]

    class ClassData$$GeneratedRuqusInfo extends ClassData {
        static {
            // [Real Class Name]
            realNames.add([Real Class Name]);
            classMap.put([Real Class Name], ...);
            queryable.add(...); // if necessary
            visibleNames.put([Real Class Name], ...);
            fieldDatas.put([Real Class Name], FieldData.getForClassName([Real Class Name]));
            ...
        }
    }
     */

    /**
     * Get a list of real class names.
     * @return List of real class names.
     */
    public static ArrayList<String> getClassNames() {
        return new ArrayList<>(realNames);
    }

    /**
     * Get a list of human-readable names for classes.
     * @param queryableOnly If true, only include names of classes which were annotated with {@link Queryable}.
     * @return List of human-readable class names.
     */
    public static ArrayList<String> getVisibleNames(boolean queryableOnly) {
        if (queryableOnly) {
            ArrayList<String> vNames = new ArrayList<>(queryable.size());
            for (String string : queryable) vNames.add(visibleNameOf(string));
            return vNames;
        } else {
            return new ArrayList<>(visibleNames.values());
        }
    }

    /**
     * Get the actual class object for this class.
     * @return Class object.
     */
    public static Class<? extends RealmObject> getClassObj(String realName) {
        return classMap.get(realName);
    }

    /**
     * Get the human-readable name for this class.
     * @param clazz Class.
     * @return Human-readable name.
     */
    public static String visibleNameOf(Class<? extends RealmObject> clazz) {
        return visibleNameOf(clazz.getCanonicalName());
    }

    /**
     * Get the human-readable name for this class.
     * @param realName Real class name.
     * @return Human-readable name.
     */
    public static String visibleNameOf(String realName) {
        return visibleNames.get(realName);
    }

    /**
     * @param clazz Class
     * @return Whether or not the class was annotated with {@link Queryable}.
     */
    public static boolean isQueryable(Class<? extends RealmObject> clazz) {
        return isQueryable(clazz.getCanonicalName());
    }

    /**
     * @param realName Real class name.
     * @return Whether or not a class was annotated with {@link Queryable}.
     */
    public static boolean isQueryable(String realName) {
        return queryable.contains(realName);
    }

    /**
     * Get FieldData for a class.
     * @param clazz Class to get field data for.
     * @return Class's field data.
     */
    public static FieldData getFieldData(Class<? extends RealmObject> clazz) {
        return getFieldData(clazz.getCanonicalName());
    }

    /**
     * Get FieldData for a class.
     * @param realName Real class name.
     * @return Class's field data.
     */
    public static FieldData getFieldData(String realName) {
        return fieldDatas.get(realName);
    }
}
