package net.sharksystem.asap;

import net.sharksystem.asap.RDFComparator.LiteralStringComparator;
import net.sharksystem.asap.RDFComparator.RDFComparator;
import net.sharksystem.asap.RDFModel.JenaRDFModel;
import net.sharksystem.asap.RDFModel.RDFModel;
import net.sharksystem.asap.apps.testsupport.ASAPRoutingTestPeerFS;
import net.sharksystem.asap.apps.testsupport.ASAPTestPeerFS;
import net.sharksystem.asap.mockAndTemplates.ASAPMessageReceivedListenerExample;
import net.sharksystem.asap.mockAndTemplates.TestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.jena.Jena;
import org.apache.jena.rdf.model.Literal;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static net.sharksystem.asap.mockAndTemplates.TestUtils.*;

public class RoutingASAPPeerFSTest {

    private ASAPRoutingTestPeerFS aliceRoutingTestPeer, bobRoutingTestPeer;
    private RDFModel rdfModel;
    private RDFComparator rdfComparator;

    private String APP_NAME = "ASAPRouterTest";

    private String TEST_URI = "sn://test";

    @Before
    public void setUp() throws IOException, ASAPException {
        // delete directory „testPeerFS” to prevent errors when running twice
        FileUtils.deleteDirectory(new File("testPeerFS"));
        Collection<CharSequence> formats = new ArrayList<>();
        formats.add("ASAPRouter");

        this.rdfModel = new JenaRDFModel();

        this.rdfComparator = new LiteralStringComparator(rdfModel);

        this.aliceRoutingTestPeer = new ASAPRoutingTestPeerFS("ALICE", "./testPeerFS/ALICE", formats, rdfComparator);
        this.bobRoutingTestPeer = new ASAPRoutingTestPeerFS("BOB", "./testPeerFS/BOB", formats, rdfComparator);

        aliceRoutingTestPeer.useBlacklistForRouting();
        bobRoutingTestPeer.useBlacklistForRouting();
    }

    @Test
    public void chunkReceived() {
    }

    @Test
    public void compareWithRDFModel() {
    }

    @Test
    public void compareAttributeWithUri() {
    }

    private static final int PORT = 7777;

    private static int port = 0;
    static int getPortNumber() {
        if(RoutingASAPPeerFSTest.port == 0) {
            RoutingASAPPeerFSTest.port = PORT;
        } else {
            RoutingASAPPeerFSTest.port++;
        }

        return RoutingASAPPeerFSTest.port;
    }

    @Test
    public void asapTestExample() throws IOException, ASAPException, InterruptedException {
        ///////////////// ALICE //////////////////////////////////////////////////////////////
        // setup mocked peer / asap application and activity in android

        // 1st encounter
        this.scenarioPart1(aliceRoutingTestPeer, bobRoutingTestPeer);

        aliceRoutingTestPeer.startEncounter(getPortNumber(), bobRoutingTestPeer);
        // give your app a moment to process
        Thread.sleep(1000);
        // stop encounter
        bobRoutingTestPeer.stopEncounter(aliceRoutingTestPeer);
        // give your app a moment to process
        Thread.sleep(1000);

        // 2nd encounter
        this.scenarioPart2(aliceRoutingTestPeer, bobRoutingTestPeer);

        aliceRoutingTestPeer.startEncounter(getPortNumber(), bobRoutingTestPeer);
    }

    public void scenarioPart1(ASAPPeer alicePeer, ASAPPeer bobPeer)
            throws IOException, ASAPException, InterruptedException {
        // simulate ASAP first encounter with full ASAP protocol stack and engines
        System.out.println("+++++++++++++++++++ 1st encounter starts soon ++++++++++++++++++++");
        Thread.sleep(50);

        // setup message received listener - this should be replaced with your code - you implement a listener.
        ASAPMessageReceivedListenerExample aliceMessageReceivedListenerExample =
                new ASAPMessageReceivedListenerExample();

        alicePeer.addASAPMessageReceivedListener(APP_NAME, aliceMessageReceivedListenerExample);

        // example - this should be produced by your application
        byte[] serializedData = TestUtils.serializeExample(42, "from alice", true);

        alicePeer.sendASAPMessage(APP_NAME, TEST_URI, serializedData);

        ///////////////// BOB //////////////////////////////////////////////////////////////

        // this should be replaced with your code - you must implement a listener.
        ASAPMessageReceivedListenerExample asapMessageReceivedListenerExample =
                new ASAPMessageReceivedListenerExample();

        // register your listener (or that mock) with asap connection mock
        bobPeer.addASAPMessageReceivedListener(APP_NAME, asapMessageReceivedListenerExample);

        // bob writes something
        bobPeer.sendASAPMessage(APP_NAME, TEST_URI,
                TestUtils.serializeExample(43, "from bob", false));
        bobPeer.sendASAPMessage(APP_NAME, TEST_URI,
                TestUtils.serializeExample(44, "from bob again", false));


        // give your app a moment to process
        Thread.sleep(500);
    }

    public void scenarioPart2(ASAPPeer alicePeer, ASAPPeer bobPeer)
            throws IOException, ASAPException, InterruptedException {

        // bob writes something
        bobPeer.sendASAPMessage(APP_NAME, TEST_URI,
                TestUtils.serializeExample(43, "third message from bob", false));

        // simulate second encounter
        System.out.println("+++++++++++++++++++ 2nd encounter starts soon ++++++++++++++++++++");
        Thread.sleep(50);
    }
}