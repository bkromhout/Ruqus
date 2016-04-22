package com.bkromhout.ruqus.transformers;

import com.bkromhout.ruqus.Condition;
import com.bkromhout.ruqus.FieldType;
import com.bkromhout.ruqus.RUQTransformer;
import com.bkromhout.ruqus.Transformer;
import io.realm.RealmModel;
import io.realm.RealmQuery;

import java.util.Date;

/**
 * Transformer which wraps the various {@link RealmQuery} {@code between()} methods.
 */
@Transformer(name = Names.BETWEEN, numArgs = 2, validArgTypes = {Date.class, Double.class, Float.class, Integer.class,
                                                                 Long.class})
public class Between extends RUQTransformer {
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
            return realmQuery.between(field, (Date) args[0], (Date) args[1]);
        else if (FieldType.DOUBLE == fieldType)
            return realmQuery.between(field, (Double) args[0], (Double) args[1]);
        else if (FieldType.FLOAT == fieldType)
            return realmQuery.between(field, (Float) args[0], (Float) args[1]);
        else if (FieldType.INTEGER == fieldType)
            return realmQuery.between(field, (Integer) args[0], (Integer) args[1]);
        else if (FieldType.LONG == fieldType)
            return realmQuery.between(field, (Long) args[0], (Long) args[1]);
        else
            throw new IllegalArgumentException(String.format("Illegal argument type \"%s\".", fieldType.getTypeName()));
    }
}