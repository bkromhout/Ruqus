package com.bkromhout.ruqus;

import android.os.Parcel;
import android.os.Parcelable;
import com.squareup.phrase.ListPhrase;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Used to let end-users build Realm Queries by wrapping {@link io.realm.RealmQuery}.
 * <p/>
 * TODO make this Parcelable!
 */
public class RealmUserQuery implements Parcelable {
    private static final String PART_SEP = "#$_Ruqus_RUQ_$#";
    private static final String COND_SEP = "#$_Condition_$#";
    private static final String SORT_SEP = "#$_Sort_$#";
    private static final String SORT_SUB_SEP = "<<>>";
    private static final Pattern PART_SEP_PATTERN = Pattern.compile("\\Q" + PART_SEP + "\\E");
    private static final Pattern COND_SEP_PATTERN = Pattern.compile("\\Q" + COND_SEP + "\\E");
    private static final Pattern SORT_SEP_PATTERN = Pattern.compile("\\Q" + SORT_SEP + "\\E");
    private static final Pattern SORT_SUB_SEP_PATTERN = Pattern.compile("\\Q" + SORT_SUB_SEP + "\\E");

    /**
     * Type of object which will be returned by the query.
     */
    private Class<? extends RealmObject> queryClass;
    /**
     * List of query conditions.
     */
    private ArrayList<Condition> conditions;
    /**
     * List of fields to sort by (and which sort direction to use for each).
     */
    private ArrayList<String> sortFields;
    /**
     * List of sort directions.
     */
    private ArrayList<Sort> sortDirs;

    /**
     * Create a new {@link RealmUserQuery}.
     */
    RealmUserQuery() {
        queryClass = null;
        conditions = new ArrayList<>();
        sortFields = new ArrayList<>();
        sortDirs = new ArrayList<>();
    }

    /**
     * Create a new {@link RealmUserQuery} from a string version of a realm user query.
     * @param ruqString A string obtained from {@link #toRuqString()}.
     */
    public RealmUserQuery(String ruqString) {
        if (ruqString == null || ruqString.isEmpty())
            throw new IllegalArgumentException("ruqString must ne non-null and non-empty");

        // Split RUQ string into parts, make sure we have all of them.
        String[] parts = PART_SEP_PATTERN.split(ruqString);
        if (parts.length != 3) throw new IllegalArgumentException("ruqString is missing parts.");

        // Figure out query class.
        if (parts[0].isEmpty()) throw new IllegalArgumentException("Query class part must not be empty.");
        queryClass = Ruqus.getClassFromName(parts[0]);

        // Figure out Conditions.
        conditions = new ArrayList<>();
        if (!parts[1].isEmpty()) {
            String[] condStrings = COND_SEP_PATTERN.split(parts[1]);
            for (String condString : condStrings) conditions.add(new Condition(condString));
        }

        // Figure out sort fields and directions.
        sortFields = new ArrayList<>();
        sortDirs = new ArrayList<>();
        if (!parts[2].isEmpty()) {
            String[] sortStrings = SORT_SEP_PATTERN.split(parts[2]);
            for (String sortString : sortStrings) {
                String[] sortStringParts = SORT_SUB_SEP_PATTERN.split(sortString);
                if (sortStringParts.length != 2)
                    throw new IllegalArgumentException(String.format("Invalid sort string \"%s\".", sortString));
                sortFields.add(sortStringParts[0]);
                sortDirs.add(sortStringParts[1].equals("ASC") ? Sort.ASCENDING : Sort.DESCENDING);
            }
        }
    }

    /**
     * Get the class for the type of objects which this query will return.
     * @return Class of query result objects.
     */
    Class<? extends RealmObject> getQueryClass() {
        return queryClass;
    }

    /**
     * Set the type of object this query should return.
     * @param typeClassName Name of class of type to return.
     */
    void setQueryClass(String typeClassName) {
        setQueryClass(Ruqus.getClassFromName(typeClassName));
    }

    /**
     * Set the type of object this query should return.
     * @param typeClass Class of type to return.
     */
    private void setQueryClass(Class<? extends RealmObject> typeClass) {
        queryClass = typeClass;
    }

    /**
     * Get the number of conditions currently present in this query.
     * @return Number of conditions.
     */
    int conditionCount() {
        return conditions.size();
    }

    /**
     * Get the list of conditions in this query.
     * @return List of conditions.
     */
    ArrayList<Condition> getConditions() {
        return conditions;
    }

    /**
     * Set the sort fields and directions. Lists must be of the same size, and must contain from 0 to 3 elements each.
     * @param sortFields Fields to use to sort the results of this query.
     * @param sortDirs   Directions to sort in.
     */
    void setSorts(ArrayList<String> sortFields, ArrayList<Sort> sortDirs) {
        if (sortFields == null || sortDirs == null || sortFields.size() != sortDirs.size())
            throw new IllegalArgumentException("Neither list may be null, and they must be of the same size.");
        if (sortFields.size() > 3)
            throw new IllegalArgumentException("A maximum of 3 fields may be used for sorting.");
        this.sortFields = sortFields;
        this.sortDirs = sortDirs;
    }

    /**
     * Get the list of fields to sort the query results by.
     * @return Sort fields list.
     */
    ArrayList<String> getSortFields() {
        return sortFields;
    }

    /**
     * Get the list of directions to sort in.
     * @return Sort directions list.
     */
    ArrayList<Sort> getSortDirs() {
        return sortDirs;
    }

    /**
     * Checks whether the query is currently in a valid state.
     * @return True if this could be executed, otherwise false.
     */
    public boolean isQueryValid() {
        // Checks for realmClass.
        if (!Ruqus.knowsOfClass(queryClass) || !Ruqus.isClassQueryable(queryClass)) return false;
        // Checks for sort fields.
        for (String sortField : sortFields)
            if (!Ruqus.classHasField(queryClass.getSimpleName(), sortField)) return false;
        // Checks for conditions.
        for (Condition condition : conditions) if (!condition.isValid()) return false;
        // We're good.
        return true;
    }

    /**
     * Execute this query and return the results.
     * @return RealmResults, or null if query is invalid.
     */
    public <E extends RealmObject> RealmResults<E> execute() {
        if (!isQueryValid()) return null;
        // TODO who knows if this is even legal...
        // noinspection unchecked
        return (RealmResults<E>) RUQExecutor.get(queryClass, this).executeQuery();
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
        // Build up visible string. Start by stating the visible name of the type our results will be.
        StringBuilder builder = new StringBuilder()
                .append("Find all ")
                .append(Ruqus.getClassData().visibleNameOf(queryClass));

        // Next, loop through any conditions, appending their human-readable strings and applicable separators.
        if (!conditions.isEmpty()) {
            builder.append(" where ");
            for (int i = 0; i < conditions.size(); i++) {
                Condition condition = conditions.get(i);
                Condition nextCondition = i + 1 == conditions.size() ? null : conditions.get(i + 1);
                // Append human-readable condition string.
                builder.append(condition.toString());
                // If we don't have any more conditions after this one, just append a period and continue.
                if (nextCondition == null) {
                    builder.append(".");
                    continue;
                }
                // As long as this condition wasn't a BEGIN_GROUP and next condition isn't END_GROUP, append a space.
                if (condition.getType() != Condition.Type.BEGIN_GROUP &&
                        nextCondition.getType() != Condition.Type.END_GROUP) builder.append(" ");
                // As long as this condition wasn't OR or BEGIN_GROUP, and the next condition is NORMAL, NO_ARGS, or
                // BEGIN_GROUP, append "and ".
                if (!(condition.getType() == Condition.Type.OR || condition.getType() == Condition.Type.BEGIN_GROUP) &&
                        (nextCondition.getType() == Condition.Type.NORMAL ||
                                nextCondition.getType() == Condition.Type.NO_ARGS ||
                                nextCondition.getType() == Condition.Type.BEGIN_GROUP)) builder.append("and ");
            }
        }

        // Finally, state any sort fields we'll use to sort the results (and the directions of each).
        if (!sortFields.isEmpty()) builder.append(" Sort the results by ")
                                          .append(getSortString());

        return builder.toString();
    }

    /**
     * Return a human-readable sort string.
     * @return Human-readable sort string, or null if there are no sort fields.
     */
    String getSortString() {
        if (sortStrings().isEmpty()) return null;
        return ListPhrase.from(" and ", ", ", ", and ").join(sortStrings()).toString() + ".";
    }

    /**
     * Get a list of human-readable sort strings.
     * @return List of human-readable sort strings.
     */
    private ArrayList<String> sortStrings() {
        if (sortFields.isEmpty()) return new ArrayList<>();
        String className = queryClass.getSimpleName();
        ArrayList<String> sorts = new ArrayList<>();
        for (int i = 0; i < sortFields.size(); i++) {
            sorts.add(String.format(
                    "%s (%s)",
                    Ruqus.visibleFieldFromField(className, sortFields.get(i)),
                    Ruqus.typeEnumForField(className, sortFields.get(i)).getPrettySortString(sortDirs.get(i))
            ));
        }
        return sorts;
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
        StringBuilder builder = new StringBuilder();

        // Write out query class's name.
        builder.append(queryClass.getSimpleName())
               .append(PART_SEP);

        // Write out condition strings.
        if (!conditions.isEmpty()) {
            for (Condition condition : conditions)
                builder.append(condition.toConditionString())
                       .append(COND_SEP);
            builder.delete(builder.lastIndexOf(COND_SEP), builder.length());
        }
        builder.append(PART_SEP);

        // Write out sort strings.
        if (!sortFields.isEmpty()) {
            for (int i = 0; i < sortFields.size(); i++) {
                builder.append(sortFields.get(i))
                       .append(SORT_SUB_SEP)
                       .append(sortDirs.get(i) == Sort.ASCENDING ? "ASC" : "DESC")
                       .append(SORT_SEP);
            }
            builder.delete(builder.lastIndexOf(SORT_SEP), builder.length());
        }

        return builder.toString();
    }

    /* Parcelable implementation. */

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.queryClass);
        dest.writeTypedList(conditions);
        dest.writeStringList(this.sortFields);
        ArrayList<Integer> temp = new ArrayList<>();
        for (Sort sortDir : this.sortDirs) temp.add(sortDir.ordinal());
        dest.writeList(temp);
    }

    RealmUserQuery(Parcel in) {
        // noinspection unchecked
        this.queryClass = (Class<? extends RealmObject>) in.readSerializable();
        this.conditions = in.createTypedArrayList(Condition.CREATOR);
        this.sortFields = in.createStringArrayList();
        this.sortDirs = new ArrayList<>();
        ArrayList<Integer> temp = new ArrayList<>();
        in.readList(temp, null);
        for (Integer sortOrdinal : temp) this.sortDirs.add(Sort.values()[sortOrdinal]);
    }

    public static final Parcelable.Creator<RealmUserQuery> CREATOR = new Parcelable.Creator<RealmUserQuery>() {
        @Override
        public RealmUserQuery createFromParcel(Parcel source) {return new RealmUserQuery(source);}

        @Override
        public RealmUserQuery[] newArray(int size) {return new RealmUserQuery[size];}
    };
}
