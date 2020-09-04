package ru.codeunited.sbapi.escrow;

import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.codeunited.sberapi.*;
import ru.sbrf.escrow.tfido.model.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EscrowShowcase {

    private static final Logger log = LoggerFactory.getLogger(EscrowShowcase.class);

    public static void main(String[] args) throws Exception {

        log.info("Escrow");

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
        log.info("ЖК: " + rc.getResidentialComplexInfo().getName());

        // Выбор очереди (берем первый попавшийся)
        Optional<CommissioningObjectDetails> commissioningObjectOpt = rc.getCommissioningObject().stream().findFirst();
        if (!commissioningObjectOpt.isPresent()) {
            throw new RuntimeException("У ЖК '" + rc.getResidentialComplexInfo().getName()  + "' отсутствуют очереди ввода");
        }

        CommissioningObjectDetails commissioningObjectDetails = commissioningObjectOpt.get();
        String commissioningObjectDetailsCode = commissioningObjectDetails.getCode();
        log.info("Очередь: " + commissioningObjectDetailsCode);

        // Выбор подписанта и его сертификата (ищем первого у кого доступен электронный сертификат)
        AuthorizedRepresentativeDetails representative = residentialComplexDetails
                .getAuthorizedRepresentative()
                .stream()
                .filter(rep -> rep.getBaseInfo().getCertificateSerial() != null)
                .findFirst().orElseThrow(() -> new RuntimeException("Подписант не найден"));

        AuthorizedRepresentative authorizedRepresentative = representative.getBaseInfo();
        String certificateSerial = authorizedRepresentative.getCertificateSerial();

        // Список счетов
        LocalDate start = LocalDate.now().minus(60, ChronoUnit.DAYS);
        LocalDate end = LocalDate.now().minus(30, ChronoUnit.DAYS);
        String objectCode = commissioningObjectDetails.getCode();
        List<EscrowAccount> accountList = escrowClient.getAccountList(0, 1000, objectCode, start, end);

        log.info("Количество счетов с {} по {}: {}", start, end, accountList.size());

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
        log.info("Черновик =>\n{}", individualTermsXML);

        // ПОДПИСАНИЕ И СОЗДАНИЕ ИУ

        final UUID uuid = UUID.fromString(
                accountList.stream().findAny().orElseThrow(() -> new RuntimeException("Accounts not found")).getIndividualTermsId()
        );

        // ПОЛУЧЕНИЕ ИУ по ID

        Optional<IndividualTerms> individualTerms = escrowClient.getIndividualTerms(uuid);
        String getResult = individualTerms.map(it -> "found").orElse("not found");
        log.info("ИУ для {} {}", uuid, getResult);
        individualTerms.ifPresent(it -> {
            log.info("  Beneficiary/LegalName   {}", it.getBeneficiary().getLegalName());
            log.info("  Depositor/Name/LastName {}", it.getDepositor().getName().getLastName());
        });

        // Получение статуса ИУ по ID
        Optional<Status> status = escrowClient.status(uuid);
        String statusResult = status.map(it ->  "found").orElse("not found");
        log.info("Статус ИУ для {} {}", uuid, statusResult);
        status.ifPresent(s -> {
            log.info("  Status code: {}", s.getStatusCode());
            log.info("  Processing details: {}", s.getProcessingDetails());
        });

        // Отмена ИУ по ID
        escrowClient.cancel(UUID.randomUUID());
    }
}
