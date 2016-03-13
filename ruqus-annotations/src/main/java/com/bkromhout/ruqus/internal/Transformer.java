package com.bkromhout.ruqus.internal;

import java.lang.annotation.*;

/**
 * Used by RUQTransformer. Indicates that a class provides a transformation for a RealmUserQuery.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Inherited
@interface Transformer {
}
