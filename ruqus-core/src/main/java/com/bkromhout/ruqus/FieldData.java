package com.bkromhout.ruqus;

import java.util.ArrayList;

/**
 * Holds information about the fields of a class which extends {@link io.realm.RealmObject}.
 * @see ClassData
 */
public interface FieldData {
    /**
     * Get a list of real field names.
     * @return Real field names.
     */
    ArrayList<String> getFieldNames();

    /**
     * Get a list of visible field names.
     * @return Visible field names.
     */
    ArrayList<String> getVisibleNames();

    /**
     * Get the human-readable name of a field from its real field name.
     * @param realFieldName Real name of a field.
     * @return Human-readable field name.
     */
    String visibleNameOf(String realFieldName);

    /**
     * The class of the type for this field.
     * @param realFieldName Real field name.
     * @return Field type.
     */
    Class<?> fieldType(String realFieldName);

    /**
     * If {@link #isRealmListType(String)} returns true, this will return the class of the type of realm object that the
     * list contains.
     * @param realFieldName Real field name.
     * @return Field type, if this field is a {@link io.realm.RealmList}. Otherwise null.
     */
    Class<?> realmListType(String realFieldName);

    /**
     * @param realFieldName Real field name.
     * @return True if field type extends {@link io.realm.RealmObject}, otherwise false.
     */
    boolean isRealmObjectType(String realFieldName);

    /**
     * @param realFieldName Real field name.
     * @return True if field is a {@link io.realm.RealmList} type, otherwise false.
     */
    boolean isRealmListType(String realFieldName);
}
