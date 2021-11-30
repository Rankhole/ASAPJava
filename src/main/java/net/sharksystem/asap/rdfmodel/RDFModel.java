package net.sharksystem.asap.rdfmodel;

import java.util.List;

public interface RDFModel {

    /**
     * Return a list of all rdf model attributes. This is used for the compare methods to calculate the similarity of
     * the URI and the attributes in the model.
     *
     * @return List of strings with model attributes
     */
    List<String> getModelAttributesAsList();

    void persistModelToFilesystem();
}
