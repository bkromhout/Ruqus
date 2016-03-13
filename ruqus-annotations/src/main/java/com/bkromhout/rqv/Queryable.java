package com.bkromhout.rqv;

import java.lang.annotation.*;

/**
 * Classes annotated with {@link Queryable} can be used as the query return type in a {@link RealmUserQuery}.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Queryable {
    /**
     * Human-readable name which will be shown for this
     * @return
     */
     int name();
}
