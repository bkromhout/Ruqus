package com.bkromhout.ruqus.transformers;

import android.support.annotation.NonNull;
import com.bkromhout.ruqus.Condition;
import com.bkromhout.ruqus.RUQTransformer;
import com.bkromhout.ruqus.Transformer;
import io.realm.RealmModel;
import io.realm.RealmQuery;

/**
 * Transformer which wraps {@link RealmQuery#or()}.
 */
@Transformer(name = Names.OR, numArgs = 0, isNoArgs = true, validArgTypes = {})
public class Or extends RUQTransformer {
    @Override
    public <T extends RealmModel> RealmQuery<T> transform(RealmQuery<T> realmQuery, Condition condition) {
        // Check condition.
        if (condition.getType() != Condition.Type.OR)
            throw new IllegalArgumentException("Condition type is not OR.");
        // Transform query.
        return realmQuery.or();
    }

    @Override
    public String makeReadableString(@NonNull Condition current, Condition previous, Condition next) {
        // Since this is appended in a similar fashion to "and" (that is, not surrounded by spaces), just return "or".
        return Names.OR;
    }
}
