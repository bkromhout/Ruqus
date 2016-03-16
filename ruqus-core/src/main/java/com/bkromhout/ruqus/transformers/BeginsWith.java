package com.bkromhout.ruqus.transformers;

import com.bkromhout.ruqus.Condition;
import com.bkromhout.ruqus.Transformer;
import com.bkromhout.ruqus.RUQTransformer;
import io.realm.RealmObject;
import io.realm.RealmQuery;

/**
 * Transformer which wraps {@link RealmQuery#beginsWith(String, String)}.
 */
@Transformer(name = Names.BEGINS_WITH, validArgTypes = {String.class})
public class BeginsWith<T extends RealmObject> extends RUQTransformer<T> {
    @Override
    public RealmQuery<T> transform(RealmQuery<T> realmQuery, Condition condition) {
        // Checks.
        if (!condition.isValid()) throw new IllegalArgumentException("Condition isn't valid.");
        if (condition.getType() != Condition.Type.NORMAL)
            throw new IllegalArgumentException("Condition type is not NORMAL.");

        // Get data from Conditions.
        String field = condition.getField();
        String fieldTypeName = condition.getFieldType().getCanonicalName();
        Object[] args = condition.getArgs();

        if (Names.STRING_CANON_NAME.equals(fieldTypeName)) return realmQuery.beginsWith(field, (String) args[0]);
        else throw new IllegalArgumentException(String.format("Illegal argument type \"%s\".", fieldTypeName));
    }
}