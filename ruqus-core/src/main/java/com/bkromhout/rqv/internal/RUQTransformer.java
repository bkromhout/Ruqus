package com.bkromhout.rqv.internal;

import com.bkromhout.rqv.RealmUserQuery;
import io.realm.RealmObject;
import io.realm.RealmQuery;

/**
 * Extenders must provide a way to transform a {@link RealmUserQuery}.
 */
@Transformer
public abstract class RUQTransformer<T extends RealmObject> {

    /**
     * Called to transform a {@link RealmQuery} in some way.
     * @param realmQuery {@link RealmQuery} to transform.
     * @return Transformed {@link RealmQuery}.
     */
    abstract RealmQuery<T> transform(RealmQuery<T> realmQuery);
}
