package net.sharksystem.asap.rdfmodel;

import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JenaRDFModel implements RDFModel {

    private Model rdfModel = ModelFactory.createDefaultModel();
    private boolean onlyChildren = false;

    public JenaRDFModel(){
        InputStream in = FileManager.getInternal().open("src/main/resources/rdfModel.rdf");
        if (in == null) {
            throw new IllegalArgumentException( "File: src/main/resources/rdfModel.rdf not found");
        }

        // load model into memory
        rdfModel.read(in, "");
    }

    @Override
    public List<String> getModelAttributesAsList() {
        Set<String> attributeList = new HashSet<>();

        NodeIterator nodeIterator = rdfModel.listObjects();
        while (nodeIterator.hasNext()){
            RDFNode rdfNode = nodeIterator.next();
            attributeList.add(rdfNode.toString());
        }

        if(!onlyChildren){
            ResIterator resIterator = rdfModel.listSubjects();
            while (resIterator.hasNext()){
                RDFNode rdfNode = resIterator.next();
                attributeList.add(rdfNode.toString());
            }
        }

        return new ArrayList<>(attributeList);
    }

    @Override
    public void persistModelToFilesystem() {

    }

    public boolean isOnlyChildren() {
        return onlyChildren;
    }

    public void setOnlyChildren(boolean onlyChildren) {
        this.onlyChildren = onlyChildren;
    }
}
