package net.sharksystem.asap;

import de.linguatools.disco.CorruptConfigFileException;
import de.linguatools.disco.DISCO;
import de.linguatools.disco.WrongWordspaceTypeException;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.VCARD;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class to test out some libraries, not essential to ASAP at all
 */
public class JenaMain {

    public static DISCO disco;

    static {
        try {
            disco = DISCO.load("src/main/resources/cc.de.300.col.denseMatrix/cc.de.300-COL.denseMatrix");
        } catch (IOException | CorruptConfigFileException e) {
            e.printStackTrace();
        }
    }

    public static double wordSimilarity(String word1, String word2) throws IOException {
        return disco.semanticSimilarity(word1, word2,
                DISCO.getVectorSimilarity(DISCO.SimilarityMeasure.COSINE));
    }

    // main method to test out some stuff and write out the rdf file
    public static void main(String[] args) throws IOException, CorruptConfigFileException, WrongWordspaceTypeException {
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
            throw new IllegalArgumentException("File: src/main/resources/rdfModel.rdf not found");
        }

        // read the RDF/XML file
        model.read(in, "");
        // write it to standard out
        model.write(System.out);

        NodeIterator nodeIterator = model.listObjects();
        while (nodeIterator.hasNext()) {
            RDFNode rdfNode = nodeIterator.next();
            System.out.println(rdfNode);
        }
        ResIterator resIterator = model.listSubjects();
        while (resIterator.hasNext()) {
            RDFNode rdfNode = resIterator.next();
            System.out.println(rdfNode);
        }

        String queryString = "PREFIX vcard: <http://www.w3.org/2001/vcard-rdf/3.0#> " +
                "SELECT ?x WHERE { ?x  vcard:Other \"Eis\" }";
        Query query = QueryFactory.create(queryString);

        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet results = qe.execSelect();
        ResultSetFormatter.out(System.out, results, query);
        qe.close();

        System.out.println(JenaMain.wordSimilarity("Sport", "Schwimmen"));
        System.out.println(JenaMain.wordSimilarity("Sport", "Fleisch"));
        System.out.println(JenaMain.wordSimilarity("Sport", "Eis"));
        System.out.println(JenaMain.wordSimilarity("Mann", "Frau"));
    }
}
