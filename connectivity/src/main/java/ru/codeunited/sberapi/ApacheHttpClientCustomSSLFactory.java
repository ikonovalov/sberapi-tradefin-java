package ru.codeunited.sberapi;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class ApacheHttpClientCustomSSLFactory implements ApacheHttpClientFactory {

    private final TlsFactory tlsFactory;
    private final HostnameVerifier hostnameVerifier;

    public ApacheHttpClientCustomSSLFactory(TlsFactory tlsFactory, HostnameVerifier hostnameVerifier) {
        this.tlsFactory = tlsFactory;
        this.hostnameVerifier = hostnameVerifier;
    }

    public ApacheHttpClientCustomSSLFactory(TlsFactory tlsFactory) {
        this(tlsFactory, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
    }

    public CloseableHttpClient build() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        SSLContext sslContext = tlsFactory.build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslContext,
                this.tlsFactory.supportedAglorithms(),
                this.tlsFactory.supportedCipherSuites(),
                this.hostnameVerifier);

        CloseableHttpClient httpclient = HttpClients
                .custom()
                .setSSLSocketFactory(sslsf)
                .build();
        return httpclient;
    }
}
