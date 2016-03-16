package com.bkromhout.ruqus;

/**
 * Access to Ruqus information.
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
            throw new IllegalStateException(
                    "Could not find generated Ruqus class data, did the annotation processor run?");
        } catch (Exception e) {
            throw new IllegalStateException("Could not get generated Ruqus class data.");
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
                if (noneYet) throw new IllegalStateException(
                        "Could not find generated Ruqus class data, did the annotation processor run?");
                else break;
            } catch (Exception e) {
                if (noneYet) throw new IllegalStateException("Could not get generated Ruqus class data.");
                else break;
            }
        }
    }

    public static void init() {
        if (INSTANCE == null) INSTANCE = new Ruqus();
    }

    private static void ensureInit() {
        if (INSTANCE == null) throw new IllegalArgumentException("Ruqus.init() must be called first.");
    }

    public static ClassData getClassData() {
        ensureInit();
        return INSTANCE.classData;
    }

    public static TransformerData getTransformerData() {
        ensureInit();
        return INSTANCE.transformerData;
    }
}
