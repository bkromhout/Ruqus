package com.bkromhout.ruqus.transformers;

import com.bkromhout.ruqus.FieldData;
import com.bkromhout.ruqus.internal.RUQTransformer;
import io.realm.RealmObject;
import io.realm.RealmQuery;

/**
 * Created by bkromhout on 3/13/16.
 */
public class EqualTo<T extends RealmObject> extends RUQTransformer<T> {
    @Override
    public RealmQuery<T> transform(RealmQuery<T> realmQuery, FieldData fieldData, Object... params) {
        return null;
    }
}
