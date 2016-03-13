package com.bkromhout.ruqus;

import java.lang.annotation.*;

/**
 * Classes annotated with {@link Queryable} can be used as the query return type in a RealmUserQuery.
 * <p/>
 * The name given to this annotation will be used even if the {@link VisibleAs} annotation is present on a class.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Queryable {
    /**
     * Name to use in user-visible areas.
     */
    int name();
}
