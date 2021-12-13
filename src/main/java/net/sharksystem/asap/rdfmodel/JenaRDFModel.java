package net.sharksystem.asap.rdfmodel;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class JenaRDFModel implements RDFModel {

    private Model rdfModel = ModelFactory.createDefaultModel();
    private boolean onlyChildren = false;

    public JenaRDFModel() {
        // we just load this file as our model, we don't really need to change it for our tests and usecases.
        InputStream in = FileManager.getInternal().open("src/main/resources/rdfModel.rdf");
        if (in == null) {
            throw new IllegalArgumentException("File: src/main/resources/rdfModel.rdf not found");
        }

        // load model into memory
        rdfModel.read(in, "");
    }

    @Override
    public List<String> getModelResourcesAsList() {
        Set<String> attributeList = new HashSet<>();

        NodeIterator nodeIterator = rdfModel.listObjects();
        while (nodeIterator.hasNext()) {
            RDFNode rdfNode = nodeIterator.next();
            attributeList.add(rdfNode.toString());
        }

        if (!onlyChildren) {
            ResIterator resIterator = rdfModel.listSubjects();
            while (resIterator.hasNext()) {
                RDFNode rdfNode = resIterator.next();
                attributeList.add(rdfNode.toString());
            }
        }

        return new ArrayList<>(attributeList);
    }

    @Override
    public boolean searchUsingQuery(String queryTemplate, String name) throws IOException {
        // read query template file and parse to string
        String queryString = new String(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(queryTemplate)).readAllBytes(), StandardCharsets.UTF_8);
        // replace placeholder with actual search term
        queryString = queryString.replace("#PLACEHOLDER", name);
        // create query
        Query query = QueryFactory.create(queryString);
        // execute query
        QueryExecution qe = QueryExecutionFactory.create(query, rdfModel);
        // obtain results
        ResultSet results = qe.execSelect();
        boolean found = false;
        while (results.hasNext()) {
            QuerySolution querySolution = results.next();
            found = true;
            System.out.println("RDFModel: FOUND QUERY: " + querySolution.toString());
        }
        return found;
    }

    public boolean isOnlyChildren() {
        return onlyChildren;
    }

    public void setOnlyChildren(boolean onlyChildren) {
        this.onlyChildren = onlyChildren;
    }
}
