package ru.codeunited.sberapi;

import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class TlsFactorySystemPropsSource implements TlsFactory {

    private final String keystoreLocation = System.getProperty("keystore");
    private final String keystorePwd = System.getProperty("keystorePwd");
    private final String keyPwd = System.getProperty("keyPwd");

    private final String truststoreLocation = System.getProperty("truststore");
    private final String truststorePwd = System.getProperty("truststorePwd");

    private final File keyStore;
    private final  File trustStore;

    public TlsFactorySystemPropsSource() {
        this.keyStore = new File(keystoreLocation);
        this.trustStore = new File(truststoreLocation);
    }

    @Override
    public SSLContext build() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableKeyException, KeyManagementException {
        return SSLContexts.custom()
                .loadTrustMaterial(trustStore, truststorePwd.toCharArray(), new TrustAllStrategy())
                .loadKeyMaterial(keyStore, keystorePwd.toCharArray(), keyPwd.toCharArray())
                .build();
    }
}
