package net.sharksystem.asap.RDFComparator;

import de.linguatools.disco.CorruptConfigFileException;
import de.linguatools.disco.DISCO;
import net.sharksystem.asap.RDFModel.RDFModel;

import java.io.IOException;

public class SemanticComparator extends RDFComparatorTemplateImpl {

    private DISCO disco = DISCO.load("src/main/resources/cc.de.300.col.denseMatrix/cc.de.300-COL.denseMatrix");

    public SemanticComparator(RDFModel rdfModel) throws IOException, CorruptConfigFileException {
        super(rdfModel);
    }

    @Override
    public float compareAttributes(String uri, String rdfModelAttribute) {
        try {
            return disco.semanticSimilarity(uri, rdfModelAttribute,
                    DISCO.getVectorSimilarity(DISCO.SimilarityMeasure.COSINE));
        } catch (IOException e) {
            e.printStackTrace();
            return -1f;
        }
    }
}
