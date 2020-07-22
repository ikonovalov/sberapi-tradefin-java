package ru.codeunited.sberapi;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public interface TlsFactory {

    SSLContext build() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableKeyException, KeyManagementException;

    default String[] supportedAglorithms() {
        return new String[] {"TLSv1.2"};
    }

    default String[] supportedCipherSuites() {
        return null;
    }
}
