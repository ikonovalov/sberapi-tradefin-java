package ru.codeunited.sbapi.escrow;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import ru.codeunited.sberapi.*;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class EscrowClient {
    public static void main(String[] args) throws Exception {
        ApacheHttpClientCustomSSLFactory httpClientFactory = new ApacheHttpClientCustomSSLFactory(new TlsFactorySystemPropsSource());
        CloseableHttpClient httpClient = httpClientFactory.build();
        CredentialsSource credentials = new CredentialsSystemPropsSource();

        TokenClient tokenClient = new TokenClient(httpClient, credentials);
        String tokenId = tokenClient.getTokenID();

        HttpUriRequest request = RequestBuilder
                .get("https://api.sberbank.ru/ru/prod/v2/escrow/residential-complex")
                .addHeader("Authorization", "Bearer " + tokenId)
                .addHeader("x-ibm-client-id", credentials.getClientId())
                .addHeader("x-introspect-rquid", UUID.randomUUID().toString().replaceAll("-", ""))
                .build();
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            String rsBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            System.out.println(rsBody);
        }

    }
}
