package com.bkromhout.ruqus.transformers;

import com.bkromhout.ruqus.Condition;
import com.bkromhout.ruqus.Transformer;
import com.bkromhout.ruqus.RUQTransformer;
import io.realm.RealmObject;
import io.realm.RealmQuery;

/**
 * Transformer which wraps {@link RealmQuery#beginGroup()}.
 */
@Transformer(name = Names.BEGIN_GROUP, numArgs = 0, isNoArg = true)
public class BeginGroup<T extends RealmObject> extends RUQTransformer<T> {
    @Override
    public RealmQuery<T> transform(RealmQuery<T> realmQuery, Condition condition) {
        // Check condition.
        if (condition.getType() != Condition.Type.BEGIN_GROUP)
            throw new IllegalArgumentException("Condition type is not BEGIN_GROUP.");
        // Transform query.
        return realmQuery.beginGroup();
    }
}
