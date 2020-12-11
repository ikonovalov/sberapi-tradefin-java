package ru.codeunited.sberapi;

import com.google.gson.Gson;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Logger log = LoggerFactory.getLogger(TokenClient.class);

    private final ConcurrentMap<String, String> tokens = new ConcurrentHashMap<>();

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

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        CloseableHttpResponse execute = httpClient.execute(request);
        stopWatch.stop();
        log.info("{} -> {}", request.getURI(), stopWatch);
        return execute;
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
