package com.bkromhout.ruqus.internal;

import com.bkromhout.ruqus.FieldData;
import com.bkromhout.ruqus.RealmUserQuery;
import io.realm.RealmObject;
import io.realm.RealmQuery;

/**
 * Extenders must provide a way to transform a {@link RealmUserQuery}.
 */
@Transformer
public abstract class RUQTransformer<T extends RealmObject> {
    // No-args constructor.
    protected RUQTransformer() {}

    /**
     * Called to transform a {@link RealmQuery} in some way.
     * @param realmQuery {@link RealmQuery} to transform.
     * @return Transformed {@link RealmQuery}.
     */
    public abstract RealmQuery<T> transform(RealmQuery<T> realmQuery, FieldData fieldData);
}
