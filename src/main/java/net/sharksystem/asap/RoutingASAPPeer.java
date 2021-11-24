package net.sharksystem.asap;

import net.sharksystem.asap.RDFComparator.RDFComparator;
import net.sharksystem.asap.RDFModel.RDFModel;

public interface RoutingASAPPeer extends ASAPPeer{
    /**
     * Set the base data which incoming message URI's are matched with. Default Routing should be with whitelist.
     */
    void setRDFModel(RDFModel rdfModel);

    /**
     * Set the Routing to whitelist with the given RDFModel
     * @param rdfModel rdf model implementation
     */
    void setRoutingWhitelist(RDFModel rdfModel);

    /**
     * Set the Routing to blacklist with the given RDFModel
     * @param rdfModel rdf model implementation
     */
    void setRoutingBlacklist(RDFModel rdfModel);

    /**
     * Sets Routing to whitelist mode
     */
    void useWhitelistForRouting();

    /**
     * Sets Routing to blacklist mode
     */
    void useBlacklistForRouting();

    float getSimilarityValue();

    void setSimilarityValue(float similarityValue);

    void setRDFComparator(RDFComparator rdfComparator);
}
