package net.sharksystem.asap;

import net.sharksystem.asap.RDFComparator.LiteralStringComparator;
import net.sharksystem.asap.RDFComparator.RDFComparator;
import net.sharksystem.asap.RDFModel.JenaRDFModel;
import net.sharksystem.asap.RDFModel.RDFModel;
import net.sharksystem.asap.apps.testsupport.ASAPRoutingTestPeerFS;
import net.sharksystem.asap.apps.testsupport.ASAPTestPeerFS;
import net.sharksystem.asap.cmdline.TCPStream;
import net.sharksystem.asap.mockAndTemplates.ASAPMessageReceivedListenerExample;
import net.sharksystem.asap.mockAndTemplates.TestUtils;
import net.sharksystem.asap.utils.ASAPPeerHandleConnectionThread;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import static net.sharksystem.asap.mockAndTemplates.TestUtils.*;

public class RoutingASAPPeerFSTest {

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
        formats = new ArrayList<>();
        formats.add(format);

        this.rdfModel = new JenaRDFModel();

        this.rdfComparator = new LiteralStringComparator(rdfModel);

        this.aliceRoutingTestPeer = new ASAPRoutingTestPeerFS("ALICE", "./testPeerFS/ALICE", formats, rdfComparator);
        this.bobRoutingTestPeer = new ASAPRoutingTestPeerFS("BOB", "./testPeerFS/BOB", formats, rdfComparator);

        aliceRoutingTestPeer.useBlacklistForRouting();
        bobRoutingTestPeer.useBlacklistForRouting();

        firstRun = true;
    }

    // original test case seems to work (with same uri)
    @Test
    public void asapTestExample() throws IOException, ASAPException, InterruptedException {
        ///////////////// ALICE //////////////////////////////////////////////////////////////
        // 1st encounter
        this.scenarioPart1(aliceRoutingTestPeer, bobRoutingTestPeer);


        // 2nd encounter
        this.scenarioPart1(aliceRoutingTestPeer, bobRoutingTestPeer);


        // 3rd encounter
        this.scenarioPart1(aliceRoutingTestPeer, bobRoutingTestPeer);


        aliceRoutingTestPeer.startEncounter(getPortNumber(), bobRoutingTestPeer);
    }

    @Test
    public void chunkReceivedAndNoBlacklistEntry() throws InterruptedException, IOException, ASAPException {
        String uriAlice = "Tier";
        String uriBob = "Wasser";

        scenario1_sendMessageWithUris(uriAlice, uriBob);

        Assert.assertTrue(aliceRoutingTestPeer.getASAPStorage(format).getChunkStorage().existsChunk(uriAlice, 0));
        Assert.assertTrue(bobRoutingTestPeer.getASAPStorage(format).getChunkStorage().existsChunk(uriBob, 0));
        Assert.assertTrue(aliceRoutingTestPeer.getASAPStorage(format).getChunkStorage().existsChunk(uriBob, 1));
        Assert.assertTrue(bobRoutingTestPeer.getASAPStorage(format).getChunkStorage().existsChunk(uriAlice, 1));
    }

    @Test
    public void encounter_AndTopicsNotFullyTransmitted() throws InterruptedException, IOException, ASAPException {

        String uriAlice = "topic1";
        String uriBob = "topic1";

        scenario1_sendMessageWithUris(uriAlice, uriBob);

        uriAlice = "topic2";
        uriBob = "topic1";
        scenario1_sendMessageWithUris(uriAlice, uriBob);

        uriAlice = "topic2";
        uriBob = "topic2";
        scenario1_sendMessageWithUris(uriAlice, uriBob);

        uriAlice = "topic3";
        uriBob = "topic1";
        scenario1_sendMessageWithUris(uriAlice, uriBob);

        // check results in main folder: testPeerFS -> ALICE/BOB
        // Bob doesn't have all topics from Alice, Alice doesn't have all topics from Bob
    }

    @Test
    public void encounter_sameUrisGetTransmitted() throws InterruptedException, IOException, ASAPException {

        String uriAlice = "topic1";
        String uriBob = "topic1";

        scenario1_sendMessageWithUris(uriAlice, uriBob);

        uriAlice = "topic1";
        uriBob = "topic1";
        scenario1_sendMessageWithUris(uriAlice, uriBob);

        uriAlice = "topic1";
        uriBob = "topic1";
        scenario1_sendMessageWithUris(uriAlice, uriBob);

        uriAlice = "topic1";
        uriBob = "topic1";
        scenario1_sendMessageWithUris(uriAlice, uriBob);

        // check results in main folder: testPeerFS -> ALICE/BOB
        // Bob doesn't have all topics from Alice, Alice doesn't have all topics from Bob
    }

    // sends messages with given uri, starts and stops encounter
    public void scenario1_sendMessageWithUris(String uriAlice, String uriBob)
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

    public void scenario1_sendMessageWithUris_tcpStream(ASAPPeer alicePeer, ASAPPeer bobPeer, String uriAlice, String uriBob)
            throws IOException, ASAPException, InterruptedException {

        // simulate ASAP first encounter with full ASAP protocol stack and engines
        System.out.println("+++++++++++++++++++ 1st encounter starts soon ++++++++++++++++++++");
        Thread.sleep(50);

        // example - this should be produced by your application
        byte[] serializedData = TestUtils.serializeExample(42, uriAlice + "from alice", true);

        alicePeer.sendASAPMessage(format, uriAlice, serializedData);

        ///////////////// BOB //////////////////////////////////////////////////////////////

        // bob writes something
        bobPeer.sendASAPMessage(format, uriBob,
                TestUtils.serializeExample(43, uriBob + "from bob", false));

        // give your app a moment to process
        Thread.sleep(500);
        // create connections for both sides
        TCPStream aliceChannel = new TCPStream(getPortNumber(), true, "a2b");
        TCPStream bobChannel = new TCPStream(port, false, "b2a");

        aliceChannel.start();
        bobChannel.start();

        // wait to connect
        aliceChannel.waitForConnection();
        bobChannel.waitForConnection();

        ASAPPeerHandleConnectionThread aliceEngineThread = new ASAPPeerHandleConnectionThread(aliceRoutingTestPeer,
                aliceChannel.getInputStream(), aliceChannel.getOutputStream());

        aliceEngineThread.start();

        // for better debugging - no new thread
        bobRoutingTestPeer.handleConnection(bobChannel.getInputStream(), bobChannel.getOutputStream());

        // wait until communication probably ends
        System.out.flush();
        System.err.flush();
        Thread.sleep(10000);
        System.out.flush();
        System.err.flush();

        // close connections: note ASAPEngine does NOT close any connection!!
        aliceChannel.close();
        bobChannel.close();
        System.out.flush();
        System.err.flush();
        Thread.sleep(1000);
        System.out.flush();
        System.err.flush();

        // give your app a moment to process
        Thread.sleep(1000);
    }

    public void scenarioPart1(ASAPPeer alicePeer, ASAPPeer bobPeer)
            throws IOException, ASAPException, InterruptedException {
        // simulate ASAP first encounter with full ASAP protocol stack and engines
        System.out.println("+++++++++++++++++++ 1st encounter starts soon ++++++++++++++++++++");
        Thread.sleep(50);

        // setup message received listener - this should be replaced with your code - you implement a listener.
        ASAPMessageReceivedListenerExample aliceMessageReceivedListenerExample =
                new ASAPMessageReceivedListenerExample();

        alicePeer.addASAPMessageReceivedListener(format, aliceMessageReceivedListenerExample);

        // example - this should be produced by your application
        byte[] serializedData = TestUtils.serializeExample(42, "from alice", true);

        alicePeer.sendASAPMessage(format, TEST_URI, serializedData);

        ///////////////// BOB //////////////////////////////////////////////////////////////

        // this should be replaced with your code - you must implement a listener.
        ASAPMessageReceivedListenerExample asapMessageReceivedListenerExample =
                new ASAPMessageReceivedListenerExample();

        // register your listener (or that mock) with asap connection mock
        bobPeer.addASAPMessageReceivedListener(format, asapMessageReceivedListenerExample);

        // bob writes something
        bobPeer.sendASAPMessage(format, TEST_URI,
                TestUtils.serializeExample(43, "from bob", false));
        bobPeer.sendASAPMessage(format, TEST_URI,
                TestUtils.serializeExample(44, "from bob again", false));


        // give your app a moment to process
        Thread.sleep(500);

        aliceRoutingTestPeer.startEncounter(getPortNumber(), bobRoutingTestPeer);
        // give your app a moment to process
        Thread.sleep(1000);
        // stop encounter
        bobRoutingTestPeer.stopEncounter(aliceRoutingTestPeer);
        // give your app a moment to process
        Thread.sleep(1000);
    }

    public void scenarioPart2(ASAPPeer alicePeer, ASAPPeer bobPeer)
            throws IOException, ASAPException, InterruptedException {

        // bob writes something
        bobPeer.sendASAPMessage(format, TEST_URI,
                TestUtils.serializeExample(43, "third message from bob", false));

        // simulate second encounter
        System.out.println("+++++++++++++++++++ 2nd encounter starts soon ++++++++++++++++++++");
        Thread.sleep(50);
    }
}