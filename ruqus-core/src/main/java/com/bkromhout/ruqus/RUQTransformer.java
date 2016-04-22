package com.bkromhout.ruqus;

import io.realm.RealmModel;
import io.realm.RealmQuery;

/**
 * Extenders must provide a way to transform a {@link RealmUserQuery} and also must be annotated with {@link
 * Transformer}.
 * <p>
 * Note that Ruqus creates transformers using no-arg constructors.
 */
public abstract class RUQTransformer {
    // No-args constructor.
    protected RUQTransformer() {}

    /**
     * Called to transform a {@link RealmQuery} in some way.
     * @param realmQuery {@link RealmQuery} to transform.
     * @param condition  The {@link Condition} which this transformation is being applied to enforce.
     * @return Transformed {@link RealmQuery}.
     */
    public abstract <T extends RealmModel> RealmQuery<T> transform(RealmQuery<T> realmQuery, Condition condition);
}
