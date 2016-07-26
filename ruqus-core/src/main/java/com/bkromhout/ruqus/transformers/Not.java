package com.bkromhout.ruqus.transformers;

import android.support.annotation.NonNull;
import com.bkromhout.ruqus.Condition;
import com.bkromhout.ruqus.RUQTransformer;
import com.bkromhout.ruqus.Transformer;
import io.realm.RealmModel;
import io.realm.RealmQuery;

/**
 * Transformer which wraps {@link RealmQuery#not()}.
 */
@Transformer(name = Names.NOT, numArgs = 0, isNoArgs = true, validArgTypes = {})
public class Not extends RUQTransformer {
    @Override
    public <T extends RealmModel> RealmQuery<T> transform(RealmQuery<T> realmQuery, Condition condition) {
        // Check condition.
        if (condition.getType() != Condition.Type.NOT)
            throw new IllegalArgumentException("Condition type is not NOT.");
        // Transform query.
        return realmQuery.not();
    }

    @Override
    public String makeReadableString(@NonNull Condition current, Condition previous, Condition next) {
        // Return the empty string unless the next operator is (, in which case return "not".
        return next != null && next.getType() == Condition.Type.BEGIN_GROUP ? Names.NOT : "";
    }
}
