package ru.codeunited.sberapi;

import java.util.Base64;

public interface CredentialsSource {

    String getClientId();

    String getClientSecret();

    default String getBasicAuthenticationString() {
        String basicAuth = getClientId() + ":" + getClientSecret();
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(basicAuth.getBytes());
    }
}
