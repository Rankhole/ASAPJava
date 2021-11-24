package net.sharksystem.asap.RDFComparator;

import net.sharksystem.asap.RDFModel.RDFModel;

;

public interface RDFComparator {

    /**
     * Comparison logic which defines how a word should be compared with your rdf model.
     * @param uri uri
     * @param similarityValue value between 0.0 and 1.0 which describes the threshold for matching
     * @return true, if similarity bigger then threshold, else false
     */
    boolean compareWithRDFModel(String uri, float similarityValue);

    /**
     * Set the rdf model of the comparator.
     * @param rdfModel rdf model
     */
    void setRDFModel(RDFModel rdfModel);
}
