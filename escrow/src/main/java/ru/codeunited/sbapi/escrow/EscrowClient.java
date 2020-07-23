package ru.codeunited.sbapi.escrow;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import ru.codeunited.sberapi.*;
import ru.sbrf.escrow.tfido.model.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static java.util.Arrays.asList;

public class EscrowClient {

    private final JAXBContext jaxbContext;

    private final CloseableHttpClient httpClient;

    private final CredentialsSource credentials;

    private final TokenClient tokenClient;

    private final String scopeName = "https://api.sberbank.ru/escrow";

    public EscrowClient(CloseableHttpClient httpClient, CredentialsSource credentials, TokenClient tokenClient) throws JAXBException {
        this.httpClient = httpClient;
        this.credentials = credentials;
        this.tokenClient = tokenClient;
        this.jaxbContext = JAXBContext.newInstance("ru.sbrf.escrow.tfido.model");
    }

    public ResidentialComplexDetails getResidentialComplexDetails() throws Exception {
        String tokenId = tokenClient.getTokenId(scopeName);
        HttpUriRequest request = RequestBuilder
                .get("https://api.sberbank.ru/ru/prod/v2/escrow/residential-complex")
                .addHeader("Authorization", "Bearer " + tokenId)
                .addHeader("x-ibm-client-id", credentials.getClientId())
                .addHeader("x-introspect-rquid", UUID.randomUUID().toString().replaceAll("-", ""))
                .build();

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            String rsBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);

            if (statusLine.getStatusCode() == 200) {
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                JAXBElement<ResidentialComplexDetails> rcElement = (JAXBElement<ResidentialComplexDetails>) unmarshaller.unmarshal(new StringReader(rsBody));
                return rcElement.getValue();
            } else {
                throw new RuntimeException(statusLine + "\n" + rsBody);
            }
        }
    }

    public String getDraft(String escrowAmount,
                            String depositorLastName,
                            String depositorFirstName,
                            String depositorMiddleName,
                            String depositorRegistrationAddress,
                            String depositorCurrentAddress,
                            String depositorIdentificationDocumentType, String depositorIdentificationDocumentSeries, String depositorIdentificationDocumentNumber, String depositorIdentificationDocumentIssuer, String depositorIdentificationDocumentIssueDate, String depositorPhone, String depositorEmail, String beneficiaryTaxId, String beneficiaryAuthorizedRepresentativeCertificateSerial, String equityParticipationAgreementNumber, String equityParticipationAgreementDate, String estateObjectCommisioningObjectCode, String estateObjectType, String estateObjectConstructionNumber) throws Exception {
        String tokenId = tokenClient.getTokenId(scopeName);
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        HttpUriRequest request = RequestBuilder
                .post("https://api.sberbank.ru/ru/prod/v2/escrow/individual-terms/draft")
                .addHeader("Authorization", "Bearer " + tokenId)
                .addHeader("x-ibm-client-id", credentials.getClientId())
                .addHeader("x-introspect-rquid", uuid)
                .setEntity(new UrlEncodedFormEntity(
                        asList(
                                new BasicNameValuePair("escrowAmount", escrowAmount),
                                new BasicNameValuePair("depositorLastName", depositorLastName),
                                new BasicNameValuePair("depositorFirstName", depositorFirstName),
                                new BasicNameValuePair("depositorMiddleName", depositorMiddleName),
                                new BasicNameValuePair("depositorRegistrationAddress", depositorRegistrationAddress),
                                new BasicNameValuePair("depositorCurrentAddress", depositorCurrentAddress),
                                new BasicNameValuePair("depositorIdentificationDocumentType", depositorIdentificationDocumentType),
                                new BasicNameValuePair("depositorIdentificationDocumentSeries", depositorIdentificationDocumentSeries),
                                new BasicNameValuePair("depositorIdentificationDocumentNumber", depositorIdentificationDocumentNumber),
                                new BasicNameValuePair("depositorIdentificationDocumentIssuer", depositorIdentificationDocumentIssuer),
                                new BasicNameValuePair("depositorIdentificationDocumentIssueDate", depositorIdentificationDocumentIssueDate),
                                new BasicNameValuePair("depositorPhone", depositorPhone),
                                new BasicNameValuePair("depositorEmail", depositorEmail),
                                new BasicNameValuePair("beneficiaryTaxId", beneficiaryTaxId),
                                new BasicNameValuePair("beneficiaryAuthorizedRepresentativeCertificateSerial", beneficiaryAuthorizedRepresentativeCertificateSerial),
                                new BasicNameValuePair("equityParticipationAgreementNumber", equityParticipationAgreementNumber),
                                new BasicNameValuePair("equityParticipationAgreementDate", equityParticipationAgreementDate),
                                new BasicNameValuePair("estateObjectCommisioningObjectCode", estateObjectCommisioningObjectCode),
                                new BasicNameValuePair("estateObjectType", estateObjectType),
                                new BasicNameValuePair("estateObjectConstructionNumber", estateObjectConstructionNumber)
                        ), StandardCharsets.UTF_8) // Важно корректно указывать кодировку
                ).build();

        try(CloseableHttpResponse response = httpClient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            String rsBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            if (statusLine.getStatusCode() == 200) {
                System.out.println(rsBody);
                return rsBody;
            } else {
                throw new RuntimeException(statusLine + "\n" + rsBody);
            }
        }
    }
}
