package com.bkromhout.ruqus;

import java.lang.annotation.*;

/**
 * Fields annotated with {@link Hide} won't be made available for users when they are building a RealmUserQuery.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Hide {
}
