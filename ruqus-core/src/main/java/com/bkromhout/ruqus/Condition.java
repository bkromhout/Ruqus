package com.bkromhout.ruqus;

/**
 * This class is responsible for holding a condition.
 */
public class Condition {
    private static final String BEGIN_GROUP_TNAME = "BeginGroup";
    private static final String END_GROUP_TNAME = "EndGroup";
    private static final String OR_TNAME = "Or";

    /**
     * Type of condition.
     */
    public enum Type {
        NORMAL, NO_ARG, BEGIN_GROUP, END_GROUP, OR
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
    private Class<?> fieldType;
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
    public Condition() {
        this(Type.NORMAL);
    }

    public Condition(Type type) {
        this.type = type;
        handleSpecialTypes();
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
     * @return Whether or not this {@link Condition} is fully-formed and valid.
     */
    public boolean isValid() {
        switch (type) {
            case NORMAL:
                return isRealmClassValid() && isFieldDataValid() && isTransformerValid() && areArgsValid();
            case NO_ARG:
            case BEGIN_GROUP:
            case END_GROUP:
            case OR:
                return isRealmClassValid() && isTransformerValid() && areArgsValid();
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
        return validStr(transformer) && (type != Type.NORMAL || Ruqus.transformerAcceptsType(transformer, fieldType));
    }

    /**
     * Checks that {@code args} is valid given the current state.
     * @return True if conditions are met, otherwise false.
     */
    private boolean areArgsValid() {
        if (type != Type.NORMAL) return true;
        int numArgs = Ruqus.getTransformerData().numArgsOf(transformer);
        if (numArgs == C.VAR_ARGS) return fieldType != null && args != null && args.length > 0;
        else if (numArgs == 0) return true;
        else if (numArgs > 0) {
            if (fieldType == null || args == null || args.length < numArgs) return false;
            // Ensure that the first [numArgs] items in args have the same type as fieldType.
            for (int i = 0; i < numArgs; i++) if (!fieldType.isInstance(args[i])) return false;
            return true;
        } else throw new IllegalArgumentException("Transformer \"" + transformer + "\" has numArgs set to < -1.");
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        if (this.type == type) return;
        // If this is a different type, clear our vars.
        resetState();
        this.type = type;
        // Do some special stuff based on the new type.
        handleSpecialTypes();
    }

    public String getRealmClass() {
        return realmClass;
    }

    public void setRealmClass(String realmClass) {
        this.realmClass = realmClass;
        // Also try to figure out the field type, if field is already set.
        tryResolveFieldType();
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        if (type != Type.NORMAL)
            throw new IllegalArgumentException("Condition type must be NORMAL to set the field.");
        this.field = field;
        // Also figure out what the field type is and set that.
        tryResolveFieldType();
    }

    public Class<?> getFieldType() {
        return fieldType;
    }

    private void tryResolveFieldType() {
        if (type != Type.NORMAL) return;
        // We can only do this if we have both the realmClass and the field name.
        if (realmClass == null || realmClass.isEmpty() || field == null || field.isEmpty()) return;

    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        if (type != Type.NORMAL)
            throw new IllegalArgumentException("Condition type must be NORMAL to set arguments.");
        this.args = args;
    }

    public String getTransformer() {
        return transformer;
    }

    public void setTransformer(String transformer) {
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
}
