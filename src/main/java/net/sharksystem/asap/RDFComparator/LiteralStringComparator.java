package net.sharksystem.asap.RDFComparator;

public class LiteralStringComparator implements RDFComparator {

    /**
     * Simply checks if both strings are equal and returns a 1 or 0, representing true or false.
     * @param uri               uri to compare
     * @param rdfModelAttribute attribute to compare
     * @return 1.0 = true, 0.0 = false
     */
    @Override
    public float compare(String uri, String rdfModelAttribute) {
        if(uri.equals(rdfModelAttribute)){
            return 1.0f;
        } else {
            return 0.0f;
        }
    }
}
