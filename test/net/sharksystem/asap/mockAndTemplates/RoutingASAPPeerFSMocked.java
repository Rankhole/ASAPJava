package net.sharksystem.asap.mockAndTemplates;

import net.sharksystem.asap.*;
import net.sharksystem.asap.engine.ASAPInternalPeer;
import net.sharksystem.asap.rdfcomparator.RDFComparator;
import net.sharksystem.asap.rdfmodel.RDFModel;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collection;

public class RoutingASAPPeerFSMocked extends RoutingASAPPeerFS {

    //singleton object
    private static ASAPStorage mockedASAPStorage;

    public RoutingASAPPeerFSMocked(CharSequence owner, CharSequence rootFolder, Collection<CharSequence> supportFormats) throws IOException, ASAPException {
        super(owner, rootFolder, supportFormats);
    }

    public RoutingASAPPeerFSMocked(CharSequence owner, CharSequence rootFolder, Collection<CharSequence> supportFormats, RDFComparator rdfComparator, RDFModel rdfModel) throws IOException, ASAPException {
        super(owner, rootFolder, supportFormats, rdfComparator, rdfModel);
    }

    // singleton pattern
    public static ASAPStorage getMockedASAPStorage() throws IOException, ASAPException {
        if (mockedASAPStorage == null){
            mockedASAPStorage = Mockito.mock(ASAPStorage.class);
            ASAPChunkStorage mockedChunkStorage = Mockito.mock(ASAPChunkStorage.class);
            Mockito.when(mockedASAPStorage.getExistingIncomingStorage(Mockito.any())).thenReturn(mockedASAPStorage);
            Mockito.when(mockedASAPStorage.getChunkStorage()).thenReturn(mockedChunkStorage);
        }
        return mockedASAPStorage;
    }

    @Override
    public ASAPStorage getASAPStorage(CharSequence format) throws IOException, ASAPException {
        // inject singleton into ASAPPeer class
        return RoutingASAPPeerFSMocked.getMockedASAPStorage();
    }
}
