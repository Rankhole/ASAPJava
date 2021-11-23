package net.sharksystem.asap.RDFComparator;

/**
 * This interface can be used to implement comparison algorithms.
 */
public interface RDFComparator {
    /**
     * Compare uri to the rdf model attribute. Implementation can be any given algorithm.
     *
     * @param uri               uri to compare
     * @param rdfModelAttribute attribute to compare
     * @return float representing similarity. perfect match = 1.0f, no relation = 0.0f (use instead of returning true/false)
     */
    float compare(String uri, String rdfModelAttribute);
}
