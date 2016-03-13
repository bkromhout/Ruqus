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
    public static final int MULTIPLE = -1;

    // No-args constructor.
    protected RUQTransformer() {}

    /**
     * How many parameters the user will be able to input into this transformer (not including the field itself). By
     * default this is 1, but developers can override it to return either a higher number, or {@link #MULTIPLE} for an
     * array.
     * @return Number of parameters user will be able to enter.
     */
    public int getNumParams() {
        return 1;
    }

    /**
     * Called to transform a {@link RealmQuery} in some way.
     * @param realmQuery {@link RealmQuery} to transform.
     * @param fieldData  Data about the field which will be used when transforming the {@link RealmQuery}. May be null
     *                   if unnecessary.
     * @param params     Some parameters. What each one is and is for is depends on the method implementation.
     * @return Transformed {@link RealmQuery}.
     */
    public abstract RealmQuery<T> transform(RealmQuery<T> realmQuery, FieldData fieldData, Object... params);
}
