package net.sharksystem.asap.protocol;

import net.sharksystem.asap.ASAPSecurityException;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface ASAPReadonlyKeyStorage {
    /**
     *
     * @return private key of local device - for signing
     * @throws ASAPSecurityException
     */
    PrivateKey getPrivateKey() throws ASAPSecurityException;

    // debugging
    PrivateKey getPrivateKey(CharSequence subjectID) throws ASAPSecurityException;

    /**
     *
     * @param subjectID
     * @return public key of recipient - to encrypt
     * @throws ASAPSecurityException if key cannot be found
     */
    PublicKey getPublicKey(CharSequence subjectID) throws ASAPSecurityException;

    /**
     * @return public key of local device - for signing
     * @throws ASAPSecurityException
     */
    PublicKey getPublicKey() throws ASAPSecurityException;

    String getRSAEncryptionAlgorithm();

    String getRSASigningAlgorithm();
}
