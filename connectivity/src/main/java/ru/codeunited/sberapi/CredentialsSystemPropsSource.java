package ru.codeunited.sberapi;

public class CredentialsSystemPropsSource implements CredentialsSource {

    @Override
    public String getClientId() {
        return System.getProperty("clientid");
    }

    @Override
    public String getClientSecret() {
        return System.getProperty("clientsecret");
    }
}
