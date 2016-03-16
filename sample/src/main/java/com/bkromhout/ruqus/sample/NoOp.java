package com.bkromhout.ruqus.sample;

import com.bkromhout.ruqus.Condition;
import com.bkromhout.ruqus.RUQTransformer;
import com.bkromhout.ruqus.Transformer;
import io.realm.RealmObject;
import io.realm.RealmQuery;

/**
 * Created by bkromhout on 3/16/16.
 */
@Transformer(name = "NoOp")
public class NoOp<T extends RealmObject> extends RUQTransformer<T> {
    @Override
    public RealmQuery<T> transform(RealmQuery<T> realmQuery, Condition condition) {
        return realmQuery;
    }
}
