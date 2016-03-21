package com.bkromhout.ruqus;

import android.content.Context;
import android.os.Build;
import io.realm.RealmObject;
import io.realm.Sort;

import java.util.ArrayList;
import java.util.Date;

/**
 * Access to Ruqus information. This class mostly serves as a convenience class, using the instances of {@link
 * ClassData} and {@link TransformerData} objects that it holds in order to provide static methods which allow the rest
 * of the library to make on-liner calls as much as possible.
 */
public class Ruqus {
    static int LIGHT_TEXT_COLOR, DARK_TEXT_COLOR;
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
                if (noneYet)
                    throw ex("Could not find generated Ruqus transformer data, did the annotation processor run?");
                else break;
            } catch (Exception e) {
                if (noneYet) throw ex("Could not get generated Ruqus transformer data.");
                else break;
            }
        }
    }

    /**
     * Initializes Ruqus. This must be called <i>once</i> before any of the other methods on {@link Ruqus} can be used,
     * and it requires reflection. It is recommended that it be called as early as possible in the application's
     * lifecycle.
     */
    public static void init(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new Ruqus();

            if (Build.VERSION.SDK_INT < 23) {
                //noinspection deprecation
                LIGHT_TEXT_COLOR = context.getResources().getColor(R.color.textColorPrimaryLight);
                //noinspection deprecation
                DARK_TEXT_COLOR = context.getResources().getColor(R.color.textColorPrimaryDark);
            } else {
                LIGHT_TEXT_COLOR = context.getColor(R.color.textColorPrimaryLight);
                DARK_TEXT_COLOR = context.getColor(R.color.textColorPrimaryDark);
            }
        }
    }

    /**
     * Ensures that {@link Ruqus#init()} has been called.
     */
    private static void ensureInit() {
        if (INSTANCE == null) throw ex("Ruqus.init() must be called first.");
    }

    static ClassData getClassData() {
        ensureInit();
        return INSTANCE.classData;
    }

    static FieldData getFieldData(String realmClass) {
        return getClassData().getFieldData(realmClass);
    }

    static TransformerData getTransformerData() {
        ensureInit();
        return INSTANCE.transformerData;
    }

    /**
     * Check whether or not Ruqus has data for a RealmObject subclass with the name {@code realmClass}.
     * @param realmClass Name to check for.
     * @return True if Ruqus knows of a class called {@code realmClass}, otherwise false.
     */
    static boolean knowsOfClass(String realmClass) {
        return getClassData().isValidName(realmClass);
    }

    /**
     * Check that Ruqus recognizes and has data for a RealmObject subclass {@code clazz}.
     * @param realmClass A RealmObject subclass.
     * @return True if we know about the class, otherwise false.
     */
    static boolean knowsOfClass(Class<? extends RealmObject> realmClass) {
        return getClassData().isValidClass(realmClass);
    }

    /**
     * Check that Ruqus recognizes and has data for a transformer called {@code transformer}.
     * @param transformer Name to check for.
     * @return True if Ruqus knows of a transformer called {@code transformer}, otherwise false.
     */
    static boolean knowsOfTransformer(String transformer) {
        return getTransformerData().isValidName(transformer);
    }

    /**
     * Translate the visible name of a RealmObject subclass to its real name.
     * @param visibleName Visible name of a RealmObject subclass.
     * @return Real name of the RealmObject subclass with the given {@code visibleName}.
     */
    static String classNameFromVisibleName(String visibleName) {
        ClassData classData = getClassData();
        return classData.getNames().get(classData.getVisibleNames(false).indexOf(visibleName));
    }

    /**
     * Get the actual class object for the RealmObject subclass with the given real name.
     * @param realmClassName Name of a RealmObject subclass.
     * @return Class object whose name is {@code realmClassName}.
     */
    static Class<? extends RealmObject> getClassFromName(String realmClassName) {
        ClassData classData = getClassData();
        return classData.getClassObj(realmClassName);
    }

    /**
     * Check if {@code realmClass} is marked is Queryable.
     * @param realmClass Class to check.
     * @return True if class is queryable, otherwise false.
     */
    static boolean isClassQueryable(Class<? extends RealmObject> realmClass) {
        ClassData classData = getClassData();
        return classData.isQueryable(realmClass);
    }

    /**
     * Check if a {@code realmClass} has a given {@code field}. This will drill down linked fields, checking all of them
     * along the way.
     * @param realmClass Name of the RealmObject subclass to check.
     * @param field      Name of the field to check for.
     * @return True if {@code realmClass} has {@code field}.
     */
    static boolean classHasField(String realmClass, String field) {
        if (field == null || field.isEmpty()) throw ex("field cannot be non-null or empty.");
        ClassData classData = getClassData();
        FieldData fieldData = classData.getFieldData(realmClass);
        if (fieldData == null) throw ex("\"%s\" is not a valid realm object class name.", realmClass);

        // Split field name up so that we can drill down to the end of any linked fields.
        String[] fieldParts = field.split("\\Q.\\E");
        for (String fieldPart : fieldParts) {
            // Make sure we have this field part.
            if (!fieldData.hasField(fieldPart)) return false;
            // Now, if the field type is RealmObject or RealmList, we'll need to drill down.
            if (fieldData.isRealmListType(fieldPart) || fieldData.isRealmObjectType(fieldPart)) {
                // Try to get it as a realm list type.
                Class clazz = fieldData.realmListType(fieldPart);
                // If that doesn't work, do it the normal way.
                if (clazz == null) clazz = fieldData.fieldType(fieldPart);
                // Either way, we now have something which extends RealmObject. Get the field data for that object so
                // that we can check the next part of the link field in the next iteration.
                // noinspection unchecked
                fieldData = classData.getFieldData(clazz);
                continue;
            }
            // If it isn't, we can return true, because we already checked that we have it.
            return true;
        }
        throw ex("Couldn't verify if \"%\" has field \"%s\".", realmClass, field);
    }

    /**
     * Get the type of {@code field} on {@code realmClass}. If this is a linked field (e.g., the immediate type on the
     * class is a RealmObject subclass or a RealmList of such), this will drill down to the end of the linked field to
     * get the type from the end of it.
     * <p/>
     * For example, if {@code field} is something like "age", and the type for it in {@code realmClass} is Integer,
     * that's what would be returned.<br>But if instead {@code field} was something like "dog.age", where the immediate
     * type is a class called "{@code Dog}" which extends RealmObject and has an Integer field called "age", this method
     * would drill down and find that information, and still return Integer.
     * @param realmClass Name of RealmObject subclass which contains the {@code field}.
     * @param field      Name of the field whose type is being retrieved.
     * @return Type of the field, or the field at the end of the linked field string.
     */
    static Class<?> typeForField(String realmClass, String field) {
        if (field == null || field.isEmpty()) throw ex("field cannot be non-null or empty.");
        ClassData classData = getClassData();
        FieldData fieldData = classData.getFieldData(realmClass);
        if (fieldData == null) throw ex("\"%s\" is not a valid realm object class name.", realmClass);

        // Split field name up so that we can drill down to the end of any linked fields.
        String[] fieldParts = field.split("\\Q.\\E");
        for (String fieldPart : fieldParts) {
            // Try to get it as a realm list type.
            Class clazz = fieldData.realmListType(fieldPart);
            // If that doesn't work, do it the normal way.
            if (clazz == null) clazz = fieldData.fieldType(fieldPart);
            // Now, check to see if this type is a subclass of RealmObject.
            if (RealmObject.class.isAssignableFrom(clazz)) {
                // It is, so we need to get the field data for that type, and we'll try again in the next iteration.
                // noinspection unchecked
                fieldData = classData.getFieldData((Class<? extends RealmObject>) clazz);
            } else {
                // It isn't, so return it.
                return clazz;
            }
        }
        // Shouldn't ever get here, but just in case.
        throw ex("Couldn't get type for \"%s\" on \"%s\".", field, realmClass);
    }

    /**
     * Get the real name of a field on the given class.
     * @param realmClass       Real name of a RealmObject subclass.
     * @param visibleFieldName Visible name of a field.
     * @return Real name of the field on the given class with the given visible field name.
     */
    static String fieldNameFromVisibleName(String realmClass, String visibleFieldName) {
        FieldData fieldData = getFieldData(realmClass);
        return fieldData.getFieldNames().get(fieldData.getVisibleNames().indexOf(visibleFieldName));
    }

    /**
     * Return a list of visible names for all fields on the given RealmObject subclass, but for any fields whose types
     * are either also RealmObject subclass or RealmList, add entries for their fields as well.
     * @param realmClass Name of the RealmObject subclass.
     * @return List of visible flat field names.
     */
    static ArrayList<String> visibleFlatFieldsForClass(String realmClass) {
        ClassData classData = getClassData();
        return _visibleFlatFieldsForClass(classData, classData.getFieldData(realmClass), "");
    }

    private static ArrayList<String> _visibleFlatFieldsForClass(ClassData classData, FieldData fieldData,
                                                                String prepend) {
        ArrayList<String> vNames = new ArrayList<>();
        // Loop through real names.
        for (String name : fieldData.getFieldNames()) {
            // Get visible name of field.
            String visibleName = fieldData.visibleNameOf(name);
            // Do something different based on field's type.
            if (fieldData.isRealmListType(name)) {
                // Field type is RealmList, recurse and get its visible names as well.
                vNames.addAll(_visibleFlatFieldsForClass(classData,
                        classData.getFieldData(fieldData.realmListType(name)), prepend + " > " + visibleName));
            } else if (fieldData.isRealmObjectType(name)) {
                // Field type is RealmObject, recurse and get its visible names as well.
                //noinspection unchecked
                vNames.addAll(_visibleFlatFieldsForClass(classData,
                        classData.getFieldData((Class<? extends RealmObject>) fieldData.fieldType(name)),
                        prepend + " > " + visibleName));
            } else {
                // Normal field, just add its visible name.
                vNames.add(visibleName);
            }
        }
        return vNames;
    }

    /**
     * Takes a visible flat field name and converts it to a real flat field name.
     * @param realmClass       Name of the RealmObject subclass.
     * @param visibleFlatField Visible flat field name.
     * @return Real flat field name.
     */
    static String flatFieldNameFromVisibleFlatFieldName(String realmClass, String visibleFlatField) {
        ClassData classData = getClassData();
        FieldData fieldData = classData.getFieldData(realmClass);
        String[] parts = visibleFlatField.split("\\Q > \\E");
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            // Use the field data to get the real name of the field.
            String realFieldName = fieldData.getFieldNames().get(fieldData.getVisibleNames().indexOf(parts[i]));
            // Append the real name.
            builder.append(realFieldName);
            if (i < parts.length - 1) {
                // This is a RealmObject/RealmList-type field. Append a dot and switch the field data.
                builder.append(".");
                if (fieldData.isRealmListType(realFieldName)) {
                    fieldData = classData.getFieldData(fieldData.realmListType(realFieldName));
                } else {
                    //noinspection unchecked
                    fieldData = classData.getFieldData(
                            (Class<? extends RealmObject>) fieldData.fieldType(realFieldName));
                }
            }
        }
        return builder.toString();
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
    static boolean fieldIsOfType(String realmClass, String field, Class<?> type) {
        FieldData fieldData = getFieldData(realmClass);
        if (fieldData == null) throw ex("\"%s\" is not a valid realm object class name.", realmClass);
        Class<?> actualType = fieldData.fieldType(field);
        if (actualType == null) throw ex("\"%s\" is not a valid field name for the class \"%s\".", field,
                realmClass);
        return actualType.isAssignableFrom(type);
    }

    /**
     * Gets the real name of a transformer class whose visible name is {@code visibleTransName}.
     * @param visibleTransName Visible name of transformer.
     * @param isNoArgs         Whether the transformer is a no-args transformer or not.
     * @return Real name of transformer.
     */
    static String transformerNameFromVisibleName(String visibleTransName, boolean isNoArgs) {
        TransformerData transformerData = getTransformerData();
        return isNoArgs
                ? transformerData.getNoArgNames().get(transformerData.getVisibleNoArgNames().indexOf(visibleTransName))
                : transformerData.getNames().get(transformerData.getVisibleNames().indexOf(visibleTransName));
    }

    /**
     * Gets the number of arguments which the transformer whose name is {@code transformerName} accepts.
     * @param transformerName Real name of transformer.
     * @return Number of arguments accepted. May be {@link C#VAR_ARGS}, which equates to -1.
     */
    static int numberOfArgsFor(String transformerName) {
        return getTransformerData().numArgsOf(transformerName);
    }

    /**
     * Whether or not the transformer whose name is {@code transformerName} accepts the given {@code type}.
     * @param transformerName Real name of a normal transformer.
     * @param type            Type to check for.
     * @return True if the transformer with the given name accepts the given type, otherwise false.
     */
    static boolean transformerAcceptsType(String transformerName, Class type) {
        return getTransformerData().acceptsType(transformerName, type);
    }

    /**
     * Get an instance of the transformer whose real name is {@code transformerName}.
     * @param transformerName Fully-qualified name of the transformer class to get an instance of.
     * @return Instance of the transformer class with the given name.
     */
    static RUQTransformer getTransformer(String transformerName) {
        return getTransformerData().getTransformer(transformerName);
    }

    /**
     * Gets a "pretty" version of the sort direction based on a field's type.
     * @param fieldName Field name.
     * @param sortDir   Sort direction.
     * @return Pretty sort direction string.
     */
    static String prettySortDirStringForField(String realmClass, String fieldName, Sort sortDir) {
        Class fieldType = typeForField(realmClass, fieldName);
        if (sortDir == Sort.ASCENDING) {
            // Ascending direction.
            if (Boolean.class.isAssignableFrom(fieldType)) return "False before True";
            else if (Date.class.isAssignableFrom(fieldType)) return "Earliest to Latest";
            else if (Number.class.isAssignableFrom(fieldType)) return "Lowest to Highest";
            else if (String.class.isAssignableFrom(fieldType)) return "a to Z";
            else throw new IllegalArgumentException("Invalid field type.");
        } else {
            // Descending direction.
            if (Boolean.class.isAssignableFrom(fieldType)) return "True before False";
            else if (Date.class.isAssignableFrom(fieldType)) return "Latest to Earliest";
            else if (Number.class.isAssignableFrom(fieldType)) return "Highest to Lowest";
            else if (String.class.isAssignableFrom(fieldType)) return "Z to a";
            else throw new IllegalArgumentException("Invalid field type.");
        }
    }

    /**
     * Returns an array which contains two "pretty" sort strings.
     * @param realmClass Name of the RealmObject subclass.
     * @param fieldName  Field name.
     * @return String array like {[asc pretty name], [desc pretty name]}.
     */
    static String[] prettySortDirStringsForField(String realmClass, String fieldName) {
        Class fieldType = typeForField(realmClass, fieldName);
        if (Boolean.class.isAssignableFrom(fieldType))
            return new String[] {"False before True", "True before False"};
        else if (Date.class.isAssignableFrom(fieldType))
            return new String[] {"Earliest to Latest", "Latest to Earliest"};
        else if (Number.class.isAssignableFrom(fieldType))
            return new String[] {"Lowest to Highest", "Highest to Lowest"};
        else if (String.class.isAssignableFrom(fieldType))
            return new String[] {"a to Z", "Z to a"};
        else throw new IllegalArgumentException("Invalid field type.");
    }

    /**
     * Convenience method for throwing an IllegalArgumentException with a formatted string.
     */
    static IllegalArgumentException ex(String format, Object... args) {
        return new IllegalArgumentException(String.format(format, args));
    }
}
