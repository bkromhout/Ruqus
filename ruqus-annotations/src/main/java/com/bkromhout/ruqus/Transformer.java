package com.bkromhout.ruqus;

import java.lang.annotation.*;

/**
 * Indicates that a class provides a transformation for a RealmUserQuery.
 * <p/>
 * Classes annotated with this must also extend {@code RUQTransformer}.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Transformer {

    // TODO have a field-only constant?

    /**
     * Name to use in user-visible areas. For transformers, this should be something that makes sense within the context
     * of a sentence.<br>For example, if a transformer wraps the {@code between()} method on {@code RealmQuery}, this
     * should be something like "is between"; if it wraps {@code beginsWith()}, this should be something like "begins
     * with"; etc.
     */
    String name();

    /**
     * Array of classes which represent the type(s) which this transformer will accept for its arguments.<br>For
     * example, if a transformer wraps the {@code between()} method on {@code RealmQuery}, this should be
     * <code>{Date.class, Double.class, Float.class, Integer.class, Long.class}</code>; if it wraps {@code
     * beginsWith()}, this should be <code>{String.class}</code>; etc.<br>This may be set to {@code null} if {@code
     * numArgs = 0}, and it is ignored for no-arg transformers.
     */
    Class[] validArgTypes();

    /**
     * The number of arguments this transformer takes (not including the field itself). Ruqus uses this number to verify
     * that enough arguments are present in the {@code Condition} before it is passed to the transformer, as well as
     * verifying that the types of those arguments match that of the field.
     * <p/>
     * By default this is 1, but developers can override it if need be. All integers >= 0 are allowed.<br>If you wish to
     * have a variable number of arguments, set this to {@link C#VAR_ARGS}, but be aware that Ruqus will not check that
     * the {@code Condition}'s argument types match its field's type if you do this, you will have to do it yourself in
     * your transformer.
     * <p/>
     * This is ignored for no-arg transformers.
     */
    int numArgs() default 1;

    /**
     * If true, this will be shown along with the list of fields rather than after selecting a field.
     * <p/>
     * This should only be set to true for transformers which do not require a field or any arguments to operate. Most
     * developers will have no need for this.
     */
    boolean isNoArgs() default false;
}
