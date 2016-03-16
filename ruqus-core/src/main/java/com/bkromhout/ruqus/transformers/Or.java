package com.bkromhout.ruqus.transformers;

import com.bkromhout.ruqus.Condition;
import com.bkromhout.ruqus.RUQTransformer;
import com.bkromhout.ruqus.Transformer;
import io.realm.RealmObject;
import io.realm.RealmQuery;

/**
 * Transformer which wraps {@link RealmQuery#or()}.
 */
@Transformer(name = Names.OR, numArgs = 0, isNoArg = true)
public class Or<T extends RealmObject> extends RUQTransformer<T> {
    @Override
    public RealmQuery<T> transform(RealmQuery<T> realmQuery, Condition condition) {
        // Check condition.
        if (condition.getType() != Condition.Type.OR)
            throw new IllegalArgumentException("Condition type is not OR.");
        // Transform query.
        return realmQuery.or();
    }
}
