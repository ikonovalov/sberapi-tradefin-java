package ru.codeunited.sbapi.escrow;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
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

import static java.lang.Integer.MIN_VALUE;
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

    private Quote quote = new Quote("UNKNOWN", MIN_VALUE, MIN_VALUE);

    public EscrowClient(CloseableHttpClient httpClient, CredentialsSource credentials, TokenClient tokenClient) throws JAXBException {
        this.httpClient = httpClient;
        this.credentials = credentials;
        this.tokenClient = tokenClient;
        this.jaxbContext = JAXBContext.newInstance("ru.sbrf.escrow.tfido.model");
    }

    public Quote getQuote() {
        return quote;
    }

    public String baseURI() {
        return baseURI;
    }

    protected URI uri(String... path) {
        String p = stream(path)
                .reduce(baseURI(), (left, right) -> left + "/" + right);
        return URI.create(p);
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
            String rsBody = dumpBody(entity);

            if (statusLine.getStatusCode() == 200) {
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                JAXBElement<ResidentialComplexDetails> rcElement =
                        (JAXBElement<ResidentialComplexDetails>) unmarshaller.unmarshal(new StringReader(rsBody));
                this.quote = extractQuota(response);
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
            String rsBody = dumpBody(entity);

            if (statusLine.getStatusCode() == 200) {
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                JAXBElement<IndividualTerms> rcElement =
                        (JAXBElement<IndividualTerms>) unmarshaller.unmarshal(new StringReader(rsBody));
                this.quote = extractQuota(response);
                return Optional.of(rcElement.getValue());
            } else if (statusLine.getStatusCode() == 404) {
                return Optional.empty();
            } else {
                throw new RuntimeException(statusLine + "\n" + rsBody);
            }
        }
    }

    public UUID createIndividualTerms(String base64CEncodedCms) throws IOException {
        String tokenId = tokenClient.getTokenId(scopeName);

        HttpUriRequest request = RequestBuilder
                .post(uri("individual-terms"))
                .addHeader("Authorization", "Bearer " + tokenId)
                .addHeader("x-ibm-client-id", credentials.getClientId())
                .addHeader("x-introspect-rquid", rqUID.trimmed())
                .addHeader("Content-Type", "application/cms")
                .setEntity(new StringEntity(base64CEncodedCms, StandardCharsets.UTF_8))
                .build();

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            String rsBody = dumpBody(entity);
            if (statusLine.getStatusCode() == 202) {
                this.quote = extractQuota(response);
                return null;
            } else {
                String headers = dumpHeaders(response);
                throw new RuntimeException(statusLine + "\n" + headers + rsBody);
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
            String rsBody = dumpBody(entity);
            if (statusLine.getStatusCode() == 200) {
                this.quote = extractQuota(response);
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
            String rsBody = dumpBody(entity);

            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                this.quote = extractQuota(response);
                return Boolean.TRUE;
            } else if (statusCode == 404 || statusCode == 406) {
                this.quote = extractQuota(response);
                return Boolean.FALSE;
            } else {
                String headers = dumpHeaders(response);
                throw new RuntimeException(statusLine + "\n" + headers + rsBody);
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
            String rsBody = dumpBody(entity);

            if (statusLine.getStatusCode() == 200) {
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                JAXBElement<Status> rcElement =
                        (JAXBElement<Status>) unmarshaller.unmarshal(new StringReader(rsBody));
                this.quote = extractQuota(response);
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
            String rsBody = dumpBody(entity);

            if (statusLine.getStatusCode() == 200) {
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                EscrowAccountList root = (EscrowAccountList) unmarshaller.unmarshal(new StringReader(rsBody));
                this.quote = extractQuota(response);
                return root.getEscrowAccount();
            } else {
                String headers = dumpHeaders(response);
                throw new RuntimeException(statusLine + "\n" + headers + rsBody);
            }
        }
    }

    public List<EscrowAccountOperation> getAccountOperationList(
            int offset,
            int limit,
            String commissioningObjectCode,
            LocalDate startReportDate,
            LocalDate endReportDate ) throws Exception {

        String tokenId = tokenClient.getTokenId(scopeName);
        HttpUriRequest request = RequestBuilder
                .post(uri("account-oper-list"))
                .addHeader("Authorization", "Bearer " + tokenId)
                .addHeader("Accept", "application/xml")
                .addHeader("x-ibm-client-id", credentials.getClientId())
                .addHeader("x-introspect-rquid", rqUID.trimmed())
                .setEntity(new UrlEncodedFormEntity(
                        asList(
                                new BasicNameValuePair("offset", String.valueOf(offset)),
                                new BasicNameValuePair("limit", String.valueOf(limit)),
                                new BasicNameValuePair("commisioningObjectCode", commissioningObjectCode),
                                new BasicNameValuePair("startReportDate", ISO_DATE.format(startReportDate)),
                                new BasicNameValuePair("endReportDate", ISO_DATE.format(endReportDate))
                        )
                ))
                .build();

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            String rsBody = dumpBody(entity);

            if (statusLine.getStatusCode() == 200) {
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                EscrowAccountOperationList root = (EscrowAccountOperationList) unmarshaller.unmarshal(new StringReader(rsBody));
                this.quote = extractQuota(response);
                return root.getEscrowAccountOperation();
            } else {
                String headers = dumpHeaders(response);
                throw new RuntimeException(statusLine + "\n" + headers + rsBody);
            }
        }
    }

    /**
     * Read http
     * @param entity
     * @return
     */
    private String dumpBody(HttpEntity entity) {
        try {
            return EntityUtils.toString(entity, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println(e.toString());
            return "";
        }
    }

    private String dumpHeaders(CloseableHttpResponse response) {
        return stream(response.getAllHeaders())
                .map(h -> h.getName() + ": " + h.getValue())
                .reduce((acc,val) -> acc = acc + val + "\n")
                .orElse("");
    }

    private Quote extractQuota(CloseableHttpResponse response) {
        Header limit = response.getHeaders("X-RateLimit-Limit")[0];
        Header remaining = response.getHeaders("X-RateLimit-Remaining")[0];
        String[] splited = limit.getValue().replaceAll("name=", "").split(",");
        String tariff = splited[0];
        Integer limitNum = Integer.valueOf(limit.getValue().split(",")[1].replaceAll(";", ""));
        Integer remainingNum = Integer.valueOf(remaining.getValue().split(",")[1].replaceAll(";", ""));
        return new Quote(tariff, limitNum, remainingNum);
    }


}
