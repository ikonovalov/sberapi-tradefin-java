package ru.codeunited.sbapi.escrow;

import org.apache.http.impl.client.CloseableHttpClient;
import ru.codeunited.sberapi.*;
import ru.sbrf.escrow.tfido.model.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class EscrowShowcase {

    public static void main(String[] args) throws Exception {

        // Насройка подключения и прав доступа
        ApacheHttpClientCustomSSLFactory httpClientFactory = new ApacheHttpClientCustomSSLFactory(new TlsFactorySystemPropsSource());
        CloseableHttpClient httpClient = httpClientFactory.build();
        CredentialsSource credentials = new CredentialsSystemPropsSource();

        // Инициализация клиента для получения token
        TokenClient tokenClient = new TokenClient(httpClient, credentials);

        // Инициализация клиента для экскроу
        EscrowClient escrowClient = new EscrowClient(httpClient, credentials, tokenClient);

        // Получение жилых комплексов и очередей ввода в эксплуатацию
        ResidentialComplexDetails residentialComplexDetails = escrowClient.getResidentialComplexDetails();

        // Выбор ЖК (берем первый попавшийся)
        Optional<ResidentialComplex> rcOpt = residentialComplexDetails.getResidentialComplex().stream().findFirst();
        if (!rcOpt.isPresent()) {
            throw new RuntimeException("Отсутствуют жилые комплексы");
        }
        ResidentialComplex rc = rcOpt.get();
        System.out.println("ЖК: " + rc.getResidentialComplexInfo().getName());

        // Выбор очереди (берем первый попавшийся)
        Optional<CommissioningObjectDetails> commissioningObjectOpt = rc.getCommissioningObject().stream().findFirst();
        if (!commissioningObjectOpt.isPresent()) {
            throw new RuntimeException("У ЖК '" + rc.getResidentialComplexInfo().getName()  + "' отсутствуют очереди ввода");
        }

        CommissioningObjectDetails commissioningObjectDetails = commissioningObjectOpt.get();
        String commissioningObjectDetailsCode = commissioningObjectDetails.getCode();
        System.out.println("Очередь: " + commissioningObjectDetailsCode);

        // Выбор подписанта и его сертификата (ищем первого у кого доступен электронный сертификат)
        Optional<AuthorizedRepresentativeDetails> firstAuthorized = residentialComplexDetails
                .getAuthorizedRepresentative()
                .stream()
                .filter(rep -> rep.getBaseInfo().getCertificateSerial() != null)
                .findFirst();
        if (!firstAuthorized.isPresent()) {
            throw new IllegalStateException("No one AuthorizedRepresentativeDetails");
        }
        AuthorizedRepresentativeDetails representative = firstAuthorized.get();
        AuthorizedRepresentative authorizedRepresentative = representative.getBaseInfo();
        String certificateSerial = authorizedRepresentative.getCertificateSerial();

        // Список счетов
        List<EscrowAccount> accountList = escrowClient.getAccountList(0, 1000,
                commissioningObjectDetails.getCode(),
                LocalDate.now().minus(20, ChronoUnit.MONTHS),
                LocalDate.now()
        );

        System.out.println("Количество счетов: " + accountList.size());

        // Формирование запроса для черновика
        String individualTermsXML = escrowClient.getDraft(
                "1000500",
                "Иванов",
                "Иван",
                "Иванович",
                "РФ, Москва, ул Пролетарская, 97/12",
                "РФ, Москва, ул Пролетарская, 97/12",
                "99",
                "2341",
                "903904",
                "ОВД Галактики Млечный путь",
                "2000-02-03",
                "+791600100203",
                "myemail@worldwide.com",
                "0012345688",
                certificateSerial,
                "2019/АСБ-25",
                "2019-08-13",
                commissioningObjectDetailsCode,
                "RESIDENTIAL",
                "26b"
        );


        // ПОДПИСАНИЕ И СОЗДАНИЕ ИУ

        // ПОЛУЧЕНИЕ ИУ по ID
        Optional<IndividualTerms> individualTerms = escrowClient.getIndividualTerms(UUID.randomUUID());

    }
}
