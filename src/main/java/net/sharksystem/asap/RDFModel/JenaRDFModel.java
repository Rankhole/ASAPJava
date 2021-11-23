package net.sharksystem.asap.RDFModel;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
        rdfModel.listStatements();
        return null;
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
