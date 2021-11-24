package net.sharksystem.asap.RDFComparator;

import net.sharksystem.asap.RDFModel.RDFModel;

public class LiteralStringComparator extends RDFComparatorTemplateImpl {
    public LiteralStringComparator(RDFModel rdfModel) {
        super(rdfModel);
    }

    @Override
    public float compareAttributes(String uri, String rdfModelAttribute) {
        if(uri.equals(rdfModelAttribute)){
            return 1.0f;
        } else {
            return 0.0f;
        }
    }
}
