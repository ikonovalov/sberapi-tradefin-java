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
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import static java.util.Arrays.asList;

public class TokenClient extends PrimitiveClient {

    private ConcurrentMap<String, String> tokens = new ConcurrentHashMap<>();

    public static void main(String[] args) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        ApacheHttpClientCustomSSLFactory httpClientFactory = new ApacheHttpClientCustomSSLFactory(new TlsFactorySystemPropsSource());
        CloseableHttpClient httpClient = httpClientFactory.build();
        CredentialsSource credentials = new CredentialsSystemPropsSource();

        // Получение токена для эскроу
        try (CloseableHttpResponse response =
                     new TokenClient(httpClient, credentials).callToken("https://api.sberbank.ru/escrow")) {
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

    protected CloseableHttpResponse callToken(String scope) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        HttpUriRequest request = RequestBuilder
                .post("https://api.sberbank.ru/ru/prod/tokens/v2/oauth")
                .addHeader("Authorization", "Basic " + credentials.getBasicAuthenticationString())
                .addHeader("x-ibm-client-id", credentials.getClientId())
                .addHeader("rquid", getNewUUID())
                .setEntity(new UrlEncodedFormEntity(
                        asList(
                                new BasicNameValuePair("grant_type", "client_credentials"),
                                new BasicNameValuePair("scope", scope)
                        ))
                ).build();

        return httpClient.execute(request);
    }

    public final String getNewTokenID(String scope) {
        try (CloseableHttpResponse response = callToken(scope)) {
            HttpEntity entity = response.getEntity();
            String rsBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            if (response.getStatusLine().getStatusCode() == 200) {
                HashMap<String, ?> mp = new Gson().fromJson(rsBody, HashMap.class);
                return (String) mp.get("access_token");
            } else {
                throw new RuntimeException(response.getStatusLine().toString() + "\n" + rsBody);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void reset() {
        tokens.clear();
    }

    public String getTokenId(String scopeName) {
        return tokens.computeIfAbsent(scopeName, this::getNewTokenID);
    }
}
