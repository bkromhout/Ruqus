package com.bkromhout.ruqus;

import java.util.ArrayList;

/**
 * Holds information about the fields of a class which extends {@link io.realm.RealmObject}.
 * @see ClassData
 */
public abstract class FieldData {
    /**
     * Get a list of real field names.
     * @return Real field names.
     */
    public abstract ArrayList<String> getFieldNames();

    /**
     * Get a list of visible field names.
     * @return Visible field names.
     */
    public abstract ArrayList<String> getVisibleNames();

    /**
     * Get the human-readable name of a field from its real field name.
     * @param realFieldName Real name of a field.
     * @return Human-readable field name.
     */
    public abstract String visibleNameOf(String realFieldName);

    /**
     * The class of the type for this field.
     * @param realFieldName Real field name.
     * @return Field type.
     */
    public abstract Class<?> fieldType(String realFieldName);

    /**
     * If {@link #isRealmListType(String)} returns true, this will return the class of the type of realm object that the
     * list contains.
     * @param realFieldName Real field name.
     * @return Field type, if this field is a {@link io.realm.RealmList}. Otherwise null.
     */
    public abstract Class<?> realmListType(String realFieldName);

    /**
     * @param realFieldName Real field name.
     * @return True if field type extends {@link io.realm.RealmObject}, otherwise false.
     */
    public abstract boolean isRealmObjectType(String realFieldName);

    /**
     * @param realFieldName Real field name.
     * @return True if field is a {@link io.realm.RealmList} type, otherwise false.
     */
    public abstract boolean isRealmListType(String realFieldName);

    /**
     * Get the FieldData object which was generated for the class with the given name.
     * <p>
     * This method uses reflection, for a cached version please call {@link ClassData#getFieldData(String)} instead.
     * @param realClassName Real class name.
     * @return FieldData object.
     */
    static FieldData getForClassName(String realClassName) {
        try {
            return (FieldData) Class.forName(C.GEN_PKG_PREFIX + realClassName + C.FIELD_DATA_SUFFIX).newInstance();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Could not find generated Ruqus field data for " + realClassName +
                    ", did the annotation processor run?");
        } catch (Exception e) {
            throw new IllegalStateException("Could not get generated Ruqus field data for" + realClassName + ".");
        }
    }
}
