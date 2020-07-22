package ru.codeunited.sberapi;

import org.apache.http.impl.client.CloseableHttpClient;

public interface ApacheHttpClientFactory {

    CloseableHttpClient build() throws Exception;
}
