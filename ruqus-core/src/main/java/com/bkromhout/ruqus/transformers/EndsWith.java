package com.bkromhout.ruqus.transformers;

import com.bkromhout.ruqus.Condition;
import com.bkromhout.ruqus.FieldType;
import com.bkromhout.ruqus.RUQTransformer;
import com.bkromhout.ruqus.Transformer;
import io.realm.RealmModel;
import io.realm.RealmQuery;

/**
 * Transformer which wraps {@link RealmQuery#endsWith(String, String)}.
 */
@Transformer(name = Names.ENDS_WITH, validArgTypes = {String.class})
public class EndsWith extends RUQTransformer {
    @Override
    public <T extends RealmModel> RealmQuery<T> transform(RealmQuery<T> realmQuery, Condition condition) {
        // Checks.
        if (!condition.isValid()) throw new IllegalArgumentException("Condition isn't valid.");
        if (condition.getType() != Condition.Type.NORMAL)
            throw new IllegalArgumentException("Condition type is not NORMAL.");

        // Get data from Conditions.
        String field = condition.getField();
        FieldType fieldType = condition.getFieldType();
        Object[] args = condition.getArgs();

        if (FieldType.STRING == fieldType) return realmQuery.endsWith(field, (String) args[0]);
        else
            throw new IllegalArgumentException(String.format("Illegal argument type \"%s\".", fieldType.getTypeName()));
    }
}