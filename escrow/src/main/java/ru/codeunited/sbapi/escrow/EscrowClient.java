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
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

public class EscrowClient {

    private final JAXBContext jaxbContext;

    private final CloseableHttpClient httpClient;

    private final CredentialsSource credentials;

    private final TokenClient tokenClient;

    private final String scopeName = "https://api.sberbank.ru/escrow";

    private final String baseURI = "https://api.sberbank.ru/ru/prod/v2/escrow";

    private final RqUID rqUID = new RqUID();

    public EscrowClient(CloseableHttpClient httpClient, CredentialsSource credentials, TokenClient tokenClient) throws JAXBException {
        this.httpClient = httpClient;
        this.credentials = credentials;
        this.tokenClient = tokenClient;
        this.jaxbContext = JAXBContext.newInstance("ru.sbrf.escrow.tfido.model");
    }

    public String baseURI() {
        return baseURI;
    }

    protected URI uri(String... path) {
        String p = stream(path)
                .reduce(baseURI(), (left, right) -> left + "/" + right);
        return URI.create(p);
    }

    /**
     * Read http
     * @param entity
     * @return
     */
    protected String readBody(HttpEntity entity) {
        try {
            return EntityUtils.toString(entity, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println(e.toString());
            return "";
        }
    }

    public ResidentialComplexDetails getResidentialComplexDetails() throws Exception {
        String tokenId = tokenClient.getTokenId(scopeName);
        HttpUriRequest request = RequestBuilder
                .get(uri("residential-complex"))
                .addHeader("Authorization", "Bearer " + tokenId)
                .addHeader("x-ibm-client-id", credentials.getClientId())
                .addHeader("x-introspect-rquid", rqUID.trimmed())
                .build();

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            String rsBody = readBody(entity);

            if (statusLine.getStatusCode() == 200) {
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                JAXBElement<ResidentialComplexDetails> rcElement =
                        (JAXBElement<ResidentialComplexDetails>) unmarshaller.unmarshal(new StringReader(rsBody));
                return rcElement.getValue();
            } else {
                throw new RuntimeException(statusLine + "\n" + rsBody);
            }
        }
    }

    public Optional<IndividualTerms> getIndividualTerms(UUID uuid) throws Exception {
        URI uri = uri("individual-terms", uuid.toString());
        return getIndividualTerms(uri);
    }

    public Optional<IndividualTerms> getIndividualTerms(URI uri) throws Exception {
        String tokenId = tokenClient.getTokenId(scopeName);
        HttpUriRequest request = RequestBuilder
                .get(uri)
                .addHeader("Authorization", "Bearer " + tokenId)
                .addHeader("x-ibm-client-id", credentials.getClientId())
                .addHeader("x-introspect-rquid", rqUID.trimmed())
                .build();

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            String rsBody = readBody(entity);

            if (statusLine.getStatusCode() == 200) {
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                JAXBElement<IndividualTerms> rcElement =
                        (JAXBElement<IndividualTerms>) unmarshaller.unmarshal(new StringReader(rsBody));
                return Optional.of(rcElement.getValue());
            } else if (statusLine.getStatusCode() == 404) {
                return Optional.empty();
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
                           String depositorIdentificationDocumentType,
                           String depositorIdentificationDocumentSeries,
                           String depositorIdentificationDocumentNumber,
                           String depositorIdentificationDocumentIssuer,
                           String depositorIdentificationDocumentIssueDate,
                           String depositorPhone,
                           String depositorEmail,
                           String beneficiaryTaxId,
                           String beneficiaryAuthorizedRepresentativeCertificateSerial,
                           String equityParticipationAgreementNumber,
                           String equityParticipationAgreementDate,
                           String estateObjectCommisioningObjectCode,
                           String estateObjectType,
                           String estateObjectConstructionNumber) throws Exception {

        String tokenId = tokenClient.getTokenId(scopeName);

        HttpUriRequest request = RequestBuilder
                .post(uri("individual-terms", "draft"))
                .addHeader("Authorization", "Bearer " + tokenId)
                .addHeader("x-ibm-client-id", credentials.getClientId())
                .addHeader("x-introspect-rquid", rqUID.trimmed())
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

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            String rsBody = readBody(entity);
            if (statusLine.getStatusCode() == 200) {
                return rsBody;
            } else {
                throw new RuntimeException(statusLine + "\n" + rsBody);
            }
        }
    }

    public Boolean cancel(UUID uuid) throws Exception {
        String tokenId = tokenClient.getTokenId(scopeName);
        HttpUriRequest request = RequestBuilder
                .put(uri("individual-terms", uuid.toString(), "cancel"))
                .addHeader("Authorization", "Bearer " + tokenId)
                .addHeader("x-ibm-client-id", credentials.getClientId())
                .addHeader("x-introspect-rquid", rqUID.trimmed())
                .build();

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            String rsBody = readBody(entity);

            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                return Boolean.TRUE;
            } else if (statusCode == 404 || statusCode == 406) {
                return Boolean.FALSE;
            } else {
                throw new RuntimeException(statusLine + "\n" + rsBody);
            }
        }

    }

    public Optional<Status> status(UUID uuid) throws Exception {
        String tokenId = tokenClient.getTokenId(scopeName);
        HttpUriRequest request = RequestBuilder
                .get(uri("individual-terms", uuid.toString(), "status"))
                .addHeader("Authorization", "Bearer " + tokenId)
                .addHeader("x-ibm-client-id", credentials.getClientId())
                .addHeader("x-introspect-rquid", rqUID.trimmed())
                .build();

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            String rsBody = readBody(entity);

            if (statusLine.getStatusCode() == 200) {
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                JAXBElement<Status> rcElement =
                        (JAXBElement<Status>) unmarshaller.unmarshal(new StringReader(rsBody));
                return Optional.of(rcElement.getValue());
            } else if (statusLine.getStatusCode() == 404) {
                return Optional.empty(); // IT not found or canceled already
            } else {
                throw new RuntimeException(statusLine + "\n" + rsBody);
            }
        }
    }

    public List<EscrowAccount> getAccountList(
            int offset,
            int limit,
            String сommisioningObjectCode,
            LocalDate startReportDate,
            LocalDate endReportDate ) throws Exception {

        String tokenId = tokenClient.getTokenId(scopeName);
        HttpUriRequest request = RequestBuilder
                .post(uri("account-list"))
                .addHeader("Authorization", "Bearer " + tokenId)
                .addHeader("Accept", "application/xml")
                .addHeader("x-ibm-client-id", credentials.getClientId())
                .addHeader("x-introspect-rquid", rqUID.trimmed())
                .setEntity(new UrlEncodedFormEntity(
                        asList(
                                new BasicNameValuePair("offset", String.valueOf(offset)),
                                new BasicNameValuePair("limit", String.valueOf(limit)),
                                new BasicNameValuePair("commisioningObjectCode", сommisioningObjectCode),
                                new BasicNameValuePair("startReportDate", ISO_DATE.format(startReportDate)),
                                new BasicNameValuePair("endReportDate", ISO_DATE.format(endReportDate))
                        )
                ))
                .build();

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            String rsBody = readBody(entity);

            if (statusLine.getStatusCode() == 200) {
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                EscrowAccountList root = (EscrowAccountList) unmarshaller.unmarshal(new StringReader(rsBody));
                return root.getEscrowAccount();
            } else {
                throw new RuntimeException(statusLine + "\n" + rsBody);
            }
        }
    }


}
