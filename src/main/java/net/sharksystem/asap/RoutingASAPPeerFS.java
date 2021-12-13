package net.sharksystem.asap;

import net.sharksystem.asap.rdfcomparator.LiteralStringComparator;
import net.sharksystem.asap.rdfcomparator.RDFComparator;
import net.sharksystem.asap.rdfmodel.JenaRDFModel;
import net.sharksystem.asap.rdfmodel.RDFModel;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class RoutingASAPPeerFS extends ASAPPeerFS implements RoutingASAPPeer {

    RDFComparator rdfComparator;
    RDFModel rdfModel;

    // true = use whitelist, false = use blacklist
    private boolean useWhitelist = false;

    private float similarityValue = 0.5f;

    public RoutingASAPPeerFS(CharSequence owner, CharSequence rootFolder, Collection<CharSequence> supportFormats) throws IOException, ASAPException {
        super(owner, rootFolder, supportFormats);
        rdfComparator = new LiteralStringComparator();
        rdfModel = new JenaRDFModel();
    }

    public RoutingASAPPeerFS(CharSequence owner, CharSequence rootFolder, Collection<CharSequence> supportFormats, RDFComparator rdfComparator, RDFModel rdfModel) throws IOException, ASAPException {
        super(owner, rootFolder, supportFormats);
        this.rdfComparator = rdfComparator;
        this.rdfModel = rdfModel;
    }

    @Override
    public void setRDFModel(RDFModel rdfModel) {
        this.rdfModel = rdfModel;
    }

    @Override
    public RDFModel getRDFModel() {
        return this.rdfModel;
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
        boolean comparisonResult = this.rdfComparator.compareWithRDFModel(uri, similarityValue, this.rdfModel);
        if ((comparisonResult && !useWhitelist) || (!comparisonResult && useWhitelist)) {
            try {
                // Löschen aus Speicher, damit Routing nicht weiter erfolgen kann.
                ASAPStorage asapStorage = getASAPStorage(format);
                asapStorage.getExistingIncomingStorage(senderE2E).getChunkStorage().dropChunks(era);
            } catch (ASAPException e) {
                e.printStackTrace();
            }
        }
        //super.chunkReceived(format, senderE2E, uri, era, asapHopList);
    }

    public float getSimilarityValue() {
        return similarityValue;
    }

    public void setSimilarityValue(float similarityValue) {
        this.similarityValue = similarityValue;
    }

    @Override
    public void setRDFComparator(RDFComparator rdfComparator) {
        this.rdfComparator = rdfComparator;
    }

}
