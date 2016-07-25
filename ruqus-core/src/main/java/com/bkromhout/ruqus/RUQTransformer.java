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
     * Called to construct part of a human-readable query string.
     * <p>
     * The primary focus of the returned string fragment is the {@code current} condition, but the {@code previous} and
     * {@code next} conditions are also provided so that they may be factored in if they are present and not {@link
     * com.bkromhout.ruqus.Condition.Type#NORMAL NORMAL} conditions.
     * <p>
     * For example, the returned string should be surrounded with parentheses if {@code previous} was {@link
     * com.bkromhout.ruqus.transformers.BeginGroup BeginGroup} and {@code next} was {@link
     * com.bkromhout.ruqus.transformers.EndGroup EndGroup}.
     * <p>
     * This behavior should be kept in mind for non-normal transformers as well; in most cases they should simply return
     * an empty string, as the normal conditions to which they apply should be responsible for changing their returned
     * strings as described.
     * @param current  The condition to make a human-readable string for.
     * @param previous The previous condition, which should be considered when making the readable string if its type is
     *                 not {@link com.bkromhout.ruqus.Condition.Type#NORMAL NORMAL}. Might be {@code null}.
     * @param next     The next condition, which should be considered when making the readable string if its type is not
     *                 {@link com.bkromhout.ruqus.Condition.Type#NORMAL NORMAL}. Might be {@code null}.
     * @return For normal conditions: A human-readable string fragment which describes the {@code current} condition,
     * and factors in the {@code previous} and {@code next} conditions if they are applicable.<br/>For non-normal
     * conditions: Most cases should return the empty string, see the end of the method description for details.
     */
    public abstract String makeReadableString(@NonNull Condition current, Condition previous, Condition next);
}
