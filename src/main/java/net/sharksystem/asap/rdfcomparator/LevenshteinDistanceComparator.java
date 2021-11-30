package net.sharksystem.asap.rdfcomparator;

import net.sharksystem.asap.rdfmodel.RDFModel;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class LevenshteinDistanceComparator extends RDFComparatorTemplateImpl {
    public LevenshteinDistanceComparator(RDFModel rdfModel) {
        super(rdfModel);
    }

    @Override
    public float compareAttributes(String uri, String rdfModelAttribute) {
        String longerString = uri, shorterString = rdfModelAttribute;
        if (longerString.length() < shorterString.length()) {
            longerString = rdfModelAttribute;
            shorterString = uri;
        }

        if (longerString.length() == 0) {
            // in diesem Fall sind beide leer, also gleich
            return 1.0f;
        }

        int distance = new LevenshteinDistance().apply(longerString, shorterString);

        // Prozentsatz der Ähnlichkeit basierend auf der Levenshtein Distanz der Strings
        return (longerString.length() - distance) / (float) longerString.length();
    }
}
