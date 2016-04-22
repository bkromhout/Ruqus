package com.bkromhout.ruqus.transformers;

import com.bkromhout.ruqus.Condition;
import com.bkromhout.ruqus.FieldType;
import com.bkromhout.ruqus.RUQTransformer;
import com.bkromhout.ruqus.Transformer;
import io.realm.RealmModel;
import io.realm.RealmQuery;

import java.util.Date;

/**
 * Transformer which wraps the various {@link RealmQuery} {@code greaterThanOrEqualTo()} methods.
 */
@Transformer(name = Names.GREATER_THAN_OR_EQUAL_TO, validArgTypes = {Date.class, Double.class, Float.class,
                                                                     Integer.class, Long.class})
public class GreaterThanOrEqualTo extends RUQTransformer {
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

        // Use different methods based on field type.
        if (FieldType.DATE == fieldType)
            return realmQuery.greaterThanOrEqualTo(field, (Date) args[0]);
        else if (FieldType.DOUBLE == fieldType)
            return realmQuery.greaterThanOrEqualTo(field, (Double) args[0]);
        else if (FieldType.FLOAT == fieldType)
            return realmQuery.greaterThanOrEqualTo(field, (Float) args[0]);
        else if (FieldType.INTEGER == fieldType)
            return realmQuery.greaterThanOrEqualTo(field, (Integer) args[0]);
        else if (FieldType.LONG == fieldType)
            return realmQuery.greaterThanOrEqualTo(field, (Long) args[0]);
        else
            throw new IllegalArgumentException(String.format("Illegal argument type \"%s\".", fieldType.getTypeName()));
    }
}