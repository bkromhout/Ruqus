package com.bkromhout.ruqus;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Responsible for executing a RealmUserQuery.
 */
class RUQExecutor<E extends RealmObject> {
    private Class<E> clazz;
    private RealmUserQuery ruq;

    static <E extends RealmObject> RUQExecutor<E> get(Class<E> clazz, RealmUserQuery ruq) {
        return new RUQExecutor<E>(clazz, ruq);
    }

    private RUQExecutor(Class<E> clazz, RealmUserQuery ruq) {
        this.clazz = clazz;
        this.ruq = ruq;
    }

    RealmResults<E> executeQuery() {
        // Ensure types on executor and query match.
        if (!clazz.getCanonicalName().equals(ruq.getQueryClass().getCanonicalName()))
            throw new IllegalArgumentException(String.format("Parameterized types of RUQExecutor and RealmUserQuery " +
                            "do not match! Executor's type: \"%s\", Query's type: \"%s\"", clazz.getCanonicalName(),
                    ruq.getQueryClass().getCanonicalName()));

        // Create the query using the default realm instance.
        RealmQuery<E> query = RealmQuery.createQuery(Realm.getDefaultInstance(), clazz);

        for (Condition condition : ruq.getConditions()) {
            // TODO
        }

        RealmResults<E> results = null; // TODO sorts, async, etc.
        return results;
    }
}
