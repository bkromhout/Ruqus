package com.bkromhout.ruqus;

import io.realm.RealmModel;

import java.util.ArrayList;

/**
 * Holds information about the fields of a class which extends {@link io.realm.RealmModel}.
 * @see ClassData
 */
public abstract class FieldData {
    /**
     * Get a list of real field names.
     * @return Real field names.
     */
    abstract ArrayList<String> getFieldNames();

    /**
     * Get a list of visible field names.
     * @return Visible field names.
     */
    abstract ArrayList<String> getVisibleNames();

    /**
     * Get the human-readable name of a field from its real field name.
     * @param realFieldName Real name of a field.
     * @return Human-readable field name.
     */
    abstract String visibleNameOf(String realFieldName);

    /**
     * The class of the type for this field.
     * @param realFieldName Real field name.
     * @return Field type.
     */
    abstract Class<?> fieldType(String realFieldName);

    /**
     * If {@link #isRealmListType(String)} returns true, this will return the class of the type of realm object that the
     * list contains.
     * @param realFieldName Real field name.
     * @return Field type, if this field is a {@link io.realm.RealmList}. Otherwise null.
     */
    abstract Class<? extends RealmModel> realmListType(String realFieldName);

    /**
     * Whether or not the field whose name is {@code realFieldName} exists.
     * @param realFieldName Field name to check for.
     * @return True if field exists, otherwise false.
     */
    abstract boolean hasField(String realFieldName);

    /**
     * @param realFieldName Real field name.
     * @return True if field type extends {@link io.realm.RealmModel}, otherwise false.
     */
    abstract boolean isRealmModelType(String realFieldName);

    /**
     * @param realFieldName Real field name.
     * @return True if field is a {@link io.realm.RealmList} type, otherwise false.
     */
    abstract boolean isRealmListType(String realFieldName);
}
