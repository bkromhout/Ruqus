package com.bkromhout.ruqus;

import io.realm.RealmObject;
import io.realm.RealmResults;

import java.util.ArrayList;

/**
 * Used to let end-users build Realm Queries by wrapping {@link io.realm.RealmQuery}.
 */
public class RealmUserQuery {
    /**
     * Type of object which will be returned by the query.
     */
    private Class<? extends RealmObject> queryType;
    /**
     * List of query conditions.
     */
    private ArrayList<Condition> conditions;

    // TODO add the sort condition somehow.

    /**
     * Create a new {@link RealmUserQuery}.
     */
    public RealmUserQuery() {
        queryType = null;
        conditions = new ArrayList<>();
    }

    /**
     * Create a new {@link RealmUserQuery} from a string version of a realm user query.
     * @param ruqString A string obtained from {@link #toRuqString()}.
     */
    public RealmUserQuery(String ruqString) {
        conditions = new ArrayList<>();
        // TODO parse internal ruq string to re-create a ruq.
    }

    /**
     * Get the class for the type of objects which this query will return.
     * @return Class of query result objects.
     */
    public Class<? extends RealmObject> getQueryType() {
        return queryType;
    }

    /**
     * Set the type of object this query should return.
     * @param typeClass Class of type to return.
     * @throws IllegalArgumentException if {@code typeClass} isn't annotated with {@link Queryable}.
     */
    public void setQueryType(Class<? extends RealmObject> typeClass) throws IllegalArgumentException {
        // TODO check for queryable first.
        queryType = typeClass;
    }

    /**
     * Get the number of conditions currently present in this query.
     * @return Number of conditions.
     */
    public int conditionCount() {
        return conditions.size();
    }

    /**
     * Get the condition at {@code index}.
     * @param index Index of the condition to retrieve.
     * @return Condition, or null if the index is invalid.
     */
    public Condition getConditionAt(int index) {
        return (index >= 0 && index < conditions.size()) ? conditions.get(index) : null;
    }

    /**
     * Remove the condition at {@code index}.
     * @param index Index of the condition to remove.
     * @return Removed condition, or null if the index is invalid.
     */
    public Condition removeConditionAt(int index) {
        // TODO be smart about groups!
        return (index >= 0 && index < conditions.size()) ? conditions.remove(index) : null;
    }

    /**
     * Get the list of conditions in this query.
     * @return List of conditions.
     */
    ArrayList<Condition> getConditions() {
        return conditions;
    }

    /**
     * Checks whether the query is currently in a valid state.
     * @return True if this could be executed, otherwise false.
     */
    public boolean isQueryValid() {
        // TODO
        return false;
    }

    /**
     * Get a human-readable version of this query, suitable for displaying for the user.
     * <p/>
     * This will return null if the query isn't currently valid.
     * @return Human-readable query string.
     */
    @Override
    public String toString() {
        if (!isQueryValid()) return null;
        // TODO.
        return super.toString();
    }

    /**
     * Get a string which holds all of the information needed to create a {@link RealmUserQuery} identical to this one.
     * This string is not something which should be shown to users, it is intended to be stored somewhere so that it can
     * be used to recreate this query again later.
     * <p/>
     * This will return null if the query isn't currently in a valid state.
     * @return Internal string representation of this query.
     */
    public String toRuqString() {
        if (!isQueryValid()) return null;
        // TODO
        return null;
    }

    /**
     * Execute this query and return the results.
     * @return RealmResults, or null if query is invalid.
     */
    public <E extends RealmObject> RealmResults<E> execute() {
        if (!isQueryValid()) return null;
        // TODO who knows if this is even legal...
        return (RealmResults<E>) RUQExecutor.get(queryType, this).executeQuery();
    }
}
