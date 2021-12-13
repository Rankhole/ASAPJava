package net.sharksystem.asap.rdfcomparator;

import net.sharksystem.asap.rdfmodel.RDFModel;

import java.io.IOException;

public class SPARQLComparator implements RDFComparator {

    @Override
    public boolean compareWithRDFModel(String uri, float similarityValue, RDFModel rdfModel) {
        try {
            return rdfModel.searchUsingQuery("queryTemplate.txt", uri);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
