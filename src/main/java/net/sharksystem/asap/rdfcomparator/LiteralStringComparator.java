package net.sharksystem.asap.rdfcomparator;

public class LiteralStringComparator extends RDFComparatorTemplateImpl {
    @Override
    public float compareAttributes(String uri, String rdfModelAttribute) {
        if(uri.equals(rdfModelAttribute)){
            return 1.0f;
        } else {
            return 0.0f;
        }
    }
}
