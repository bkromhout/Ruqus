package com.bkromhout.ruqus.transformers;

import android.support.annotation.NonNull;
import com.bkromhout.ruqus.*;
import io.realm.RealmModel;
import io.realm.RealmQuery;

import java.util.Date;

/**
 * Transformer which wraps the various {@link RealmQuery} {@code notEqualTo()} methods.
 */
@Transformer(name = Names.NOT_EQUAL_TO, validArgTypes = {Boolean.class, Date.class, Double.class, Float.class,
                                                         Integer.class, Long.class, Short.class, String.class})
public class NotEqualTo extends RUQTransformer {
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
        if (FieldType.BOOLEAN == fieldType) return realmQuery.notEqualTo(field, (Boolean) args[0]);
        else if (FieldType.DATE == fieldType) return realmQuery.notEqualTo(field, (Date) args[0]);
        else if (FieldType.DOUBLE == fieldType) return realmQuery.notEqualTo(field, (Double) args[0]);
        else if (FieldType.FLOAT == fieldType) return realmQuery.notEqualTo(field, (Float) args[0]);
        else if (FieldType.INTEGER == fieldType) return realmQuery.notEqualTo(field, (Integer) args[0]);
        else if (FieldType.LONG == fieldType) return realmQuery.notEqualTo(field, (Long) args[0]);
        else if (FieldType.SHORT == fieldType) return realmQuery.notEqualTo(field, (Short) args[0]);
        else if (FieldType.STRING == fieldType) return realmQuery.notEqualTo(field, (String) args[0]);
        else
            throw new IllegalArgumentException(String.format("Illegal argument type \"%s\".", fieldType.getTypeName()));
    }

    @Override
    public String makeReadableString(@NonNull Condition current, Condition previous, Condition next) {
        return String.format(current.getFieldType() == FieldType.STRING ? "%s %s “%s”" : "%s %s %s",
                ReadableStringUtils.visibleFieldNameFrom(current),
                previous.getType() != Condition.Type.NOT ? "is unequal to" : "is not unequal to",
                ReadableStringUtils.argToString(current.getFieldType(), current.getArgs()[0]));
    }
}