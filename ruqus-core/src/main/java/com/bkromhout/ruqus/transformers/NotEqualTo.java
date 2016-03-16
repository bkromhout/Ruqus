package com.bkromhout.ruqus.transformers;

import com.bkromhout.ruqus.Condition;
import com.bkromhout.ruqus.RUQTransformer;
import com.bkromhout.ruqus.Transformer;
import io.realm.RealmObject;
import io.realm.RealmQuery;

import java.util.Date;

/**
 * Transformer which wraps the various {@link RealmQuery} {@code notEqualTo()} methods.
 */
@Transformer(name = Names.NOT_EQUAL_TO, validArgTypes = {Boolean.class, Date.class, Double.class, Float.class,
                                                         Integer.class, Long.class, Short.class, String.class})
public class NotEqualTo<T extends RealmObject> extends RUQTransformer<T> {
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
        if (Names.BOOLEAN_CANON_NAME.equals(fieldTypeName)) return realmQuery.notEqualTo(field, (Boolean) args[0]);
        else if (Names.DATE_CANON_NAME.equals(fieldTypeName)) return realmQuery.notEqualTo(field, (Date) args[0]);
        else if (Names.DOUBLE_CANON_NAME.equals(fieldTypeName)) return realmQuery.notEqualTo(field, (Double) args[0]);
        else if (Names.FLOAT_CANON_NAME.equals(fieldTypeName)) return realmQuery.notEqualTo(field, (Float) args[0]);
        else if (Names.INTEGER_CANON_NAME.equals(fieldTypeName)) return realmQuery.notEqualTo(field, (Integer) args[0]);
        else if (Names.LONG_CANON_NAME.equals(fieldTypeName)) return realmQuery.notEqualTo(field, (Long) args[0]);
        else if (Names.SHORT_CANON_NAME.equals(fieldTypeName)) return realmQuery.notEqualTo(field, (Short) args[0]);
        else if (Names.STRING_CANON_NAME.equals(fieldTypeName)) return realmQuery.notEqualTo(field, (String) args[0]);
        else throw new IllegalArgumentException(String.format("Illegal argument type \"%s\".", fieldTypeName));
    }
}