package ru.codeunited.sberapi;

import com.google.gson.Gson;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.UUID;

import static java.util.Arrays.asList;

public class TokenClient extends PrimitiveClient {

    public static void main(String[] args) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, UnrecoverableKeyException {
        ApacheHttpClientCustomSSLFactory httpClientFactory = new ApacheHttpClientCustomSSLFactory(new TlsFactorySystemPropsSource());
        CloseableHttpClient httpClient = httpClientFactory.build();
        CredentialsSource credentials = new CredentialsSystemPropsSource();

        try (CloseableHttpResponse response = new TokenClient(httpClient, credentials).callToken()) {
            System.out.println(response.getStatusLine());
            for (Header h : response.getAllHeaders()) {
                System.out.println(h.getName() + ": " + h.getValue());
            }
            HttpEntity entity = response.getEntity();
            System.out.println("----------------------------------------");
            String rs = EntityUtils.toString(entity);
            System.out.println(rs);
        }
    }

    public TokenClient(CloseableHttpClient httpClient, CredentialsSource credentialsSource) {
        super(httpClient, credentialsSource);
    }

    public CloseableHttpResponse callToken() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        HttpUriRequest request = RequestBuilder
                .post("https://api.sberbank.ru/ru/prod/tokens/v2/oauth")
                .addHeader("Authorization", "Basic " + credentials.getBasicAuthenticationString())
                .addHeader("x-ibm-client-id", credentials.getClientId())
                .addHeader("rquid", UUID.randomUUID().toString().replaceAll("-", ""))
                .setEntity(new UrlEncodedFormEntity(
                        asList(
                                new BasicNameValuePair("grant_type", "client_credentials"),
                                new BasicNameValuePair("scope", "https://api.sberbank.ru/escrow")
                        ))
                ).build();

        return httpClient.execute(request);
    }

    public String getTokenID() throws Exception {
        try (CloseableHttpResponse response = callToken()) {
            HttpEntity entity = response.getEntity();
            String rsBody = EntityUtils.toString(entity);
            if (response.getStatusLine().getStatusCode() == 200) {
                HashMap<String, ?> mp = new Gson().fromJson(rsBody, HashMap.class);
                return (String) mp.get("access_token");
            } else {
                throw new RuntimeException(response.getStatusLine().toString() + "\n" + rsBody);
            }
        }
    }
}
