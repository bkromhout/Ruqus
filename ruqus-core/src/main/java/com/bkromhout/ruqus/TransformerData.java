package com.bkromhout.ruqus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Generated extended class will contain data about all classes annotated with {@link Transformer}.
 */
abstract class TransformerData {
    /**
     * List of fully-qualified names for all transformers which aren't no-arg transformers.
     */
    protected static HashSet<String> realNames = new HashSet<>();
    /**
     * List of fully-qualified names for all no-arg transformers.
     */
    protected static HashSet<String> realNoArgNames = new HashSet<>();
    /**
     * Maps normal transformers' fully-qualified names to their visible names.
     */
    protected static HashMap<String, String> visibleNames = new HashMap<>();
    /**
     * Maps no-arg transformers' fully-qualified names to their visible names.
     */
    protected static HashMap<String, String> visibleNoArgNames = new HashMap<>();
    /**
     * Maps normal transformers' fully-qualified names to the number of arguments they need.
     */
    protected static HashMap<String, Integer> numArgs = new HashMap<>();
    /**
     * Maps all transformer's fully-qualified names to an instance of them.
     */
    protected static HashMap<String, RUQTransformer> instanceMap = new HashMap<>();
    /**
     * Maps types to the fully-qualified names of transformers which accept those types.
     */
    protected static HashMap<Class, HashSet<String>> typesToNames = new HashMap<>();
    /**
     * Maps types to the visible names of transformers which accept those types.
     */
    protected static HashMap<Class, HashSet<String>> typesToVisibleNames = new HashMap<>();

    /**
     * Get a list of all normal transformers' real names.
     * @return List of normal transformer names.
     */
    ArrayList<String> getNames() {
        return new ArrayList<>(realNames);
    }

    /**
     * Get a list of all normal transformers' real names which accept the given {@code type}.
     * @param typeAccepted Type which transformers whose names are returned must accept.
     * @return List of normal transformer names which accept {@code typeAccepted}. Might be empty.
     */
    ArrayList<String> getNames(Class typeAccepted) {
        return typesToNames.containsKey(typeAccepted) ? new ArrayList<>(typesToNames.get(typeAccepted)) :
                new ArrayList<String>();
    }

    /**
     * Get a list of all no arg transformers' real names.
     * @return List of no arg transformer names.
     */
    ArrayList<String> getNoArgNames() {
        return new ArrayList<>(realNoArgNames);
    }

    /**
     * Get a list of all normal transformers' visible names.
     * @return List of normal transformer visible names.
     */
    ArrayList<String> getVisibleNames() {
        return new ArrayList<>(visibleNames.values());
    }

    /**
     * Get a list of all normal transformers' visible names which accept the given {@code type}.
     * @param typeAccepted Type which transformers whose visible names are returned must accept.
     * @return List of normal transformer visible names which accept {@code typeAccepted}. Might be empty.
     */
    ArrayList<String> getVisibleNames(Class typeAccepted) {
        return typesToVisibleNames.containsKey(typeAccepted) ? new ArrayList<>(typesToVisibleNames.get(typeAccepted)) :
                new ArrayList<String>();
    }

    /**
     * Get a list of all no arg transformers' visible names.
     * @return List of no arg transformer visible names.
     */
    ArrayList<String> getVisibleNoArgNames() {
        return new ArrayList<>(visibleNoArgNames.values());
    }

    /**
     * Check that Ruqus recognizes and has data for a transformer called {@code transformerName}.
     * @param transformerName Real transformer name.
     * @return True if we know about the transformer with the given name, otherwise false.
     */
    boolean isValidName(String transformerName) {
        return instanceMap.containsKey(transformerName);
    }

    /**
     * Check if the transformer whose real name is {@code transformerName} is a no arg transformer.
     * @param transformerName Real transformer name.
     * @return True if the transformer is a no arg transformer, otherwise false.
     */
    boolean isNoArgs(String transformerName) {
        return realNoArgNames.contains(transformerName);
    }

    /**
     * Get the visible name for the transformer whose real name is {@code transformerName}.
     * @param transformerName Real transformer name.
     * @return Visible name.
     */
    String visibleNameOf(String transformerName) {
        return isNoArgs(transformerName) ? visibleNoArgNames.get(transformerName) : visibleNames.get(transformerName);
    }

    /**
     * Get the number of arguments accepted by the transformer whose real name is {@code transformerName}.
     * @param transformerName Real transformer name.
     * @return Number of arguments. Might be {@link C#VAR_ARGS}, which equates to -1. Will return 0 if transformer is a
     * no-args transformer.
     */
    int numArgsOf(String transformerName) {
        return isNoArgs(transformerName) ? 0 : numArgs.get(transformerName);
    }

    /**
     * Get the cached instance of the transformer class named {@code transformerName}.
     * @param transformerName Fully-qualified name of the transformer class to get instance of.
     * @return Instance of transformer class.
     */
    RUQTransformer getTransformer(String transformerName) {
        return instanceMap.get(transformerName);
    }

    /**
     * Get the class object for the transformer whose real name is {@code transformerName}.
     * @param transformerName Real transformer name.
     * @return Transformer class.
     */
    Class<? extends RUQTransformer> getTransformerClass(String transformerName) {
        return instanceMap.get(transformerName).getClass();
    }

    /**
     * Whether or not the transformer whose name is {@code transformerName} accepts the given {@code type}.
     * @param transformerName Real name of a normal transformer.
     * @param type            Type to check for.
     * @return True if the transformer with the given name accepts the given type, otherwise false.
     */
    boolean acceptsType(String transformerName, Class type) {
        return typesToNames.containsKey(type) && typesToNames.get(type).contains(transformerName);
    }
}
