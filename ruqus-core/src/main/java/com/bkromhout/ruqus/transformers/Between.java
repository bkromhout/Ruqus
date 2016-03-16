package com.bkromhout.ruqus.transformers;

import com.bkromhout.ruqus.Condition;
import com.bkromhout.ruqus.Transformer;
import com.bkromhout.ruqus.RUQTransformer;
import io.realm.RealmObject;
import io.realm.RealmQuery;

import java.util.Date;

/**
 * Transformer which wraps the various {@link RealmQuery} {@code between()} methods.
 */
@Transformer(name = Names.BETWEEN, numArgs = 2)
public class Between<T extends RealmObject> extends RUQTransformer<T> {
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

        // Use different methods based on field type.
        if (Names.DATE_CANON_NAME.equals(fieldTypeName))
            return realmQuery.between(field, (Date) args[0], (Date) args[1]);
        else if (Names.DOUBLE_CANON_NAME.equals(fieldTypeName))
            return realmQuery.between(field, (Double) args[0], (Double) args[1]);
        else if (Names.FLOAT_CANON_NAME.equals(fieldTypeName))
            return realmQuery.between(field, (Float) args[0], (Float) args[1]);
        else if (Names.INTEGER_CANON_NAME.equals(fieldTypeName))
            return realmQuery.between(field, (Integer) args[0], (Integer) args[1]);
        else if (Names.LONG_CANON_NAME.equals(fieldTypeName))
            return realmQuery.between(field, (Long) args[0], (Long) args[1]);
        else throw new IllegalArgumentException(String.format("Illegal argument type \"%s\".", fieldTypeName));
    }
}