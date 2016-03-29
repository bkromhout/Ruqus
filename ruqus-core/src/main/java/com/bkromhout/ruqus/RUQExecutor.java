package com.bkromhout.ruqus;

import io.realm.*;

import java.util.ArrayList;

/**
 * Responsible for executing a RealmUserQuery.
 */
class RUQExecutor<E extends RealmObject> {
    private final Class<E> clazz;
    private final RealmUserQuery ruq;

    static <E extends RealmObject> RUQExecutor<E> get(Class<E> clazz, RealmUserQuery ruq) {
        return new RUQExecutor<>(clazz, ruq);
    }

    private RUQExecutor(Class<E> clazz, RealmUserQuery ruq) {
        this.clazz = clazz;
        this.ruq = ruq;
    }

    RealmResults<E> executeQuery(Realm realm) {
        // Ensure types on executor and query match.
        if (!clazz.getCanonicalName().equals(ruq.getQueryClass().getCanonicalName()))
            throw new IllegalArgumentException(String.format("Parameterized types of RUQExecutor and RealmUserQuery " +
                            "do not match! Executor's type: \"%s\", Query's type: \"%s\"", clazz.getCanonicalName(),
                    ruq.getQueryClass().getCanonicalName()));

        // Create the query using the default realm instance and the query class.
        RealmQuery<E> query = RealmQuery.createQuery(realm, clazz);

        // Apply any conditions we have.
        for (Condition condition : ruq.getConditions()) {
            // Get transformer which will apply the condition.
            RUQTransformer transformer = Ruqus.getTransformer(condition.getTransformer());
            // And transform the query with it.
            transformer.transform(query, condition);
        }

        // Apply any sort fields we have and execute the query.
        ArrayList<String> sortFields = ruq.getSortFields();
        ArrayList<Sort> sortDirs = ruq.getSortDirs();
        if (sortFields.isEmpty())
            return query.findAll();
        else if (sortFields.size() == 1)
            return query.findAllSorted(sortFields.get(0), sortDirs.get(0));
        else if (sortFields.size() == 2)
            return query.findAllSorted(sortFields.get(0), sortDirs.get(0), sortFields.get(1), sortDirs.get(1));
        else if (sortFields.size() == 3)
            return query.findAllSorted(sortFields.get(0), sortDirs.get(0), sortFields.get(1), sortDirs.get(1),
                    sortFields.get(2), sortDirs.get(2));
        else
            return query.findAllSorted(sortFields.toArray(new String[sortFields.size()]),
                    sortDirs.toArray(new Sort[sortDirs.size()]));
    }
}
