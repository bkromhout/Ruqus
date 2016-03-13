package com.bkromhout.rqv;

import java.lang.annotation.*;

/**
 * Fields annotated with {@link Hide} won't be made available for users when they are building a {@link
 * RealmUserQuery}.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Hide {
}
