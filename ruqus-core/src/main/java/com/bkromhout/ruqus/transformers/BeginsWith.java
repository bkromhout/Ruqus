package com.bkromhout.ruqus.transformers;

import com.bkromhout.ruqus.Condition;
import com.bkromhout.ruqus.FieldType;
import com.bkromhout.ruqus.RUQTransformer;
import com.bkromhout.ruqus.Transformer;
import io.realm.RealmObject;
import io.realm.RealmQuery;

/**
 * Transformer which wraps {@link RealmQuery#beginsWith(String, String)}.
 */
@Transformer(name = Names.BEGINS_WITH, validArgTypes = {String.class})
public class BeginsWith extends RUQTransformer {
    @Override
    public <T extends RealmObject> RealmQuery<T> transform(RealmQuery<T> realmQuery, Condition condition) {
        // Checks.
        if (!condition.isValid()) throw new IllegalArgumentException("Condition isn't valid.");
        if (condition.getType() != Condition.Type.NORMAL)
            throw new IllegalArgumentException("Condition type is not NORMAL.");

        // Get data from Conditions.
        String field = condition.getField();
        FieldType fieldType = condition.getFieldType();
        Object[] args = condition.getArgs();

        if (FieldType.STRING == fieldType) return realmQuery.beginsWith(field, (String) args[0]);
        else
            throw new IllegalArgumentException(String.format("Illegal argument type \"%s\".", fieldType.getTypeName()));
    }
}