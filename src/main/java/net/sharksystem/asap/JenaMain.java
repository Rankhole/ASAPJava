package net.sharksystem.asap;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.VCARD;

import java.io.*;
import java.util.Objects;

public class JenaMain {
    public static void main(String[] args) throws FileNotFoundException {
        // create an empty Model
        Model model = ModelFactory.createDefaultModel();

        // create the resource
        //   and add the properties cascading style
        Resource sportarten
                = model.createResource("sn://Sportarten")
                .addProperty(VCARD.Other, "Basketball")
                .addProperty(VCARD.Other, "Fußball")
                .addProperty(VCARD.Other, "Baseball")
                .addProperty(VCARD.Other, "Schwimmen");

        Resource nahrung = model.createResource("sn://Essen")
                .addProperty(VCARD.Other, "Fleisch")
                .addProperty(VCARD.Other, "Eis")
                .addProperty(VCARD.Other, "Grillen");

        Resource politik = model.createResource("sn://Politik")
                .addProperty(VCARD.Other, "Grüne")
                .addProperty(VCARD.Other, "SPD");

        model.write(new FileOutputStream("src/main/resources/rdfModel.rdf"));

        model = ModelFactory.createDefaultModel();

        InputStream in = FileManager.getInternal().open("src/main/resources/rdfModel.rdf");
        if (in == null) {
            throw new IllegalArgumentException( "File: src/main/resources/rdfModel.rdf not found");
        }

        // read the RDF/XML file
        model.read(in, "");
        // write it to standard out
        model.write(System.out);

        StmtIterator iter = model.listStatements();
        while (iter.hasNext()){
            System.out.println(iter.);
            iter.nextStatement();
        }
    }
}
