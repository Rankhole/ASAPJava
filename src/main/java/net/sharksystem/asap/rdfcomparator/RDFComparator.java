package net.sharksystem.asap.rdfcomparator;

import net.sharksystem.asap.rdfmodel.RDFModel;

public interface RDFComparator {

    /**
     * Comparison logic which defines how a word should be compared with your rdf model.
     * @param uri uri
     * @param similarityValue value between 0.0 and 1.0 which describes the threshold for matching
     * @return true, if similarity bigger then threshold, else false
     */
    boolean compareWithRDFModel(String uri, float similarityValue, RDFModel model);

}
