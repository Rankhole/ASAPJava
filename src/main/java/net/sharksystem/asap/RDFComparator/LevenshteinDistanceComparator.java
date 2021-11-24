package net.sharksystem.asap.RDFComparator;

import org.apache.commons.text.similarity.LevenshteinDistance;

public class LevenshteinDistanceComparator implements RDFComparator {
    @Override
    public float compare(String uri, String rdfModelAttribute) {
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
