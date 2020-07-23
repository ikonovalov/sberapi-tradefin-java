package ru.codeunited.sbapi.escrow;

import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class SignatureGenerator {

    private static CMSSignedData sign(byte[] data, PrivateKey privateKey, X509Certificate cert, X509Certificate[] other) throws Exception {

        CMSProcessableByteArray cmsData = new CMSProcessableByteArray(data);

        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        ContentSigner signer = new JcaContentSignerBuilder("GOST3411WITHECGOST3410-2012-512")
                .setProvider("BC")
                .build(privateKey);

        // Подготовка списка сертификатов (опционально)
        List<X509Certificate> allCerts = new ArrayList<>();
        allCerts.add(cert);
        allCerts.addAll(Arrays.asList(other));
        Store certs = new JcaCertStore(allCerts);

        generator.addSignerInfoGenerator(
                new JcaSignerInfoGeneratorBuilder(
                        new JcaDigestCalculatorProviderBuilder().setProvider("BC").build()
                ).build(signer, cert)
        );

        generator.addCertificates(certs);

        return generator.generate(cmsData, true);
    }
}
