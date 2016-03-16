package com.bkromhout.ruqus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Generated extended class will contain data about all classes annotated with {@link Transformer}.
 */
public abstract class TransformerData {
    /**
     * List of real names for all transformers which aren't no arg transformers.
     */
    protected static HashSet<String> realNames = new HashSet<>();
    /**
     * List of real names for all no arg transformers.
     */
    protected static HashSet<String> realNoArgNames = new HashSet<>();
    /**
     * List of visible names for all normal transformers.
     */
    protected static HashMap<String, String> visibleNames = new HashMap<>();
    /**
     * List of visible names for all no arg transformers.
     */
    protected static HashMap<String, String> visibleNoArgNames = new HashMap<>();
    /**
     * Maps normal transformers' real names to the number of arguments they need.
     */
    protected static HashMap<String, Integer> numArgs = new HashMap<>();
    /**
     * Maps all transformers' names to their classes.
     */
    protected static HashMap<String, Class<? extends RUQTransformer>> classMap = new HashMap<>();
    /**
     * Maps types to the transformers which accept those types.
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
    public ArrayList<String> getNames() {
        return new ArrayList<>(realNames);
    }

    /**
     * Get a list of all normal transformers' real names which accept the given {@code type}.
     * @param typeAccepted Type which transformers whose names are returned must accept.
     * @return List of normal transformer names which accept {@code typeAccepted}. Might be empty.
     */
    public ArrayList<String> getNames(Class typeAccepted) {
        return typesToNames.containsKey(typeAccepted) ? new ArrayList<>(typesToNames.get(typeAccepted)) :
                new ArrayList<String>();
    }

    /**
     * Get a list of all no arg transformers' real names.
     * @return List of no arg transformer names.
     */
    public ArrayList<String> getNoArgNames() {
        return new ArrayList<>(realNoArgNames);
    }

    /**
     * Get a list of all normal transformers' visible names.
     * @return List of normal transformer visible names.
     */
    public ArrayList<String> getVisibleNames() {
        return new ArrayList<>(visibleNames.values());
    }

    /**
     * Get a list of all normal transformers' visible names which accept the given {@code type}.
     * @param typeAccepted Type which transformers whose visible names are returned must accept.
     * @return List of normal transformer visible names which accept {@code typeAccepted}. Might be empty.
     */
    public ArrayList<String> getVisibleNames(Class typeAccepted) {
        return typesToVisibleNames.containsKey(typeAccepted) ? new ArrayList<>(typesToVisibleNames.get(typeAccepted)) :
                new ArrayList<String>();
    }

    /**
     * Get a list of all no arg transformers' visible names.
     * @return List of no arg transformer visible names.
     */
    public ArrayList<String> getVisibleNoArgNames() {
        return new ArrayList<>(visibleNoArgNames.values());
    }

    /**
     * Check if the transformer whose real name is {@code transformerName} is a no arg transformer.
     * @param transformerName Real transformer name (Can be a normal or no arg transformer).
     * @return True if the transformer is a no arg transformer, otherwise false.
     */
    public boolean isNoArg(String transformerName) {
        return realNoArgNames.contains(transformerName);
    }

    /**
     * Get the visible name for the transformer whose real name is {@code transformerName}.
     * @param transformerName Real transformer name.
     * @param isNoArgs        If {@code transformerName} is the name of a no-args transformer or not.
     * @return Visible name.
     */
    public String visibleNameOf(String transformerName, boolean isNoArgs) {
        return isNoArgs ? visibleNoArgNames.get(transformerName) : visibleNames.get(transformerName);
    }

    /**
     * Get the number of arguments accepted by the transformer whose real name is {@code transformerName}.
     * @param transformerName Real transformer name (Normal transformers only).
     * @return Number of arguments. Might be {@link C#VAR_ARGS}, which equates to -1.
     */
    public int numArgsOf(String transformerName) {
        return numArgs.get(transformerName);
    }

    /**
     * Get the class object for the transformer whose real name is {@code transformerName}.
     * @param transformerName Real transformer name (Can be a normal or no arg transformer).
     * @return Transformer class.
     */
    public Class<? extends RUQTransformer> getTransformer(String transformerName) {
        return classMap.get(transformerName);
    }
}
