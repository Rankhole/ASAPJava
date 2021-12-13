package net.sharksystem.asap;

import net.sharksystem.asap.rdfcomparator.RDFComparator;
import net.sharksystem.asap.rdfmodel.RDFModel;

public interface RoutingASAPPeer extends ASAPPeer {
    /**
     * Set the base data which incoming message URI's are matched with. Default Routing should be with whitelist.
     */
    void setRDFModel(RDFModel rdfModel);

    /**
     * Returns the peers current RDF model.
     */
    RDFModel getRDFModel();

    /**
     * Set the Routing to whitelist with the given RDFModel
     *
     * @param rdfModel rdf model implementation
     */
    default void setRoutingWhitelist(RDFModel rdfModel) {
        this.useWhitelistForRouting();
        this.setRDFModel(rdfModel);
    }

    /**
     * Set the Routing to blacklist with the given RDFModel
     *
     * @param rdfModel rdf model implementation
     */
    default void setRoutingBlacklist(RDFModel rdfModel) {
        this.useBlacklistForRouting();
        this.setRDFModel(rdfModel);
    }

    /**
     * Sets Routing to whitelist mode
     */
    void useWhitelistForRouting();

    /**
     * Sets Routing to blacklist mode
     */
    void useBlacklistForRouting();

    /**
     * Returns similarity value, which is the threshold for similarity to match two strings
     *
     * @return similarity value
     */
    float getSimilarityValue();

    /**
     * Set similarity value, which is the threshold for similarity to match two strings
     *
     * @param similarityValue similarity value
     */
    void setSimilarityValue(float similarityValue);

    void setRDFComparator(RDFComparator rdfComparator);
}
