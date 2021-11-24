package net.sharksystem.asap;

import de.linguatools.disco.*;
import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.VCARD;

import javax.swing.plaf.nimbus.State;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JenaMain {

    public static DISCO disco;

    static {
        try {
            disco = DISCO.load("src/main/resources/cc.de.300.col.denseMatrix/cc.de.300-COL.denseMatrix");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CorruptConfigFileException e) {
            e.printStackTrace();
        }
    }

    public static double wordSimilarity(String word1, String word2) throws IOException, CorruptConfigFileException {
        return disco.semanticSimilarity(word1, word2,
                DISCO.getVectorSimilarity(DISCO.SimilarityMeasure.COSINE));
    }

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

        System.out.println(JenaMain.wordSimilarity("Sport", "Schwimmen"));
        System.out.println(JenaMain.wordSimilarity("Sport", "Fleisch"));
        System.out.println(JenaMain.wordSimilarity("Sport", "Eis"));
        System.out.println(JenaMain.wordSimilarity("Mann", "Frau"));
    }
}
