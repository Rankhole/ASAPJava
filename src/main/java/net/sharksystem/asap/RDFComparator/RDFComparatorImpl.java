package net.sharksystem.asap.RDFComparator;

import net.sharksystem.asap.RDFModel.RDFModel;

import java.util.List;

public abstract class RDFComparatorImpl implements RDFComparator {

    RDFModel rdfModel;

    public RDFComparatorImpl(RDFModel rdfModel){
        this.rdfModel = rdfModel;
    }

    @Override
    public boolean compareWithRDFModel(String uri, float similarityValue) {
        List<String> rdfModelList = rdfModel.getModelAttributesAsList();
        for(String rdfModelAttribute : rdfModelList) {
            float similarityWithUri = compareAttributes(uri, rdfModelAttribute);
            if (similarityWithUri > similarityValue){
                return true;
            }
        }
        return false;
    }

    public abstract float compareAttributes(String uri, String rdfModelAttribute);

    @Override
    public void setRDFModel(RDFModel rdfModel) {
        this.rdfModel = rdfModel;
    }

}
