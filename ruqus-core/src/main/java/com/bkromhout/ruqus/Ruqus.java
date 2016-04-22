package com.bkromhout.ruqus;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import io.realm.RealmModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Access to Ruqus information. This class mostly serves as a convenience class, using the instances of {@link
 * ClassData} and {@link TransformerData} objects that it holds in order to provide static methods which allow the rest
 * of the library to make on-liner calls as much as possible.
 */
public class Ruqus {
    private static final String KEY_SEP = "$";
    private static final String FLAT_SEP = ".";
    private static final String VIS_FLAT_SEP = ">";
    private static final Pattern KEY_SEP_PATTERN = Pattern.compile("\\Q" + KEY_SEP + "\\E");
    private static final Pattern FLAT_SEP_PATTERN = Pattern.compile("\\Q" + FLAT_SEP + "\\E");
    private static final Pattern VIS_SEP_PATTERN = Pattern.compile("\\Q" + VIS_FLAT_SEP + "\\E");

    @ColorInt
    static int LIGHT_TEXT_COLOR, DARK_TEXT_COLOR, LIGHT_CARD_COLOR, DARK_CARD_COLOR;
    static String CHOOSE_FIELD, CHOOSE_CONDITIONAL;
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
    /**
     * Used to cache information to speed up converting a flat visible field name string to a flat real field name
     * string.
     */
    private HashMap<String, String> flatVisFieldToFlatField;
    /**
     * Used to cache information to speed up converting a flat real field name to a field type.
     */
    private HashMap<String, FieldType> flatFieldToFieldType;

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
        // Create hashmaps to use for caching.
        flatVisFieldToFlatField = new HashMap<>();
        flatFieldToFieldType = new HashMap<>();
    }

    /**
     * Initializes Ruqus. This must be called <i>once</i> before any of the other methods on {@link Ruqus} can be used,
     * and it requires reflection. It is recommended that it be called as early as possible in the application's
     * lifecycle.
     */
    public static void init(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new Ruqus();

            LIGHT_TEXT_COLOR = ContextCompat.getColor(context, R.color.ruqus_textColorPrimaryLight);
            DARK_TEXT_COLOR = ContextCompat.getColor(context, R.color.ruqus_textColorPrimaryDark);
            LIGHT_CARD_COLOR = ContextCompat.getColor(context, R.color.cardview_light_background);
            DARK_CARD_COLOR = ContextCompat.getColor(context, R.color.cardview_dark_background);

            CHOOSE_FIELD = context.getString(R.string.ruqus_choose_field);
            CHOOSE_CONDITIONAL = context.getString(R.string.ruqus_choose_conditional);
        }
    }

    /**
     * Ensures that {@link Ruqus#init(Context)} has been called.
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
     * Check whether or not Ruqus has data for a RealmModel subclass with the name {@code realmClass}.
     * @param realmClass Name to check for.
     * @return True if Ruqus knows of a class called {@code realmClass}, otherwise false.
     */
    static boolean knowsOfClass(String realmClass) {
        return getClassData().isValidName(realmClass);
    }

    /**
     * Check that Ruqus recognizes and has data for a RealmModel subclass {@code clazz}.
     * @param realmClass A RealmModel subclass.
     * @return True if we know about the class, otherwise false.
     */
    static boolean knowsOfClass(Class<? extends RealmModel> realmClass) {
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
     * Translate the visible name of a RealmModel subclass to its real name.
     * @param visibleName Visible name of a RealmModel subclass.
     * @return Real name of the RealmModel subclass with the given {@code visibleName}.
     */
    static String classNameFromVisibleName(String visibleName) {
        ClassData classData = getClassData();
        return classData.getNames().get(classData.getVisibleNames(false).indexOf(visibleName));
    }

    /**
     * Get the actual class object for the RealmModel subclass with the given real name.
     * @param realmClassName Name of a RealmModel subclass.
     * @return Class object whose name is {@code realmClassName}.
     */
    static Class<? extends RealmModel> getClassFromName(String realmClassName) {
        ClassData classData = getClassData();
        return classData.getClassObj(realmClassName);
    }

    /**
     * Check if {@code realmClass} is marked is Queryable.
     * @param realmClass Class to check.
     * @return True if class is queryable, otherwise false.
     */
    static boolean isClassQueryable(Class<? extends RealmModel> realmClass) {
        ClassData classData = getClassData();
        return classData.isQueryable(realmClass);
    }

    /**
     * Check if a {@code realmClass} has a given {@code field}. This will drill down linked fields, checking all of them
     * along the way.
     * @param realmClass Name of the RealmModel subclass to check.
     * @param field      Name of the field to check for.
     * @return True if {@code realmClass} has {@code field}.
     */
    static boolean classHasField(String realmClass, String field) {
        if (field == null || field.isEmpty()) throw ex("field cannot be non-null or empty.");
        ClassData classData = getClassData();
        FieldData fieldData = classData.getFieldData(realmClass);
        if (fieldData == null) throw ex("\"%s\" is not a valid realm object class name.", realmClass);

        // Split field name up so that we can drill down to the end of any linked fields.
        String[] fieldParts = FLAT_SEP_PATTERN.split(field);
        for (String fieldPart : fieldParts) {
            // Make sure we have this field part.
            if (!fieldData.hasField(fieldPart)) return false;
            // Now, if the field type is RealmModel or RealmList, we'll need to drill down.
            if (fieldData.isRealmListType(fieldPart) || fieldData.isRealmModelType(fieldPart)) {
                // Try to get it as a realm list type.
                Class clazz = fieldData.realmListType(fieldPart);
                // If that doesn't work, do it the normal way.
                if (clazz == null) clazz = fieldData.fieldType(fieldPart);
                // Either way, we now have something which extends RealmModel. Get the field data for that object so
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
     * Get the enum type of a [flat-]field's type. If this is a flat-field (e.g., the immediate type on the class is a
     * RealmModel subclass or a RealmList of such), this will drill down to the end of the flat-field to get the type
     * from the end of it.
     * <p/>
     * For example, if {@code field} is something like "age", and the type for it in {@code realmClass} is Integer,
     * that's what would be returned.<br>But if instead {@code field} was something like "dog.age", where the immediate
     * type is a class called "{@code Dog}" which extends RealmModel and has an Integer field called "age", this method
     * would drill down and find that information, and still return Integer.
     * <p/>
     * Caches values for quicker future access.
     * @param realmClass Name of RealmModel subclass which contains the {@code field}.
     * @param field      Name of the field whose type is being retrieved.
     * @return Enum type of the field at the end of a flat-field.
     */
    static FieldType typeEnumForField(String realmClass, String field) {
        if (field == null || field.isEmpty()) throw ex("field cannot be non-null or empty.");
        ensureInit();
        String key = realmClass + KEY_SEP + field;
        if (INSTANCE.flatFieldToFieldType.containsKey(key)) return INSTANCE.flatFieldToFieldType.get(key);
        else {
            ClassData classData = getClassData();
            FieldData fieldData = classData.getFieldData(realmClass);
            if (fieldData == null) throw ex("\"%s\" is not a valid realm object class name.", realmClass);
            // Split field name up so that we can drill down to the end of any linked fields.
            String[] fieldParts = FLAT_SEP_PATTERN.split(field);
            Class fieldTypeClazz = null;
            for (String fieldPart : fieldParts) {
                // Try to get it as a realm list type.
                fieldTypeClazz = fieldData.realmListType(fieldPart);
                // If that doesn't work, do it the normal way.
                if (fieldTypeClazz == null) fieldTypeClazz = fieldData.fieldType(fieldPart);
                // If that still didn't work, we have an issue.
                if (fieldTypeClazz == null) throw ex("Couldn't get type for \"%s\" on \"%s\".", field, realmClass);
                // Now, check to see if this type is a subclass of RealmModel.
                if (RealmModel.class.isAssignableFrom(fieldTypeClazz)) {
                    // It is, so we need to get the field data for that type, and we'll try again in the next iteration.
                    // noinspection unchecked
                    fieldData = classData.getFieldData((Class<? extends RealmModel>) fieldTypeClazz);
                }
            }
            FieldType fieldType = FieldType.fromClazz(fieldTypeClazz);
            INSTANCE.flatFieldToFieldType.put(key, fieldType);
            return fieldType;
        }
    }

    /**
     * Return a list of visible names for all fields on the given RealmModel subclass, except those which are a
     * RealmModel subclass or RealmList type.
     * @param realmClass Name of the RealmModel subclass.
     * @return List of visible field names.
     */
    static ArrayList<String> visibleNonRealmFieldsForClass(String realmClass) {
        FieldData fieldData = getFieldData(realmClass);
        ArrayList<String> fields = fieldData.getFieldNames();
        Iterator<String> fieldIterator = fields.iterator();
        while (fieldIterator.hasNext()) {
            String field = fieldIterator.next();
            if (fieldData.isRealmListType(field) || fieldData.isRealmModelType(field)) fieldIterator.remove();
        }
        ArrayList<String> visFields = new ArrayList<>();
        for (String field : fields) visFields.add(fieldData.visibleNameOf(field));
        return visFields;
    }

    /**
     * Return a list of visible names for all fields on the given RealmModel subclass, as well as any sub-fields (The
     * fields from any of {@code realmClass}'s fields whose types are either RealmModel subclass or RealmList).
     * @param realmClass Name of the RealmModel subclass.
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
                        classData.getFieldData(fieldData.realmListType(name)),
                        prepend.isEmpty() ? visibleName : prepend + VIS_FLAT_SEP + visibleName));
            } else if (fieldData.isRealmModelType(name)) {
                // Field type is RealmModel, recurse and get its visible names as well.
                //noinspection unchecked
                vNames.addAll(_visibleFlatFieldsForClass(classData,
                        classData.getFieldData((Class<? extends RealmModel>) fieldData.fieldType(name)),
                        prepend.isEmpty() ? visibleName : prepend + VIS_FLAT_SEP + visibleName));
            } else {
                // Normal field, just add its visible name (preceded by our current prepend string and the separator,
                // if the prepend string isn't empty.
                vNames.add(prepend.isEmpty() ? visibleName : prepend + VIS_FLAT_SEP + visibleName);
            }
        }
        return vNames;
    }

    /**
     * Takes a visible flat field name and converts it to a real flat field name.
     * @param realmClass       Name of the RealmModel subclass.
     * @param visibleFieldName Visible flat field name.
     * @return Real flat field name.
     */
    static String fieldFromVisibleField(String realmClass, String visibleFieldName) {
        ensureInit();
        String key = realmClass + KEY_SEP + visibleFieldName;
        // Try to get cached value first.
        if (INSTANCE.flatVisFieldToFlatField.containsKey(key))
            return stripKey(INSTANCE.flatVisFieldToFlatField.get(key));

        // If not cached, must go figure it out.
        ClassData classData = getClassData();
        FieldData fieldData = classData.getFieldData(realmClass);
        String[] parts = VIS_SEP_PATTERN.split(visibleFieldName);
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            // Use the field data to get the real name of the field.
            String realFieldName = fieldData.getFieldNames().get(fieldData.getVisibleNames().indexOf(parts[i]));
            // Append the real name.
            builder.append(realFieldName);
            if (i < parts.length - 1) {
                // This is a RealmModel/RealmList-type field. Append a dot and switch the field data.
                builder.append(FLAT_SEP);
                if (fieldData.isRealmListType(realFieldName)) {
                    fieldData = classData.getFieldData(fieldData.realmListType(realFieldName));
                } else {
                    //noinspection unchecked
                    fieldData = classData.getFieldData(
                            (Class<? extends RealmModel>) fieldData.fieldType(realFieldName));
                }
            }
        }
        // Cache this before returning it.
        String value = builder.toString();
        INSTANCE.flatVisFieldToFlatField.put(key, realmClass + KEY_SEP + value);
        return value;
    }

    /**
     * Takes a real flat field name and converts it to a visible flat field name.
     * @param realmClass Name of the RealmModel subclass.
     * @param field      Real flat field name.
     * @return Visible flat field name.
     */
    static String visibleFieldFromField(String realmClass, String field) {
        ensureInit();
        String value = realmClass + "$" + field;
        // Try to get cached key first.
        if (INSTANCE.flatVisFieldToFlatField.containsValue(value)) {
            for (Map.Entry<String, String> entry : INSTANCE.flatVisFieldToFlatField.entrySet()) {
                if (entry.getValue().equals(value)) return stripKey(entry.getKey());
            }
        }

        // If not cached, must go figure it out.
        StringBuilder builder = new StringBuilder();
        String className = realmClass;
        String[] parts = FLAT_SEP_PATTERN.split(field);
        for (int i = 0; i < parts.length; i++) {
            if (i != parts.length - 1) {
                // Not at the end of the link yet.
                className = parts[i];
                builder.append(INSTANCE.classData.visibleNameOf(parts[i]))
                       .append(VIS_FLAT_SEP);
            } else {
                // This is the end of the link.
                builder.append(getFieldData(className).visibleNameOf(parts[i]));
            }
        }
        // Cache this before returning it.
        String key = builder.toString();
        INSTANCE.flatVisFieldToFlatField.put(realmClass + KEY_SEP + key, value);
        return key;
    }

    /**
     * Check if a {@code field} of a {@code realmClass} is of the given {@code type}. (Note that this method uses {@link
     * Class#isAssignableFrom(Class)} to check if the given {@code type} can be used for the field; that is, {@code
     * type} may be a subclass of the field's actual type.
     * @param realmClass Name of RealmModel subclass which contains the {@code field}.
     * @param field      Name of the field whose type is being checked.
     * @param type       Type class.
     * @return True if {@code type} is assignable to {@code field}'s actual type.
     */
    static boolean fieldIsOfType(String realmClass, String field, FieldType type) {
        ensureInit();
        FieldType actualType = typeEnumForField(realmClass, field);
        if (actualType == null) throw ex("\"%s\" is not a valid field name for the class \"%s\".", field,
                realmClass);
        return actualType == type;
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
     * Strip the leading "[string]$" off of a string, if it contains a "$".
     * @param keyedString String to strip.
     * @return Stripped string.
     */
    private static String stripKey(String keyedString) {
        return keyedString.contains(KEY_SEP) ? KEY_SEP_PATTERN.split(keyedString)[1] : keyedString;
    }

    /**
     * Convenience method for throwing an IllegalArgumentException with a formatted string.
     */
    private static IllegalArgumentException ex(String format, Object... args) {
        return new IllegalArgumentException(String.format(format, args));
    }
}
