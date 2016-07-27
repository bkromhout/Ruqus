package com.bkromhout.ruqus.transformers;

import android.support.annotation.NonNull;
import com.bkromhout.ruqus.*;
import io.realm.RealmModel;
import io.realm.RealmQuery;

import static com.bkromhout.ruqus.ReadableStringUtils.notNOT;

/**
 * Transformer which wraps {@link RealmQuery#contains(String, String)}.
 */
@Transformer(name = Names.STRING_CONTAINS, validArgTypes = {String.class})
public class StringContains extends RUQTransformer {
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
        if (FieldType.STRING == fieldType) return realmQuery.contains(field, (String) args[0]);
        else
            throw new IllegalArgumentException(String.format("Illegal argument type \"%s\".", fieldType.getTypeName()));
    }

    @Override
    public String makeReadableString(@NonNull Condition current, Condition previous, Condition next) {
        return String.format("%s %s “%s”", ReadableStringUtils.visibleFieldNameFrom(current),
                notNOT(previous) ? "contains" : "does not contain",
                ReadableStringUtils.argToString(current.getFieldType(), current.getArgs()[0]));
    }
}
