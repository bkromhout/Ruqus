package com.bkromhout.ruqus.sample;

import android.support.annotation.NonNull;
import com.bkromhout.ruqus.Condition;
import com.bkromhout.ruqus.RUQTransformer;
import com.bkromhout.ruqus.Transformer;
import io.realm.RealmModel;
import io.realm.RealmQuery;

/**
 * Example of a no-args transformer. Like the name implies, it does nothing to the query.
 */
@Transformer(name = "NoOp", isNoArgs = true, numArgs = 0, validArgTypes = {})
public class NoOp extends RUQTransformer {
    @Override
    public <T extends RealmModel> RealmQuery<T> transform(RealmQuery<T> realmQuery, Condition condition) {
        return realmQuery;
    }

    @Override
    public String makeReadableString(@NonNull Condition current, Condition previous, Condition next) {
        return "";
    }
}
