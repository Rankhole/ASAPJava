package net.sharksystem.asap.apps.testsupport;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.EncounterConnectionType;
import net.sharksystem.asap.RDFComparator.LiteralStringComparator;
import net.sharksystem.asap.RDFComparator.RDFComparator;
import net.sharksystem.asap.RDFModel.JenaRDFModel;
import net.sharksystem.asap.RoutingASAPPeerFS;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;

/**
 * Wrapper class for simulating encounter using real TCP/IP
 */
public class ASAPRoutingTestPeerFS extends RoutingASAPPeerFS {
    private ServerSocket serverSocket = null;
    private Socket socket = null;

    public ASAPRoutingTestPeerFS(CharSequence peerName, Collection<CharSequence> supportedFormats, RDFComparator rdfComparator) throws IOException, ASAPException {
        this(peerName, "./testPeerFS/" + peerName, supportedFormats, rdfComparator);
    }

    public ASAPRoutingTestPeerFS(CharSequence peerName, CharSequence rootFolder, Collection<CharSequence> supportedFormats)
            throws IOException, ASAPException {
        super(peerName, rootFolder, supportedFormats, new LiteralStringComparator(new JenaRDFModel()));
    }

    public ASAPRoutingTestPeerFS(CharSequence peerName, CharSequence rootFolder, Collection<CharSequence> supportedFormats, RDFComparator rdfComparator)
            throws IOException, ASAPException {
        super(peerName, rootFolder, supportedFormats, rdfComparator);
    }

    public void startEncounter(int port, ASAPRoutingTestPeerFS otherPeer) throws IOException {
        this.serverSocket = new ServerSocket(port);

        new Thread(() -> {
            try {
                ASAPRoutingTestPeerFS.this.socket = ASAPRoutingTestPeerFS.this.serverSocket.accept();
            } catch (IOException e) {
                ASAPRoutingTestPeerFS.this.log("fatal while waiting for client to connect: "
                        + e.getLocalizedMessage());
            }

            ASAPRoutingTestPeerFS.this.startSession();
        }).start();

        // wait a moment
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }

        otherPeer.connect(port);
    }

    private void connect(int port) throws IOException {
        this.socket = new Socket("localhost", port);
        this.startSession();
    }

    public void stopEncounter(ASAPRoutingTestPeerFS otherPeer) throws IOException {
        this.socket.close();
    }

    private void startSession() {
        new Thread(() -> {
            try {
                ASAPRoutingTestPeerFS.this.handleConnection(
                        ASAPRoutingTestPeerFS.this.socket.getInputStream(),
                        ASAPRoutingTestPeerFS.this.socket.getOutputStream(),
                        EncounterConnectionType.INTERNET);
            } catch (IOException | ASAPException e) {
                ASAPRoutingTestPeerFS.this.log("fatal while connecting: " + e.getLocalizedMessage());
            }
        }).start();
    }
}
