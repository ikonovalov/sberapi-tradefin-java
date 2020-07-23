package ru.codeunited.sberapi;

import org.apache.http.impl.client.CloseableHttpClient;

import java.util.UUID;

public abstract class PrimitiveClient {

    protected final CloseableHttpClient httpClient;

    protected final CredentialsSource credentials;

    protected PrimitiveClient(CloseableHttpClient httpClient, CredentialsSource credentials) {
        this.httpClient = httpClient;
        this.credentials = credentials;
    }

    public final String getNewUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
