package com.bkromhout.ruqus;

import android.os.Parcel;
import android.os.Parcelable;
import com.squareup.phrase.ListPhrase;
import com.squareup.phrase.Phrase;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * This class is responsible for holding a condition.
 */
public class Condition implements Parcelable {
    private static final String C_SEP = "|||";
    private static final String ARG_SEP = ";,;";
    private static final Pattern C_SEP_PATTERN = Pattern.compile("\\Q" + C_SEP + "\\E");
    private static final Pattern ARG_SEP_PATTERN = Pattern.compile("\\Q" + ARG_SEP + "\\E");
    private static final String BEGIN_GROUP_TNAME = "com.bkromhout.ruqus.transformers.BeginGroup";
    private static final String END_GROUP_TNAME = "com.bkromhout.ruqus.transformers.EndGroup";
    private static final String OR_TNAME = "com.bkromhout.ruqus.transformers.Or";

    /**
     * Types of conditions.
     */
    public enum Type {
        NORMAL("NORMAL"), NO_ARGS("NO_ARGS"), BEGIN_GROUP("BEGIN_GROUP"), END_GROUP("END_GROUP"), OR("OR");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Type getForName(String name) {
            switch (name) {
                case "NORMAL":
                    return NORMAL;
                case "NO_ARGS":
                    return NO_ARGS;
                case "BEGIN_GROUP":
                    return BEGIN_GROUP;
                case "END_GROUP":
                    return END_GROUP;
                case "OR":
                    return OR;
                default:
                    throw new IllegalArgumentException("Invalid Condition type name.");
            }
        }
    }

    /**
     * Type of condition.
     */
    private Type type;
    /**
     * Real name of the realm object class this condition applies to.
     */
    private String realmClass;
    /**
     * Real name of the field this condition applies to. It's possible that this may be a field on another realm object,
     * in which case the format will be "{local field name}.{other class' field name}"
     */
    private String field;
    /**
     * The type of the field. If the field is a primitive, this will be the boxed version of it. If the field is a
     * subclass of RealmObject or a RealmList, this will be the type of the field on that object.
     */
    private FieldType fieldType;
    /**
     * The args which will be used for this condition. The types of these will be verified to ensure that they match
     * {@link #fieldType} before they are passed to the transformer.
     */
    private Object[] args;
    /**
     * The name of the transformer to use to apply this condition.
     */
    private String transformer;

    /**
     * Create a new {@link Condition}.
     */
    Condition() {
        this(Type.NORMAL);
    }

    Condition(Type type) {
        this.type = type;
        handleSpecialTypes();
    }

    /**
     * Create a condition object using a representative string.
     * @param conditionString String which contains enough information to construct a new condition object.
     */
    Condition(String conditionString) {
        // Split into parts.
        String[] parts = C_SEP_PATTERN.split(conditionString);

        // Figure out type.
        type = Type.getForName(parts[0]);
        handleSpecialTypes();

        // Figure out realmClass.
        realmClass = parts[1];
        if (!isRealmClassValid())
            throw new IllegalArgumentException(String.format("Ruqus doesn't have data for \"%s\".", parts[1]));

        // If we have the transformer already, we're done, because the type was one of our built-in special ones.
        // Otherwise, do the rest based on type (which at this point would have to be either NORMAL or NO_ARGS).
        if (transformer != null) return;
        else if (type == Type.NORMAL) {
            // Figure out field.
            setField(parts[2]);

            // Figure out args.
            args = argsFromString(parts[3]);
        }

        // Figure out transformer.
        transformer = parts[type == Type.NORMAL ? 4 : 2];
        if (!isTransformerValid()) throw new IllegalArgumentException(String.format("Ruqus doesn't have data for the " +
                "transformer \"%s\"", transformer));
    }

    /**
     * Resets all variables to null, except for {@link #type}.
     */
    private void resetState() {
        realmClass = null;
        field = null;
        fieldType = null;
        args = null;
        transformer = null;
    }

    /**
     * Provide convenience for certain types.
     */
    private void handleSpecialTypes() {
        switch (type) {
            case BEGIN_GROUP:
                transformer = BEGIN_GROUP_TNAME;
                break;
            case END_GROUP:
                transformer = END_GROUP_TNAME;
                break;
            case OR:
                transformer = OR_TNAME;
                break;
        }
    }

    /**
     * Tries to get the correct value for {@link #fieldType} based on current values for {@link #realmClass} and {@link
     * #field}.
     */
    private void tryResolveFieldType() {
        if (type != Type.NORMAL) return;
        // We can only do this if we have both the realmClass and the field name.
        if (realmClass == null || realmClass.isEmpty() || field == null || field.isEmpty()) return;
        // Get field type from our generated info.
        fieldType = Ruqus.typeEnumForField(realmClass, field);
    }

    /**
     * @return Whether or not this {@link Condition} is fully-formed and valid.
     */
    public boolean isValid() {
        switch (type) {
            case NORMAL:
                return isRealmClassValid() && isFieldDataValid() && isTransformerValid() && areArgsValid();
            case NO_ARGS:
            case BEGIN_GROUP:
            case END_GROUP:
            case OR:
                return isRealmClassValid() && isTransformerValid();
            default:
                return false;
        }
    }

    /**
     * Checks that {@code realmClass} is non-null and non-empty, and that Ruqus has data for it.
     * @return True if conditions are met, otherwise false.
     */
    private boolean isRealmClassValid() {
        return validStr(realmClass) && Ruqus.knowsOfClass(realmClass);
    }

    /**
     * Checks that {@code field} is valid given the current state.
     * @return True if conditions are met, otherwise false.
     */
    private boolean isFieldDataValid() {
        // We need to have a non-null/non-empty realmClass, field, and field type to even check this.
        return validStr(realmClass) && validStr(field) && fieldType != null && Ruqus.classHasField(realmClass,
                field) && Ruqus.fieldIsOfType(realmClass, field, fieldType);
    }

    /**
     * Checks that {@code transformer} is valid given the current state.
     * @return True if conditions are met, otherwise false.
     */
    private boolean isTransformerValid() {
        return validStr(transformer) && Ruqus.knowsOfTransformer(transformer) &&
                (type != Type.NORMAL || Ruqus.transformerAcceptsType(transformer, fieldType.getClazz()));
    }

    /**
     * Checks that {@code args} is valid given the current state.
     * @return True if conditions are met, otherwise false.
     */
    private boolean areArgsValid() {
        if (type != Type.NORMAL) return true;
        int numArgs = Ruqus.numberOfArgsFor(transformer);
        if (numArgs == C.VAR_ARGS) return fieldType != null && args != null && args.length > 0;
        else if (numArgs == 0) return true;
        else if (numArgs > 0) {
            if (fieldType == null || args == null || args.length < numArgs) return false;
            // Ensure that the first [numArgs] items in args have the same type as fieldType.
            for (int i = 0; i < numArgs; i++) if (!fieldType.getClazz().isInstance(args[i])) return false;
            return true;
        } else throw new IllegalArgumentException("Transformer \"" + transformer + "\" has numArgs set to < -1.");
    }

    public Type getType() {
        return type;
    }

//    void setType(Type type) {
//        if (this.type == type) return;
//        // If this is a different type, clear our vars.
//        resetState();
//        this.type = type;
//        // Do some special stuff based on the new type.
//        handleSpecialTypes();
//    }

    public String getRealmClass() {
        return realmClass;
    }

    void setRealmClass(String realmClass) {
        this.realmClass = realmClass;
        // Also try to figure out the field type, if field is already set.
        tryResolveFieldType();
    }

    public String getField() {
        return field;
    }

    void setField(String field) {
        if (type != Type.NORMAL)
            throw new IllegalArgumentException("Condition type must be NORMAL to set the field.");
        this.field = field;
        // Also figure out what the field type is and set that.
        tryResolveFieldType();
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public Object[] getArgs() {
        return args;
    }

    void setArgs(Object[] args) {
        if (type != Type.NORMAL)
            throw new IllegalArgumentException("Condition type must be NORMAL to set arguments.");
        this.args = args;
    }

    String getTransformer() {
        return transformer;
    }

    void setTransformer(String transformer) {
        // Be convenient if the transformer we're setting is associated with one of our specific types.
        switch (transformer) {
            case BEGIN_GROUP_TNAME:
                resetState();
                type = Type.BEGIN_GROUP;
                handleSpecialTypes();
                break;
            case END_GROUP_TNAME:
                resetState();
                type = Type.END_GROUP;
                handleSpecialTypes();
                break;
            case OR_TNAME:
                resetState();
                type = Type.OR;
                handleSpecialTypes();
                break;
            default:
                this.transformer = transformer;
                type = Ruqus.getTransformerData().isNoArgs(transformer) ? Type.NO_ARGS : Type.NORMAL;
        }
    }

    /**
     * Convenience method to ensure that a string is non-null and non-empty.
     * @param s String to check.
     * @return True if checks pass, otherwise false.
     */
    private static boolean validStr(String s) {
        return s != null && !s.isEmpty();
    }

    /**
     * Return a human-readable string which describes this condition.
     * @return Human-readable condition string, or null if not valid.
     */
    @Override
    public String toString() {
        if (!isValid()) return null;
        TransformerData transformerData = Ruqus.getTransformerData();

        switch (type) {
            // Normal transformers have arguments which we need to factor into our string.
            case NORMAL:
                FieldData fieldData = Ruqus.getFieldData(realmClass);
                StringBuilder builder = new StringBuilder();
                builder.append(Phrase.from("{field} {transformerVName}")
                                     .put("field", fieldData.visibleNameOf(field))
                                     .put("transformerVName", transformerData.visibleNameOf(transformer))
                                     .toString());

                int numArgs = transformerData.numArgsOf(transformer);
                if (numArgs == 0)
                    return builder.toString();
                else if (numArgs == 1)
                    return builder.append(" ")
                                  .append(String.valueOf(args[0]))
                                  .toString();
                else if (numArgs == C.VAR_ARGS || numArgs > 1)
                    return builder.append(" ")
                                  .append(ListPhrase.from(" and ", ", ", ", and ")
                                                    .join(Arrays.asList(args))
                                                    .toString())
                                  .toString();
                else throw new IllegalArgumentException("numArgs < -1.");
                // The following have no arguments, so we just return the visible name.
            case NO_ARGS:
            case BEGIN_GROUP:
            case END_GROUP:
            case OR:
                return transformerData.visibleNameOf(transformer);
            default:
                return super.toString();
        }
    }

    /**
     * Return a string representation of this {@link Condition} which contains enough information to recreate it later.
     * @return String representation, or null if not valid.
     */
    String toConditionString() {
        if (!isValid()) return null;
        StringBuilder builder = new StringBuilder();
        builder.append(type.getName()) // Write out type.
               .append(C_SEP)
               .append(realmClass) // Write out real realm class name.
               .append(C_SEP);
        switch (type) {
            case NORMAL:
                builder.append(field) // Write out real field name.
                       .append(C_SEP)
                       .append(argsToString()) // Write out args.
                       .append(C_SEP);
            case NO_ARGS:
            case BEGIN_GROUP:
            case END_GROUP:
            case OR:
                builder.append(transformer); // Write out real transformer name.
                break;
        }
        return builder.toString();
    }

    private String argsToString() {
        if (args == null || args.length == 0) return "";
        StringBuilder argsStr = new StringBuilder();
        for (Object arg : args)
            argsStr.append(FieldType.makeDataString(arg))
                   .append(ARG_SEP);
        if (argsStr.length() > 1) argsStr.deleteCharAt(argsStr.lastIndexOf(ARG_SEP));
        return argsStr.toString();
    }

    private Object[] argsFromString(String argsString) {
        if (argsString == null || argsString.isEmpty()) return null;
        String[] argParts = ARG_SEP_PATTERN.split(argsString);
        Object[] args = new Object[argParts.length];
        for (int i = 0; i < argParts.length; i++) args[i] = FieldType.parseDataString(argParts[i]);
        return args;
    }

    /* Parcelable implementation. */

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
        dest.writeString(this.realmClass);
        dest.writeString(this.field);
        dest.writeInt(this.fieldType == null ? -1 : this.fieldType.ordinal());
        dest.writeString(argsToString());
        dest.writeString(this.transformer);
    }

    private Condition(Parcel in) {
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : Type.values()[tmpType];
        this.realmClass = in.readString();
        this.field = in.readString();
        int tmpFieldType = in.readInt();
        this.fieldType = tmpFieldType == -1 ? null : FieldType.values()[tmpFieldType];
        this.args = argsFromString(in.readString());
        this.transformer = in.readString();
    }

    public static final Parcelable.Creator<Condition> CREATOR = new Parcelable.Creator<Condition>() {
        @Override
        public Condition createFromParcel(Parcel source) {return new Condition(source);}

        @Override
        public Condition[] newArray(int size) {return new Condition[size];}
    };
}
