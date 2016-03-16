package com.bkromhout.ruqus.transformers;

import com.bkromhout.ruqus.Condition;
import com.bkromhout.ruqus.Transformer;
import com.bkromhout.ruqus.RUQTransformer;
import io.realm.RealmObject;
import io.realm.RealmQuery;

/**
 * Transformer which wraps {@link RealmQuery#endGroup()}.
 */
@Transformer(name = Names.END_GROUP, numArgs = 0, isNoArg = true)
public class EndGroup<T extends RealmObject> extends RUQTransformer<T> {
    @Override
    public RealmQuery<T> transform(RealmQuery<T> realmQuery, Condition condition) {
        // Check condition.
        if (condition.getType() != Condition.Type.END_GROUP)
            throw new IllegalArgumentException("Condition type is not END_GROUP.");
        // Transform query.
        return realmQuery.endGroup();
    }
}
