package net.sharksystem.asap;

import net.sharksystem.asap.RDFComparator.RDFComparator;
import net.sharksystem.asap.RDFModel.RDFModel;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class RoutingASAPPeerFS extends ASAPPeerFS implements RoutingASAPPeer{

    RDFModel rdfModel;

    // true = use whitelist, false = use blacklist
    private boolean useWhitelist = true;
    
    private float similarityValue = 0.5f;

    private RDFComparator rdfComparator;

    public RoutingASAPPeerFS(CharSequence owner, CharSequence rootFolder, Collection<CharSequence> supportFormats, RDFModel rdfModel) throws IOException, ASAPException {
        super(owner, rootFolder, supportFormats);
        setRDFModel(rdfModel);
    }

    @Override
    public void setRDFModel(RDFModel rdfModel) {
        this.rdfModel = rdfModel;
    }

    @Override
    public void setRoutingWhitelist(RDFModel rdfModel) {
        this.rdfModel = rdfModel;
        useWhitelistForRouting();
    }

    @Override
    public void setRoutingBlacklist(RDFModel rdfModel) {
        this.rdfModel = rdfModel;
        useBlacklistForRouting();
    }

    @Override
    public void useWhitelistForRouting() {
        useWhitelist = true;
    }

    @Override
    public void useBlacklistForRouting() {
        useWhitelist = false;
    }

    @Override
    public void chunkReceived(String format, String senderE2E, String uri, int era,
                              List<ASAPHop> asapHopList) throws IOException {
        boolean comparisonResult = compareWithRDFModel(uri);
        if((comparisonResult && !useWhitelist) || (!comparisonResult && useWhitelist)) {
            try {
                // Löschen aus Speicher, damit Routing nicht weiter erfolgen kann.
                ASAPStorage asapStorage = getASAPStorage(format);
                asapStorage.getChunkStorage().dropChunks(era);
            } catch (ASAPException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean compareWithRDFModel(String uri) {
        List<String> rdfModelList = rdfModel.getModelAttributesAsList();
        for(String rdfModelAttribute : rdfModelList) {
            float similarityWithUri = compareAttributeWithUri(uri, rdfModelAttribute);
            if (similarityWithUri > similarityValue){
                return true;
            }
        }
        return false;
    }

    public float compareAttributeWithUri(String uri, String rdfModelAttribute) {
        return rdfComparator.compare(uri, rdfModelAttribute);
    }

    public float getSimilarityValue() {
        return similarityValue;
    }

    public void setSimilarityValue(float similarityValue) {
        this.similarityValue = similarityValue;
    }
}
