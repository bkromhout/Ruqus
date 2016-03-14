package com.bkromhout.ruqus.internal;

import com.squareup.javapoet.JavaFile;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Helps generate *$$RuqusFieldData classes.
 */
public class FieldDataBuilder {
    HashSet<String> realNames;
    HashMap<String, String> visibleNames;
    HashMap<String, Class<?>> types;
    HashMap<String, Class<?>> realmListTypes;

    FieldDataBuilder() {
        realNames = new HashSet<>();
        visibleNames = new HashMap<>();
        types = new HashMap<>();
        realmListTypes = new HashMap<>();
    }

    void addField(String realName, String visibleName, Class<?> type, Class<?> realmListType) {
        if (realNames.add(realName)) {
            visibleNames.put(realName, visibleName);
            types.put(realName, type);
            if (realmListType != null) realmListTypes.put(realName, realmListType);
        }
    }

    JavaFile brewJava() {
        // TODO.
        return null;
    }
}
