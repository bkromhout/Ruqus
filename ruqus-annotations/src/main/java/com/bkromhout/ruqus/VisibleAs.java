package com.bkromhout.ruqus;

import java.lang.annotation.*;

/**
 * Fields and classes can annotated with {@link VisibleAs} in order to have Ruqus use the specified {@link #string()} in
 * user-visible places instead of the string that it would otherwise automatically generate.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface VisibleAs {
    /**
     * What string to use in user-visible areas.
     */
    String string();
}
