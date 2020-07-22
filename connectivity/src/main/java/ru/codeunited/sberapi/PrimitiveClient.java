package ru.codeunited.sberapi;

import org.apache.http.impl.client.CloseableHttpClient;

public abstract class PrimitiveClient {
    protected final CloseableHttpClient httpClient;

    protected final CredentialsSource credentials;

    protected PrimitiveClient(CloseableHttpClient httpClient, CredentialsSource credentials) {
        this.httpClient = httpClient;
        this.credentials = credentials;
    }
}
