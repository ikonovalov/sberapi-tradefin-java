package ru.codeunited.sbapi.escrow;

import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Inspired by https://reflectoring.io/how-to-sign/
 */
public class EasyCmsExample {

    /* В этом примере используется криптопровайдер от BC */
    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public static void main(String[] args) throws CMSException, CertificateException, KeyStoreException, NoSuchProviderException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, OperatorCreationException {

        // Загружаем хранилище, ключи и сертификаты
        KeyStore store = KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME);
        File keystoreFile = new File(System.getProperty("keystore"));
        String keystorePwd = System.getProperty("keystorePwd");
        char[] keyPassword = null;
        store.load(new FileInputStream(keystoreFile), keystorePwd.toCharArray());

        String alias = store.aliases().nextElement();

        X509Certificate certificate = (X509Certificate) store.getCertificate(alias);
        PrivateKey key = (PrivateKey) store.getKey(alias, keyPassword);

        // Формируем геренратор контейнера
        List<Certificate> certList = new ArrayList<>();
        CMSTypedData msg = new CMSProcessableByteArray("Hello world!".getBytes());
        certList.add(certificate);
        Store certs = new JcaCertStore(certList);

        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        ContentSigner sha256RsaSigner = new JcaContentSignerBuilder("SHA256withRSA").build(key);

        gen.addSignerInfoGenerator(
                new JcaSignerInfoGeneratorBuilder(
                        new JcaDigestCalculatorProviderBuilder().build())
                        .build(sha256RsaSigner, certificate));

        gen.addCertificates(certs);

        CMSSignedData sigData = gen.generate(msg, true);

        byte[] encoded = sigData.getEncoded();
        System.out.println("Bytes " + encoded.length);
        String b64 = Base64.getEncoder().encodeToString(encoded);
        System.out.println("-->\n" + b64 + "\n<--");
    }
}
