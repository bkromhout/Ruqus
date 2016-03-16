package com.bkromhout.ruqus;

/**
 * Access to Ruqus information.
 * <p/>
 * TODO flesh out; most likely just going to be getters.
 */
public class Ruqus {
    /**
     * Ruqus class information.
     */
    private static final ClassData classData;
    /**
     * Ruqus transformer information.
     */
    private static final TransformerData transformerData;

    static {
        // Load the Ruqus class data object.
        try {
            classData = (ClassData) Class.forName(C.GEN_PKG_PREFIX + C.GEN_CLASS_DATA_CLASS_NAME).newInstance();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Could not find generated Ruqus class data, did the annotation processor run?");
        } catch (Exception e) {
            throw new IllegalStateException("Could not get generated Ruqus class data.");
        }

        // Load the Ruqus transformer data object.
        try {
            transformerData = (TransformerData) Class.forName(C.GEN_PKG_PREFIX + C.GEN_TRANSFORMER_DATA_CLASS_NAME)
                                                     .newInstance();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Could not find generated Ruqus class data, did the annotation processor run?");
        } catch (Exception e) {
            throw new IllegalStateException("Could not get generated Ruqus class data.");
        }
    }

}
