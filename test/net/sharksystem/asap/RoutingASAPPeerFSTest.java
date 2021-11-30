package net.sharksystem.asap;

import net.sharksystem.asap.rdfcomparator.LiteralStringComparator;
import net.sharksystem.asap.rdfcomparator.RDFComparator;
import net.sharksystem.asap.rdfmodel.JenaRDFModel;
import net.sharksystem.asap.rdfmodel.RDFModel;
import net.sharksystem.asap.apps.testsupport.ASAPRoutingTestPeerFS;
import net.sharksystem.asap.helper.RoutingASAPPeerFSTestHelper;
import net.sharksystem.asap.mockAndTemplates.ASAPMessageReceivedListenerExample;
import net.sharksystem.asap.mockAndTemplates.TestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class RoutingASAPPeerFSTest {

    RoutingASAPPeerFSTestHelper testHelper;

    private ASAPRoutingTestPeerFS aliceRoutingTestPeer, bobRoutingTestPeer;
    private RDFModel rdfModel;
    private RDFComparator rdfComparator;

    private String TEST_URI = "sn://test";

    private Collection<CharSequence> formats;

    private CharSequence format = "ASAPRouter";

    private boolean firstRun;

    private static final int PORT = 7777;

    private static int port = 0;

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

        // helper class with usefull testing functions
        testHelper = new RoutingASAPPeerFSTestHelper(rootfolder, format);

        // default model found under: src/main/resources/rdfModel.rdf
        this.rdfModel = new JenaRDFModel();

        // default comparator: literal string matcher
        this.rdfComparator = new LiteralStringComparator(rdfModel);

        // ASAPRoutingTestPeerFS is the same as ASAPTestPeerFS with a RoutingASAPPeerFS instead of ASAPPeerFS
        this.aliceRoutingTestPeer = new ASAPRoutingTestPeerFS("ALICE", rootfolder + "/ALICE", formats, rdfComparator);
        this.bobRoutingTestPeer = new ASAPRoutingTestPeerFS("BOB", rootfolder + "/BOB", formats, rdfComparator);

        aliceRoutingTestPeer.useBlacklistForRouting();
        bobRoutingTestPeer.useBlacklistForRouting();

        firstRun = true;
    }

    @Test
    public void chunkRecievedTest_blacklistWithBlocking(){
        aliceRoutingTestPeer.useBlacklistForRouting();

        //todo create unit tests by mocking the comparator

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
        Assertions.assertTrue(testHelper.senderEraShouldExist("ALICE", "BOB", uriBob, 0));
        Assertions.assertTrue(testHelper.senderEraShouldExist("BOB", "ALICE", uriAlice, 0));
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
        Assertions.assertTrue(testHelper.senderEraShouldExist("ALICE", "BOB", uriBob, 0));
        Assertions.assertFalse(testHelper.senderEraShouldExist("BOB", "ALICE", uriAlice, 0));
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
        Assertions.assertFalse(testHelper.senderEraShouldExist("ALICE", "BOB", uriBob, 0));
        Assertions.assertFalse(testHelper.senderEraShouldExist("BOB", "ALICE", uriAlice, 0));
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
        Assertions.assertFalse(testHelper.senderEraShouldExist("ALICE", "BOB", uri, 0));
        Assertions.assertFalse(testHelper.senderEraShouldExist("ALICE", "BOB", uri, 1));
        Assertions.assertFalse(testHelper.senderEraShouldExist("ALICE", "BOB", uri, 2));
        Assertions.assertFalse(testHelper.senderEraShouldExist("ALICE", "BOB", uri, 3));

        Assertions.assertFalse(testHelper.senderEraShouldExist("BOB", "ALICE", uri, 0));
        Assertions.assertFalse(testHelper.senderEraShouldExist("BOB", "ALICE", uri, 1));
        Assertions.assertFalse(testHelper.senderEraShouldExist("BOB", "ALICE", uri, 2));
        Assertions.assertFalse(testHelper.senderEraShouldExist("BOB", "ALICE", uri, 3));
    }

    @Test
    public void multiEncounter_onlyNonBlacklistEntriesSent() throws InterruptedException, IOException, ASAPException {

        String uri = "Pinguin";

        simpleEncounterWithMessageExchange(uri, uri);

        simpleEncounterWithMessageExchange(uri, uri);

        simpleEncounterWithMessageExchange(uri, uri);

        simpleEncounterWithMessageExchange(uri, uri);

        // each message should have created a new era, so there should be a meta and content file in each subfolder
        Assertions.assertTrue(testHelper.senderEraShouldExist("ALICE", "BOB", uri, 0));
        Assertions.assertTrue(testHelper.senderEraShouldExist("ALICE", "BOB", uri, 1));
        Assertions.assertTrue(testHelper.senderEraShouldExist("ALICE", "BOB", uri, 2));
        Assertions.assertTrue(testHelper.senderEraShouldExist("ALICE", "BOB", uri, 3));

        Assertions.assertTrue(testHelper.senderEraShouldExist("BOB", "ALICE", uri, 0));
        Assertions.assertTrue(testHelper.senderEraShouldExist("BOB", "ALICE", uri, 1));
        Assertions.assertTrue(testHelper.senderEraShouldExist("BOB", "ALICE", uri, 2));
        Assertions.assertTrue(testHelper.senderEraShouldExist("BOB", "ALICE", uri, 3));
    }

    @Test
    public void bugShowcase_blacklist_thisDoesNotWorkAnymore() throws InterruptedException, IOException, ASAPException {

        // this is blacklisted, should be deleted out of incoming storage
        String uri = "Eis";

        simpleEncounterWithMessageExchange("Tiger", uri);

        simpleEncounterWithMessageExchange(uri, "Elefant");

        simpleEncounterWithMessageExchange("Hallo", uri);

        simpleEncounterWithMessageExchange(uri, "Hallo");

        // expected: no era 0 and era 2 of Bob, no era 1 and 3 of Alice
        Assertions.assertFalse(testHelper.senderEraShouldExist("ALICE", "BOB", uri, 0));
        Assertions.assertTrue(testHelper.senderEraShouldExist("ALICE", "BOB", "Elefant", 1));
        Assertions.assertFalse(testHelper.senderEraShouldExist("ALICE", "BOB", uri, 2));
        Assertions.assertTrue(testHelper.senderEraShouldExist("ALICE", "BOB", "Hallo", 3));

        Assertions.assertTrue(testHelper.senderEraShouldExist("BOB", "ALICE", "Tiger", 0));
        Assertions.assertFalse(testHelper.senderEraShouldExist("BOB", "ALICE", uri, 1));
        Assertions.assertTrue(testHelper.senderEraShouldExist("BOB", "ALICE", "Hallo", 2));
        Assertions.assertFalse(testHelper.senderEraShouldExist("BOB", "ALICE", uri, 3));
    }

    @Test
    public void bugShowcase_NonBlacklist_thisDoesNotWorkAnymore() throws InterruptedException, IOException, ASAPException {

        // not on blacklist, so all entries should persist (should be the same as ASAPPeerFS)
        String uri = "Pinguin";

        simpleEncounterWithMessageExchange("Tiger", uri);

        simpleEncounterWithMessageExchange(uri, "Elefant");

        simpleEncounterWithMessageExchange("Hallo", uri);

        simpleEncounterWithMessageExchange(uri, "Hallo");

        // expected: ALL eras should exist on both sides
        // actual: alice missing „Elefant”, bob missing the last „Pinguin”...
        Assertions.assertTrue(testHelper.senderEraShouldExist("ALICE", "BOB", uri, 0));
        Assertions.assertTrue(testHelper.senderEraShouldExist("ALICE", "BOB", "Elefant", 1));
        Assertions.assertTrue(testHelper.senderEraShouldExist("ALICE", "BOB", uri, 2));
        Assertions.assertTrue(testHelper.senderEraShouldExist("ALICE", "BOB", "Hallo", 3));

        Assertions.assertTrue(testHelper.senderEraShouldExist("BOB", "ALICE", "Tiger", 0));
        Assertions.assertTrue(testHelper.senderEraShouldExist("BOB", "ALICE", uri, 1));
        Assertions.assertTrue(testHelper.senderEraShouldExist("BOB", "ALICE", "Hallo", 2));
        Assertions.assertTrue(testHelper.senderEraShouldExist("BOB", "ALICE", uri, 3));
    }

    @Test
    public void multiEncounter_AndTopicsNotFullyTransmitted() throws InterruptedException, IOException, ASAPException {

        String uriAlice = "topic1";
        String uriBob = "topic1";

        simpleEncounterWithMessageExchange(uriAlice, uriBob);

        uriAlice = "topic2";
        uriBob = "topic1";
        simpleEncounterWithMessageExchange(uriAlice, uriBob);

        uriAlice = "topic2";
        uriBob = "topic2";
        simpleEncounterWithMessageExchange(uriAlice, uriBob);

        uriAlice = "topic3";
        uriBob = "topic1";
        simpleEncounterWithMessageExchange(uriAlice, uriBob);

        // check results in main folder: testPeerFS -> ALICE/BOB
        // Bob doesn't have all topics from Alice, Alice doesn't have all topics from Bob
    }

    @Test
    public void multiEncounter_differentUrisDoNotGetTransmitGoodAtAll() throws InterruptedException, IOException, ASAPException {

        String uriAlice = "aliceUri1";
        String uriBob = "bobUri1";

        simpleEncounterWithMessageExchange(uriAlice, uriBob);

        uriAlice = "aliceUri2";
        uriBob = "bobUri2";
        simpleEncounterWithMessageExchange(uriAlice, uriBob);

        uriAlice = "aliceUri3";
        uriBob = "bobUri3";
        simpleEncounterWithMessageExchange(uriAlice, uriBob);

        uriAlice = "aliceUri4";
        uriBob = "bobUri4";
        simpleEncounterWithMessageExchange(uriAlice, uriBob);

        // check results in main folder: testPeerFS -> ALICE/BOB
        // Bob doesn't have all topics from Alice, Alice doesn't have all topics from Bob
    }

    @Test
    public void multiEncounter_sameUrisGetTransmitted() throws InterruptedException, IOException, ASAPException {

        String uriAlice = "topic1";
        String uriBob = "topic1";

        simpleEncounterWithMessageExchange(uriAlice, uriBob);

        uriAlice = "topic1";
        uriBob = "topic1";
        simpleEncounterWithMessageExchange(uriAlice, uriBob);

        uriAlice = "topic1";
        uriBob = "topic1";
        simpleEncounterWithMessageExchange(uriAlice, uriBob);

        uriAlice = "topic1";
        uriBob = "topic1";
        simpleEncounterWithMessageExchange(uriAlice, uriBob);

    }

    @Test
    public void bugShowcase_blacklist_thisDoesNotWorkAnymore2() throws InterruptedException, IOException, ASAPException {

        // this is blacklisted, should be deleted out of incoming storage
        String uri = "Eis";

        simpleEncounterWithMessageExchange("Tiger", uri);

        // this seems to break the engine
        simpleEncounterWithMessageExchange("THISBREAKSEVERYTHING", "Elefant");

        simpleEncounterWithMessageExchange("Hallo", uri);

        simpleEncounterWithMessageExchange(uri, "Hallo");

        // expected: no era 0 and era 2 of Bob, no era 3 of Alice
        // actual: alice has no era 1 of bob („Elefant” went missing! ??? )
    }

    // sends messages with given uri, starts and then stops the encounter
    public void simpleEncounterWithMessageExchange(String uriAlice, String uriBob)
            throws IOException, ASAPException, InterruptedException {

        // simulate ASAP first encounter with full ASAP protocol stack and engines
        System.out.println("+++++++++++++++++++ 1st encounter starts soon ++++++++++++++++++++");
        Thread.sleep(50);

        // setup message received listener - this should be replaced with your code - you implement a listener.
        ASAPMessageReceivedListenerExample aliceMessageReceivedListenerExample =
                new ASAPMessageReceivedListenerExample();

        aliceRoutingTestPeer.addASAPMessageReceivedListener(format, aliceMessageReceivedListenerExample);

        // example - this should be produced by your application
        byte[] serializedData = TestUtils.serializeExample(42, "from alice", true);

        aliceRoutingTestPeer.sendASAPMessage(format, uriAlice, serializedData);

        ///////////////// BOB //////////////////////////////////////////////////////////////

        // this should be replaced with your code - you must implement a listener.
        ASAPMessageReceivedListenerExample asapMessageReceivedListenerExample =
                new ASAPMessageReceivedListenerExample();

        // register your listener (or that mock) with asap connection mock
        bobRoutingTestPeer.addASAPMessageReceivedListener(format, asapMessageReceivedListenerExample);

        // bob writes something
        bobRoutingTestPeer.sendASAPMessage(format, uriBob,
                TestUtils.serializeExample(43, "from bob", false));
        bobRoutingTestPeer.sendASAPMessage(format, uriBob,
                TestUtils.serializeExample(44, "from bob again", false));

        // give your app a moment to process
        Thread.sleep(500);
        // start actual encounter
        aliceRoutingTestPeer.startEncounter(getPortNumber(), bobRoutingTestPeer);

        // give your app a moment to process
        Thread.sleep(1000);
        // stop encounter
        bobRoutingTestPeer.stopEncounter(aliceRoutingTestPeer);
        // give your app a moment to process
        Thread.sleep(1000);
    }
}