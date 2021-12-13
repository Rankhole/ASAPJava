package net.sharksystem.asap.rdfcomparator;

import de.linguatools.disco.CorruptConfigFileException;
import de.linguatools.disco.DISCO;

import java.io.IOException;

public class SemanticComparator extends RDFComparatorTemplateImpl {

    // follow instructions on Github to make sure you have this file
    private DISCO disco = DISCO.load("src/main/resources/cc.de.300.col.denseMatrix/cc.de.300-COL.denseMatrix");

    public SemanticComparator() throws IOException, CorruptConfigFileException {
    }

    @Override
    public float compareAttributes(String uri, String rdfModelAttribute) {
        try {
            // calculate semantic similarity using the loaded word vector file and the disco tool
            return disco.semanticSimilarity(uri, rdfModelAttribute,
                    DISCO.getVectorSimilarity(DISCO.SimilarityMeasure.COSINE));
        } catch (IOException e) {
            e.printStackTrace();
            return -1f;
        }
    }
}
