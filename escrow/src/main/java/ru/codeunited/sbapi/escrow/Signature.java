package ru.codeunited.sbapi.escrow;

import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class Signature {

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public static final String GOST_3411_WITHECGOST_3410_2012_256 = "GOST3411WITHECGOST3410-2012-256";
    public static final String GOST_3411_WITHECGOST_3410_2012_512 = "GOST3411WITHECGOST3410-2012-512";
    public static final String SHA256withRSA = "SHA256withRSA";

    public static final X509Certificate[] NO_OTHER = new X509Certificate[]{};

    private PrivateKey privateKey;
    private X509Certificate certificate;
    private String alg;
    private String alias;
    private char[] keyPwd;

    public Signature() throws NoSuchProviderException, KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore store = loadKeyStore();
        this.keyPwd = System.getProperty("keyPwd") != null ? System.getProperty("keyPwd").toCharArray() : null;
        this.alg = System.getProperty("alg", GOST_3411_WITHECGOST_3410_2012_256);
        this.alias = System.getProperty("alias");
        if (this.alias == null) { // используем первый попавшийся сертификат
            this.alias = store.aliases().nextElement();
        }
        this.certificate = (X509Certificate) store.getCertificate(this.alias);
        this.privateKey = (PrivateKey) store.getKey(alias, this.keyPwd);
    }

    public static KeyStore loadKeyStore() throws KeyStoreException, NoSuchProviderException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore store = KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME);
        File keystoreFile = new File(System.getProperty("keystore"));
        String keystorePwd = System.getProperty("keystorePwd");
        store.load(new FileInputStream(keystoreFile), keystorePwd.toCharArray());
        return store;
    }

    public String signAndEncode(byte[] data, X509Certificate[] other) throws Exception {
        CMSSignedData signedData = sign(data, other);
        byte[] encoded = signedData.getEncoded();
        return Base64.getEncoder().encodeToString(encoded);
    }

    public CMSSignedData sign(byte[] data, X509Certificate[] other) throws Exception {

        CMSProcessableByteArray cmsData = new CMSProcessableByteArray(data);

        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        ContentSigner signer = new JcaContentSignerBuilder(alg)
                .setProvider("BC")
                .build(getPrivateKey());

        // Подготовка списка сертификатов (опционально)
        List<X509Certificate> allCerts = new ArrayList<>();
        allCerts.add(getCertificate());
        allCerts.addAll(Arrays.asList(other));
        Store certs = new JcaCertStore(allCerts);

        generator.addSignerInfoGenerator(
                new JcaSignerInfoGeneratorBuilder(
                        new JcaDigestCalculatorProviderBuilder().setProvider("BC").build()
                ).build(signer, certificate)
        );

        generator.addCertificates(certs);

        return generator.generate(cmsData, true);
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }
}
