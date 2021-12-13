package net.sharksystem.asap.rdfcomparator;

import net.sharksystem.asap.rdfmodel.RDFModel;

import java.util.List;


/**
 * Template method pattern for comparators.
 */
public abstract class RDFComparatorTemplateImpl implements RDFComparator {

    @Override
    public boolean compareWithRDFModel(String uri, float similarityValue, RDFModel model) {
        // load up all the models as a list
        List<String> rdfModelList = model.getModelResourcesAsList();
        for (String rdfModelAttribute : rdfModelList) {
            // compare the uri with each attribute in the list
            float similarityWithUri = compareAttributes(uri, rdfModelAttribute);
            if (similarityWithUri >= similarityValue) {
                return true;
            }
        }
        return false;
    }

    public abstract float compareAttributes(String uri, String rdfModelAttribute);
}
