package net.sharksystem.asap.rdfmodel;

import java.io.IOException;
import java.util.List;

public interface RDFModel {

    /**
     * Return a list of all rdf model resources. This is used for the compare methods to calculate the similarity of
     * the URI and the attributes in the model.
     *
     * @return List of strings with model attributes
     */
    List<String> getModelResourcesAsList();

    /**
     * This is defined as a search directly within the RDFModel using some sort of query language. The idea
     * is to give the name of the queryTemplate-file which you want to use and the term you search for, which gets
     * substituted in the template.
     *
     * @param queryTemplate name of template file
     * @param name search term
     * @return true if query found the string, else false
     * @throws IOException in case query goes bad
     */
    boolean searchUsingQuery(String queryTemplate, String name) throws IOException;

}
