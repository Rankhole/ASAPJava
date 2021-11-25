package net.sharksystem.asap;

import de.linguatools.disco.CorruptConfigFileException;
import net.sharksystem.asap.RDFComparator.*;
import net.sharksystem.asap.RDFModel.JenaRDFModel;
import net.sharksystem.asap.RDFModel.RDFModel;
import net.sharksystem.asap.engine.ASAPEngineFS;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class RoutingASAPPeerFS extends ASAPPeerFS implements RoutingASAPPeer{

    RDFComparator rdfComparator;

    // true = use whitelist, false = use blacklist
    private boolean useWhitelist = false;
    
    private float similarityValue = 0.5f;

    public RoutingASAPPeerFS(CharSequence owner, CharSequence rootFolder, Collection<CharSequence> supportFormats) throws IOException, ASAPException {
        super(owner, rootFolder, supportFormats);
        rdfComparator = new LiteralStringComparator(new JenaRDFModel());
    }

    public RoutingASAPPeerFS(CharSequence owner, CharSequence rootFolder, Collection<CharSequence> supportFormats, RDFComparator rdfComparator) throws IOException, ASAPException {
        super(owner, rootFolder, supportFormats);
        this.rdfComparator = rdfComparator;
    }

    @Override
    public void setRDFModel(RDFModel rdfModel) {
        this.rdfComparator.setRDFModel(rdfModel);
    }

    @Override
    public void setRoutingWhitelist(RDFModel rdfModel) {
        this.rdfComparator.setRDFModel(rdfModel);
        useWhitelistForRouting();
    }

    @Override
    public void setRoutingBlacklist(RDFModel rdfModel) {
        this.rdfComparator.setRDFModel(rdfModel);
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
        boolean comparisonResult = this.rdfComparator.compareWithRDFModel(uri, similarityValue);
        if((comparisonResult && !useWhitelist) || (!comparisonResult && useWhitelist)) {
            try {
                // Löschen aus Speicher, damit Routing nicht weiter erfolgen kann.
                ASAPStorage asapStorage = getASAPStorage(format);
                String rootdir = asapStorage.getChunkStorage().getRootDirectory();
                ASAPEngineFS.removeFolder(rootdir + "/" + senderE2E + "/" + era);
            } catch (ASAPException e) {
                e.printStackTrace();
            }
        }
        super.chunkReceived(format, senderE2E, uri, era, asapHopList);
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
