package com.bkromhout.ruqus;

/**
 * Access to Ruqus information. This class mostly serves as a convenience class, using the instances of {@link
 * ClassData} and {@link TransformerData} objects that it holds in order to provide static methods which allow the rest
 * of the library to make on-liner calls as much as possible.
 */
public class Ruqus {
    /**
     * Whether or not Ruqus.init() has already been called.
     */
    private static Ruqus INSTANCE = null;
    /**
     * Ruqus class information.
     */
    private ClassData classData;
    /**
     * Ruqus transformer information.
     */
    private TransformerData transformerData;

    private Ruqus() {
        // Load the Ruqus class data object.
        try {
            classData = (ClassData) Class.forName(C.GEN_PKG_PREFIX + C.GEN_CLASS_DATA_CLASS_NAME).newInstance();
        } catch (ClassNotFoundException e) {
            throw ex("Could not find generated Ruqus class data, did the annotation processor run?");
        } catch (Exception e) {
            throw ex("Could not get generated Ruqus class data.");
        }

        // Load the Ruqus transformer data object. Make sure that we look through all of them so that all of the
        // transformers' data are loaded into the base class's static variables. The reason there are more than
        // one is due to there being at least one which comes with Ruqus (for the transformers which come with
        // Ruqus), plus any more which are generated when the app compiles due to the dev creating their own.
        int num = 1;
        boolean noneYet = true;
        while (true) {
            try {
                transformerData = (TransformerData) Class.forName(C.GEN_PKG_PREFIX +
                        C.GEN_TRANSFORMER_DATA_CLASS_NAME + String.valueOf(num)).newInstance();
                noneYet = false;
                num++;
            } catch (ClassNotFoundException e) {
                if (noneYet) throw ex("Could not find generated Ruqus class data, did the annotation processor run?");
                else break;
            } catch (Exception e) {
                if (noneYet) throw ex("Could not get generated Ruqus class data.");
                else break;
            }
        }
    }

    /**
     * Initializes Ruqus. This must be called <i>once</i> before any of the other methods on {@link Ruqus} can be used,
     * and it requires reflection. It is recommended that it be called as early as possible in the application's
     * lifecycle.
     */
    public static void init() {
        if (INSTANCE == null) INSTANCE = new Ruqus();
    }

    /**
     * Ensures that {@link Ruqus#init()} has been called.
     */
    private static void ensureInit() {
        if (INSTANCE == null) throw ex("Ruqus.init() must be called first.");
    }

    public static ClassData getClassData() {
        ensureInit();
        return INSTANCE.classData;
    }

    public static TransformerData getTransformerData() {
        ensureInit();
        return INSTANCE.transformerData;
    }

    /**
     * Check whether or not Ruqus has data for a RealmObject subclass with the name {@code realmClass}.
     * @param realmClass Name to check for.
     * @return True if Ruqus knows of a class called {@code realmClass}, otherwise false.
     */
    public static boolean knowsOfClass(String realmClass) {
        return INSTANCE.classData.isValidName(realmClass);
    }

    /**
     * Check if a {@code realmClass} has a given {@code field}.
     * @param realmClass Name of the RealmObject subclass to check.
     * @param field      Name of the field to check for.
     * @return True if {@code realmClass} has {@code field}.
     */
    public static boolean classHasField(String realmClass, String field) {
        ensureInit();
        FieldData fieldData = INSTANCE.classData.getFieldData(realmClass);
        if (fieldData == null) throw ex("\"%s\" is not a valid realm object class name.", realmClass);
        return fieldData.hasField(field);
    }

    /**
     * Check if a {@code field} of a {@code realmClass} is of the given {@code type}. (Note that this method uses {@link
     * Class#isAssignableFrom(Class)} to check if the given {@code type} can be used for the field; that is, {@code
     * type} may be a subclass of the field's actual type.
     * @param realmClass Name of RealmObject subclass which contains the {@code field}.
     * @param field      Name of the field whose type is being checked.
     * @param type       Type class.
     * @return True if {@code type} is assignable to {@code field}'s actual type.
     */
    public static boolean fieldIsOfType(String realmClass, String field, Class<?> type) {
        ensureInit();
        FieldData fieldData = INSTANCE.classData.getFieldData(realmClass);
        if (fieldData == null) throw ex("\"%s\" is not a valid realm object class name.", realmClass);
        Class<?> actualType = fieldData.fieldType(field);
        if (actualType == null) throw ex("\"%s\" is not a valid field name for the class \"%s\".", field,
                realmClass);
        return actualType.isAssignableFrom(type);
    }

    /**
     * Convenience method for throwing an IllegalArgumentException with a formatted string.
     */
    public static IllegalArgumentException ex(String format, Object... args) {
        return new IllegalArgumentException(String.format(format, args));
    }
}
