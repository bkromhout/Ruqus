package com.bkromhout.ruqus;

import io.realm.RealmList;
import io.realm.RealmObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by bkromhout on 3/13/16.
 */
public class TestExt extends FieldData {
    private static HashSet<String> realNames;
    private static HashMap<String, String> visibleNames;
    private static HashMap<String, Class<?>> types;
    private static HashMap<String, Class<? extends RealmObject>> realmListTypes;

    static {
        realNames = new HashSet<>();
        visibleNames = new HashMap<>();
        types = new HashMap<>();
        realmListTypes = new HashMap<>();

        realNames.add("realName1");
        visibleNames.put("realName1", "Real Name 1");
        types.put("realName1", String.class);
    }

    @Override
    public final ArrayList<String> getFieldNames() {
        return new ArrayList<>(realNames);
    }

    @Override
    public final ArrayList<String> getVisibleNames() {
        return new ArrayList<>(visibleNames.values());
    }

    @Override
    public final String visibleNameOf(String realFieldName) {
        return visibleNames.get(realFieldName);
    }

    @Override
    public final Class<?> fieldType(String realFieldName) {
        return types.get(realFieldName);
    }

    @Override
    public final Class<?> realmListType(String realFieldName) {
        return realmListTypes.get(realFieldName);
    }

    @Override
    public final boolean isRealmObjectType(String realFieldName) {
        return fieldType(realFieldName).isInstance(RealmObject.class);
    }

    @Override
    public final boolean isRealmListType(String realFieldName) {
        return fieldType(realFieldName).isInstance(RealmList.class);
    }
}
