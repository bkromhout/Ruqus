package com.bkromhout.ruqus;

import android.support.annotation.NonNull;
import io.realm.RealmModel;
import io.realm.RealmQuery;

/**
 * Extenders must provide a way to transform a {@link RealmUserQuery} and also must be annotated with {@link
 * Transformer}.
 * <p>
 * Note that Ruqus creates transformers using no-arg constructors.
 */
public abstract class RUQTransformer {
    // No-args constructor.
    protected RUQTransformer() {}

    /**
     * Called to transform a {@link RealmQuery} in some way.
     * @param realmQuery {@link RealmQuery} to transform.
     * @param condition  The {@link Condition} which this transformation is being applied to enforce.
     * @return Transformed {@link RealmQuery}.
     */
    public abstract <T extends RealmModel> RealmQuery<T> transform(RealmQuery<T> realmQuery, Condition condition);

    /**
     * Called to construct part of a human-readable query string which will be joined together with other parts to form
     * a whole.
     * <p>
     * The returned string fragment should describe the {@code current} condition, but the {@code previous} and {@code
     * next} conditions are also provided so that they may be taken into consideration; this typically only matters if
     * they are not {@link com.bkromhout.ruqus.Condition.Type#NORMAL NORMAL} conditions.
     * <p>
     * If the {@code previous} condition is the {@link com.bkromhout.ruqus.transformers.Not Not operator}, the returned
     * string should reflect this correctly; the {@link com.bkromhout.ruqus.transformers.Not Not operator} itself always
     * returns the empty string when its implementation of this method is called, except for some internally-handled
     * use cases.
     * @param current  The condition to make a human-readable string for.
     * @param previous The previous condition, which could affect the returned string. Might be {@code null}.
     * @param next     The next condition, which could affect the returned string. Might be {@code null}.
     * @return A human-readable string fragment which describes the {@code current} condition, with proper consideration
     * given to the {@code previous} and {@code next} conditions if applicable.
     * @see ReadableStringUtils
     */
    public abstract String makeReadableString(@NonNull Condition current, Condition previous, Condition next);
}
