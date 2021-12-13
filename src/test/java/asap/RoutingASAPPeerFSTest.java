package asap;

import asap.mockAndTemplates.RoutingASAPPeerFSMocked;
import de.linguatools.disco.CorruptConfigFileException;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPPeer;
import net.sharksystem.asap.RoutingASAPPeerFS;
import asap.mockAndTemplates.ASAPMessageReceivedListenerExample;
import net.sharksystem.asap.rdfcomparator.*;
import net.sharksystem.asap.rdfmodel.JenaRDFModel;
import net.sharksystem.asap.rdfmodel.RDFModel;
import net.sharksystem.asap.testsupport.ASAPRoutingTestPeerFS;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.ArgumentMatchers.eq;

public class RoutingASAPPeerFSTest {

    private ASAPRoutingTestPeerFS aliceRoutingTestPeer, bobRoutingTestPeer, claraRoutingTestPeer;
    private RoutingASAPPeerFS unitTestPeer;
    private RDFModel rdfModel;
    private RDFComparator rdfComparator;

    private String TEST_URI = "sn://test";

    private Collection<CharSequence> formats;

    private CharSequence format = "ASAPRouter";

    String match = "MATCH", nomatch = "NO_MATCH";

    private static final int PORT = 7777;

    private static int port = 0;

    private static int timesCalledCounter = 0;

    static int getPortNumber() {
        if (RoutingASAPPeerFSTest.port == 0) {
            RoutingASAPPeerFSTest.port = PORT;
        } else {
            RoutingASAPPeerFSTest.port++;
        }

        return RoutingASAPPeerFSTest.port;
    }


    @BeforeEach
    public void setUp() throws IOException, ASAPException {
        // delete directory „testPeerFS” to prevent errors when running twice
        FileUtils.deleteDirectory(new File("testPeerFS"));

        // root folder of all ASAPPeers
        String rootfolder = "./testPeerFS";

        formats = new ArrayList<>();
        formats.add(format);

        // default model found under: src/main/resources/rdfModel.rdf
        this.rdfModel = new JenaRDFModel();

        // default comparator: literal string matcher
        this.rdfComparator = new LiteralStringComparator();

        // ASAPRoutingTestPeerFS is the same as ASAPTestPeerFS with a RoutingASAPPeerFS instead of ASAPPeerFS
        this.aliceRoutingTestPeer = new ASAPRoutingTestPeerFS("ALICE", rootfolder + "/ALICE", formats, rdfComparator, rdfModel);
        this.bobRoutingTestPeer = new ASAPRoutingTestPeerFS("BOB", rootfolder + "/BOB", formats, rdfComparator, rdfModel);
        this.claraRoutingTestPeer = new ASAPRoutingTestPeerFS("CLARA", rootfolder + "/CLARA", formats, rdfComparator, rdfModel);

        aliceRoutingTestPeer.useBlacklistForRouting();
        bobRoutingTestPeer.useBlacklistForRouting();
        claraRoutingTestPeer.useBlacklistForRouting();

        //setup mocked unit testcase class
        unitTestPeer = new RoutingASAPPeerFSMocked("UnitTest", "./testPeerFS/UnitTest", formats);
        RDFComparator mockedRDFComparator = Mockito.mock(RDFComparator.class);
        RDFModel mockedRDFModel = Mockito.mock(RDFModel.class);

        match = "MATCH";
        nomatch = "NO_MATCH";

        Mockito.when(mockedRDFComparator
                        .compareWithRDFModel(eq(match), Mockito.anyFloat(), Mockito.any()))
                .thenReturn(true);
        Mockito.when(mockedRDFComparator
                        .compareWithRDFModel(eq(nomatch), Mockito.anyFloat(), Mockito.any()))
                .thenReturn(false);

        unitTestPeer.setRDFComparator(mockedRDFComparator);
        unitTestPeer.setRDFModel(mockedRDFModel);
    }

    @Test
    public void chunkRecievedTest_whitelistMatch() throws IOException, ASAPException {
        // these are unit tests for the crucial code of the RoutingASAPPeerFS
        // case1: whitelist & match
        unitTestPeer.useWhitelistForRouting();
        unitTestPeer.chunkReceived(format.toString(), "somesender", match, 0, null);
        verifyDeletionCalled(0);
    }

    private void verifyDeletionCalled(int i) throws IOException, ASAPException {
        int expectedTimesToBeCalled = i + timesCalledCounter;
        if (i != 0) timesCalledCounter++;
        Mockito.verify(RoutingASAPPeerFSMocked.getMockedASAPStorage(), Mockito.times(expectedTimesToBeCalled)).getExistingIncomingStorage("somesender");
    }

    @Test
    public void chunkRecievedTest_whitelistNoMatch() throws IOException, ASAPException {
        // case2: whitelist & no match
        unitTestPeer.useWhitelistForRouting();
        unitTestPeer.chunkReceived(format.toString(), "somesender", nomatch, 0, null);
        verifyDeletionCalled(1);

    }

    @Test
    public void chunkRecievedTest_blacklistMatch() throws IOException, ASAPException {
        // case3: blacklist & match
        unitTestPeer.useBlacklistForRouting();
        unitTestPeer.chunkReceived(format.toString(), "somesender", match, 0, null);
        verifyDeletionCalled(1);

    }

    @Test
    public void chunkRecievedTest_blacklistNoMatch() throws IOException, ASAPException {
        // case4: blacklist & no match
        unitTestPeer.useBlacklistForRouting();
        unitTestPeer.chunkReceived(format.toString(), "somesender", nomatch, 0, null);
        verifyDeletionCalled(0);

    }

    @Test
    public void singleEncounter_noBlacklistEntries() throws InterruptedException, IOException, ASAPException {
        String uriAlice = "Tier";
        String uriBob = "Wasser";

        simpleEncounterWithMessageExchange(uriAlice, uriBob);

        // chunks should be created for each send message
        // this will only be checked on single encounter tests, as in further tests this gets more and more tedious to do
        Assertions.assertTrue(aliceRoutingTestPeer.getASAPStorage(format).getChunkStorage().existsChunk(uriAlice, 0));
        Assertions.assertTrue(bobRoutingTestPeer.getASAPStorage(format).getChunkStorage().existsChunk(uriBob, 0));
        Assertions.assertTrue(aliceRoutingTestPeer.getASAPStorage(format).getChunkStorage().existsChunk(uriBob, 1));
        Assertions.assertTrue(bobRoutingTestPeer.getASAPStorage(format).getChunkStorage().existsChunk(uriAlice, 1));

        // each message should have created a new era, so there should be a meta and content file in each subfolder
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uriBob, 0));
        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uriAlice, 0));
    }

    @Test
    public void singleEncounter_withBlacklistEntriesBySinglePeer() throws InterruptedException, IOException, ASAPException {
        String uriAlice = "Eis";
        String uriBob = "Wasser";

        simpleEncounterWithMessageExchange(uriAlice, uriBob);

        // chunks should be created for each send message
        // this will only be checked on single encounter tests, as in further tests this gets more and more tedious to do
        Assertions.assertTrue(aliceRoutingTestPeer.getASAPStorage(format).getChunkStorage().existsChunk(uriAlice, 0));
        Assertions.assertTrue(bobRoutingTestPeer.getASAPStorage(format).getChunkStorage().existsChunk(uriBob, 0));
        Assertions.assertTrue(aliceRoutingTestPeer.getASAPStorage(format).getChunkStorage().existsChunk(uriBob, 1));
        Assertions.assertTrue(bobRoutingTestPeer.getASAPStorage(format).getChunkStorage().existsChunk(uriAlice, 1));

        // Bob should not have saved alice's message, but alice should have saved bob's message
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uriBob, 0));
        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uriAlice, 0));
    }

    @Test
    public void singleEncounter_withBlacklistEntriesByBothPeers() throws InterruptedException, IOException, ASAPException {
        String uriAlice = "Eis";
        String uriBob = "Fleisch";

        simpleEncounterWithMessageExchange(uriAlice, uriBob);

        // chunks should be created for each send message
        // this will only be checked on single encounter tests, as in further tests this gets more and more tedious to do
        Assertions.assertTrue(aliceRoutingTestPeer.getASAPStorage(format).getChunkStorage().existsChunk(uriAlice, 0));
        Assertions.assertTrue(bobRoutingTestPeer.getASAPStorage(format).getChunkStorage().existsChunk(uriBob, 0));
        Assertions.assertTrue(aliceRoutingTestPeer.getASAPStorage(format).getChunkStorage().existsChunk(uriBob, 1));
        Assertions.assertTrue(bobRoutingTestPeer.getASAPStorage(format).getChunkStorage().existsChunk(uriAlice, 1));

        // Both peers should not have saved each others messages
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uriBob, 0));
        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uriAlice, 0));
    }

    @Test
    public void singleEncounter_withWhitelistNoMatchByBothPeers() throws InterruptedException, IOException, ASAPException {
        aliceRoutingTestPeer.useWhitelistForRouting();
        bobRoutingTestPeer.useWhitelistForRouting();
        // not whitelisted!
        String uriAlice = "Wasser";
        String uriBob = "Feuer";

        simpleEncounterWithMessageExchange(uriAlice, uriBob);

        // chunks should be created for each send message
        // this will only be checked on single encounter tests, as in further tests this gets more and more tedious to do
        Assertions.assertTrue(aliceRoutingTestPeer.getASAPStorage(format).getChunkStorage().existsChunk(uriAlice, 0));
        Assertions.assertTrue(bobRoutingTestPeer.getASAPStorage(format).getChunkStorage().existsChunk(uriBob, 0));
        Assertions.assertTrue(aliceRoutingTestPeer.getASAPStorage(format).getChunkStorage().existsChunk(uriBob, 1));
        Assertions.assertTrue(bobRoutingTestPeer.getASAPStorage(format).getChunkStorage().existsChunk(uriAlice, 1));

        // Both peers should not have saved each others messages
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uriBob, 0));
        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uriAlice, 0));
    }

    @Test
    public void multiEncounter_onlyBlacklistEntriesSent() throws InterruptedException, IOException, ASAPException {

        //this is in the rdf model of both peers
        String uri = "Eis";

        simpleEncounterWithMessageExchange(uri, uri);

        simpleEncounterWithMessageExchange(uri, uri);

        simpleEncounterWithMessageExchange(uri, uri);

        simpleEncounterWithMessageExchange(uri, uri);

        // each message should have created a new era, so there should be a meta and content file in each subfolder
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 0));
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 1));
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 2));
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 3));

        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 0));
        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 1));
        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 2));
        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 3));
    }

    @Test
    public void multiEncounter_onlyNonBlacklistEntriesSent() throws InterruptedException, IOException, ASAPException {

        String uri = "Pinguin";

        simpleEncounterWithMessageExchange(uri, uri);

        simpleEncounterWithMessageExchange(uri, uri);

        simpleEncounterWithMessageExchange(uri, uri);

        simpleEncounterWithMessageExchange(uri, uri);

        // each message should have created a new era, so there should be a meta and content file in each subfolder
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 0));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 1));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 2));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 3));

        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 0));
        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 1));
        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 2));
        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 3));
    }

    @Test
    public void multiEncounter_partialBlacklistEntriesSent() throws InterruptedException, IOException, ASAPException {

        // this is blacklisted, should be deleted out of incoming storage
        String uri = "Eis";

        simpleEncounterWithMessageExchange("Tiger", uri);

        simpleEncounterWithMessageExchange(uri, "Elefant");

        simpleEncounterWithMessageExchange("Hallo", uri);

        simpleEncounterWithMessageExchange(uri, "Hallo");

        // expected: no era 0 and era 2 of Bob, no era 1 and 3 of Alice
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 0));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", "Elefant", 1));
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 2));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", "Hallo", 3));

        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", "Tiger", 0));
        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 1));
        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", "Hallo", 2));
        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 3));
    }

    @Test
    public void multiEncounter_noBlacklistEntriesWithDifferentUrisSent() throws InterruptedException, IOException, ASAPException {

        // not on blacklist, so all entries should persist (should be the same as ASAPPeerFS)
        String uri = "Pinguin";

        simpleEncounterWithMessageExchange("Tiger", uri);

        simpleEncounterWithMessageExchange(uri, "Elefant");

        simpleEncounterWithMessageExchange("Hallo", uri);

        simpleEncounterWithMessageExchange(uri, "Hallo");

        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 0));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", "Elefant", 1));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 2));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", "Hallo", 3));

        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", "Tiger", 0));
        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 1));
        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", "Hallo", 2));
        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 3));
    }

    @Test
    public void multiEncounter_WhitelistNoMatchWithDifferentUrisSent() throws InterruptedException, IOException, ASAPException {
        aliceRoutingTestPeer.useWhitelistForRouting();
        bobRoutingTestPeer.useWhitelistForRouting();
        // not on blacklist, so all entries should persist (should be the same as ASAPPeerFS)
        String uri = "Pinguin";

        simpleEncounterWithMessageExchange("Tiger", uri);

        simpleEncounterWithMessageExchange(uri, "Elefant");

        simpleEncounterWithMessageExchange("Hallo", uri);

        simpleEncounterWithMessageExchange(uri, "Hallo");

        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 0));
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", "Elefant", 1));
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 2));
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", "Hallo", 3));

        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", "Tiger", 0));
        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 1));
        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", "Hallo", 2));
        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 3));
    }

    @Test
    public void sparqlComparator_nonBlacklistEntry() throws InterruptedException, IOException, ASAPException {
        aliceRoutingTestPeer.setRDFComparator(new SPARQLComparator());
        bobRoutingTestPeer.setRDFComparator(new SPARQLComparator());
        // not on blacklist, so all entries should persist (should be the same as ASAPPeerFS)
        String uri = "Pinguin";

        simpleEncounterWithMessageExchange("Tiger", uri);

        simpleEncounterWithMessageExchange(uri, "Elefant");

        simpleEncounterWithMessageExchange("Hallo", uri);

        simpleEncounterWithMessageExchange(uri, "Hallo");

        // expected: ALL eras should exist on both sides
        // actual: alice missing „Elefant”, bob missing the last „Pinguin”...
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 0));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", "Elefant", 1));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 2));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", "Hallo", 3));

        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", "Tiger", 0));
        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 1));
        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", "Hallo", 2));
        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 3));
    }

    @Test
    public void sparqlComparator_blacklistEntry() throws InterruptedException, IOException, ASAPException {
        aliceRoutingTestPeer.setRDFComparator(new SPARQLComparator());
        bobRoutingTestPeer.setRDFComparator(new SPARQLComparator());
        // this is blacklisted, should be deleted out of incoming storage
        String uri = "Eis";

        simpleEncounterWithMessageExchange("Tiger", uri);

        simpleEncounterWithMessageExchange(uri, "Elefant");

        simpleEncounterWithMessageExchange("Hallo", uri);

        simpleEncounterWithMessageExchange(uri, "Hallo");

        // expected: no era 0 and era 2 of Bob, no era 1 and 3 of Alice
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 0));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", "Elefant", 1));
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 2));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", "Hallo", 3));

        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", "Tiger", 0));
        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 1));
        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", "Hallo", 2));
        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 3));
    }

    @Test
    public void levenshteinDistanceComparator_nonBlacklistEntry() throws InterruptedException, IOException, ASAPException {
        aliceRoutingTestPeer.setRDFComparator(new LevenshteinDistanceComparator());
        bobRoutingTestPeer.setRDFComparator(new LevenshteinDistanceComparator());
        // not on blacklist, so all entries should persist (should be the same as ASAPPeerFS)
        String uri = "Pinguin";

        simpleEncounterWithMessageExchange("Tiger", uri);

        simpleEncounterWithMessageExchange(uri, "Elefant");

        simpleEncounterWithMessageExchange("Hallo", uri);

        simpleEncounterWithMessageExchange(uri, "Hallo");

        // expected: ALL eras should exist on both sides
        // actual: alice missing „Elefant”, bob missing the last „Pinguin”...
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 0));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", "Elefant", 1));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 2));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", "Hallo", 3));

        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", "Tiger", 0));
        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 1));
        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", "Hallo", 2));
        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 3));
    }

    @Test
    public void levenshteinDistanceComparator_blacklistEntry() throws InterruptedException, IOException, ASAPException {
        aliceRoutingTestPeer.setRDFComparator(new LevenshteinDistanceComparator());
        bobRoutingTestPeer.setRDFComparator(new LevenshteinDistanceComparator());
        // this is blacklisted, should be deleted out of incoming storage
        String uri = "Eis";

        simpleEncounterWithMessageExchange("Tiger", uri);

        simpleEncounterWithMessageExchange(uri, "Elefant");

        simpleEncounterWithMessageExchange("Hallo", uri);

        simpleEncounterWithMessageExchange(uri, "Hallo");

        // expected: no era 0 and era 2 of Bob, no era 1 and 3 of Alice
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 0));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", "Elefant", 1));
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 2));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", "Hallo", 3));

        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", "Tiger", 0));
        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 1));
        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", "Hallo", 2));
        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 3));
    }

    @Test
    public void semanticComparator_blacklistEntry() throws InterruptedException, IOException, ASAPException, CorruptConfigFileException {
        aliceRoutingTestPeer.setRDFComparator(new SemanticComparator());
        bobRoutingTestPeer.setRDFComparator(new SemanticComparator());
        // technically not on blacklist, but very semantically close!
        // fire || ice
        String uri = "Feuer";

        simpleEncounterWithMessageExchange("Tiger", uri);

        simpleEncounterWithMessageExchange(uri, "Elefant");

        simpleEncounterWithMessageExchange("Hallo", uri);

        simpleEncounterWithMessageExchange(uri, "Hallo");

        // expected: ALL eras should exist on both sides
        // actual: alice missing „Elefant”, bob missing the last „Pinguin”...
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 0));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", "Elefant", 1));
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 2));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", "Hallo", 3));

        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", "Tiger", 0));
        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 1));
        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", "Hallo", 2));
        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 3));
    }

    @Test
    public void multihopTests_blacklistEntry() throws IOException, ASAPException, InterruptedException {
        // this is blacklisted, should be deleted out of incoming storage
        String uri = "Eis";

        simpleEncounterWithMessageExchange("Tiger", uri);

        simpleEncounterWithMessageExchange(uri, "Elefant");

        simpleEncounterWithMessageExchange("Hallo", uri);

        simpleEncounterWithMessageExchange(uri, "Hallo");

        // expected: no era 0 and era 2 of Bob, no era 1 and 3 of Alice
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 0));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", "Elefant", 1));
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 2));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", "Hallo", 3));

        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", "Tiger", 0));
        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 1));
        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", "Hallo", 2));
        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 3));

        Assertions.assertTrue(aliceRoutingTestPeer.isASAPRoutingAllowed(format));
        // exchange between alice and clara
        simpleEncounterWithMessageExchange(aliceRoutingTestPeer, claraRoutingTestPeer, "HelloToClara", "FromClara");
        // Alice should have received message from Clara
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "CLARA", "FromClara", 0));
        // messages from Alice should have arrived at Clara
        Assertions.assertTrue(senderEraShouldExist(claraRoutingTestPeer, "ALICE", "Tiger", 0));
        Assertions.assertFalse(senderEraShouldExist(claraRoutingTestPeer, "ALICE", uri, 1));
        Assertions.assertTrue(senderEraShouldExist(claraRoutingTestPeer, "ALICE", "Hallo", 2));
        Assertions.assertFalse(senderEraShouldExist(claraRoutingTestPeer, "ALICE", uri, 3));
        Assertions.assertTrue(senderEraShouldExist(claraRoutingTestPeer, "ALICE", "HelloToClara", 4));

        // messages from Bob, which Alice had previously received, should have arrived at Clara
        // BUG: only the first message is routed
        Assertions.assertFalse(senderEraShouldExist(claraRoutingTestPeer, "BOB", uri, 0));
        Assertions.assertTrue(senderEraShouldExist(claraRoutingTestPeer, "BOB", "Elefant", 1));
        Assertions.assertFalse(senderEraShouldExist(claraRoutingTestPeer, "BOB", uri, 2));
        Assertions.assertTrue(senderEraShouldExist(claraRoutingTestPeer, "BOB", "Hallo", 3));
    }

    @Test
    public void multihopTests_nonBlacklistEntry() throws IOException, ASAPException, InterruptedException {
        String uri = "Pinguin";

        simpleEncounterWithMessageExchange("Tiger", uri);

        simpleEncounterWithMessageExchange(uri, "Elefant");

        simpleEncounterWithMessageExchange("Hallo", uri);

        simpleEncounterWithMessageExchange(uri, "Hallo");

        // expected: no era 0 and era 2 of Bob, no era 1 and 3 of Alice
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 0));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", "Elefant", 1));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 2));
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "BOB", "Hallo", 3));

        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", "Tiger", 0));
        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 1));
        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", "Hallo", 2));
        Assertions.assertTrue(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 3));

        Assertions.assertTrue(aliceRoutingTestPeer.isASAPRoutingAllowed(format));
        // exchange between alice and clara
        simpleEncounterWithMessageExchange(aliceRoutingTestPeer, claraRoutingTestPeer, "HelloToClara", "FromClara");
        // Alice should have received message from Clara
        Assertions.assertTrue(senderEraShouldExist(aliceRoutingTestPeer, "CLARA", "FromClara", 0));
        // messages from Alice should have arrived at Clara
        Assertions.assertTrue(senderEraShouldExist(claraRoutingTestPeer, "ALICE", "Tiger", 0));
        Assertions.assertTrue(senderEraShouldExist(claraRoutingTestPeer, "ALICE", uri, 1));
        Assertions.assertTrue(senderEraShouldExist(claraRoutingTestPeer, "ALICE", "Hallo", 2));
        Assertions.assertTrue(senderEraShouldExist(claraRoutingTestPeer, "ALICE", uri, 3));
        Assertions.assertTrue(senderEraShouldExist(claraRoutingTestPeer, "ALICE", "HelloToClara", 4));

        // messages from Bob, which Alice had previously received, should have arrived at Clara
        // BUG: only the first message is routed
        Assertions.assertTrue(senderEraShouldExist(claraRoutingTestPeer, "BOB", uri, 0));
        Assertions.assertTrue(senderEraShouldExist(claraRoutingTestPeer, "BOB", "Elefant", 1));
        Assertions.assertTrue(senderEraShouldExist(claraRoutingTestPeer, "BOB", uri, 2));
        Assertions.assertTrue(senderEraShouldExist(claraRoutingTestPeer, "BOB", "Hallo", 3));
    }

    @Test
    public void multihopTests_WhitelistNoMatch() throws IOException, ASAPException, InterruptedException {
        aliceRoutingTestPeer.useWhitelistForRouting();
        bobRoutingTestPeer.useWhitelistForRouting();
        claraRoutingTestPeer.useWhitelistForRouting();
        String uri = "Pinguin";

        simpleEncounterWithMessageExchange("Tiger", uri);

        simpleEncounterWithMessageExchange(uri, "Elefant");

        simpleEncounterWithMessageExchange("Hallo", uri);

        simpleEncounterWithMessageExchange(uri, "Hallo");

        // expected: no era 0 and era 2 of Bob, no era 1 and 3 of Alice
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 0));
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", "Elefant", 1));
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", uri, 2));
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "BOB", "Hallo", 3));

        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", "Tiger", 0));
        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 1));
        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", "Hallo", 2));
        Assertions.assertFalse(senderEraShouldExist(bobRoutingTestPeer, "ALICE", uri, 3));

        Assertions.assertTrue(aliceRoutingTestPeer.isASAPRoutingAllowed(format));
        // exchange between alice and clara
        simpleEncounterWithMessageExchange(aliceRoutingTestPeer, claraRoutingTestPeer, "HelloToClara", "FromClara");
        // Alice should have received message from Clara
        Assertions.assertFalse(senderEraShouldExist(aliceRoutingTestPeer, "CLARA", "FromClara", 0));
        // messages from Alice should have arrived at Clara
        Assertions.assertFalse(senderEraShouldExist(claraRoutingTestPeer, "ALICE", "Tiger", 0));
        Assertions.assertFalse(senderEraShouldExist(claraRoutingTestPeer, "ALICE", uri, 1));
        Assertions.assertFalse(senderEraShouldExist(claraRoutingTestPeer, "ALICE", "Hallo", 2));
        Assertions.assertFalse(senderEraShouldExist(claraRoutingTestPeer, "ALICE", uri, 3));
        Assertions.assertFalse(senderEraShouldExist(claraRoutingTestPeer, "ALICE", "HelloToClara", 4));

        // messages from Bob, which Alice had previously received, should have arrived at Clara
        // BUG: only the first message is routed
        Assertions.assertFalse(senderEraShouldExist(claraRoutingTestPeer, "BOB", uri, 0));
        Assertions.assertFalse(senderEraShouldExist(claraRoutingTestPeer, "BOB", "Elefant", 1));
        Assertions.assertFalse(senderEraShouldExist(claraRoutingTestPeer, "BOB", uri, 2));
        Assertions.assertFalse(senderEraShouldExist(claraRoutingTestPeer, "BOB", "Hallo", 3));
    }

    // sends messages with given uri, starts and then stops the encounter
    public void simpleEncounterWithMessageExchange(String uriAlice, String uriBob)
            throws IOException, ASAPException, InterruptedException {

        simpleEncounterWithMessageExchange(aliceRoutingTestPeer, bobRoutingTestPeer, uriAlice, uriBob);
    }

    public void simpleEncounterWithMessageExchange(ASAPRoutingTestPeerFS peerA, ASAPRoutingTestPeerFS peerB, String uriPeerA, String uriPeerB)
            throws IOException, ASAPException, InterruptedException {
        // taken over from "UseThis4YourAppTests"
        // simulate ASAP first encounter with full ASAP protocol stack and engines
        System.out.println("+++++++++++++++++++ 1st encounter starts soon ++++++++++++++++++++");
        Thread.sleep(50);

        ///////////////// PEER A //////////////////////////////////////////////////////////////

        // setup message received listener - this should be replaced with your code - you implement a listener.
        ASAPMessageReceivedListenerExample asapMessageReceivedListenerExample = new ASAPMessageReceivedListenerExample();

        peerA.addASAPMessageReceivedListener(format, asapMessageReceivedListenerExample);


        // example - this should be produced by your application
        byte[] serializedData = ("from " + peerA.getPeerID()).getBytes();

        peerA.sendASAPMessage(format, uriPeerA, serializedData);

        ///////////////// PEER B //////////////////////////////////////////////////////////////

        // register your listener (or that mock) with asap connection mock
        peerB.addASAPMessageReceivedListener(format, asapMessageReceivedListenerExample);

        // bob writes something
        serializedData = ("from " + peerB.getPeerID()).getBytes();
        peerB.sendASAPMessage(format, uriPeerB, serializedData);
        peerB.sendASAPMessage(format, uriPeerB, serializedData);

        // give your app a moment to process
        Thread.sleep(500);
        // start actual encounter
        peerA.startEncounter(getPortNumber(), peerB);

        // give your app a moment to process
        Thread.sleep(1000);
        // stop encounter
        peerA.stopEncounter(peerB);
        // give your app a moment to process
        Thread.sleep(1000);
    }

    public boolean senderEraShouldExist(ASAPPeer peer, String sender, String uri, int era)
            throws IOException, ASAPException {
        return peer.getASAPStorage(format).getExistingIncomingStorage(sender).getChunkStorage().existsChunk(uri, era);
    }
}