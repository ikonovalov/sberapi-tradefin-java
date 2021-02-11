package ru.codeunited.sbapi.escrow.tools;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.bouncycastle.util.Selector;
import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.zip.CRC32;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Pkcs7Extractor {

    public static String readContent(byte[] signedData) throws CMSException {
        CMSSignedData cmsSignedData = signedDataFrom(signedData);
        cmsSignedData.getSignerInfos().getSigners().forEach(sh -> {
            SignerId sid = sh.getSID();
            System.out.println("Signer info: ");
            System.out.println("\tSigner SN:     " + Hex.toHexString(sid.getSerialNumber().toByteArray()).toUpperCase() + " " + sid.getSerialNumber());
            System.out.println("\tSigner ISSUER: " + sid.getIssuer().toString());
            if (sid.getSubjectKeyIdentifier() != null) {
                System.out.println("\tSigner SKI: " + Base64.getEncoder().encodeToString(sid.getSubjectKeyIdentifier()));
            }
        });
        cmsSignedData.getCertificates().getMatches(new Selector<X509CertificateHolder>() {
            @Override
            public boolean match(X509CertificateHolder x509CertificateHolder) {
                return true;
            }

            @Override
            public Object clone() {
                return null;
            }
        }).forEach(ch -> {
            System.out.println("Certificate attached: ");
            X500Name name = ch.getSubject();
            System.out.println("\tSerial:  " + Hex.toHexString(ch.getSerialNumber().toByteArray()).toUpperCase() + " " + ch.getSerialNumber());
            System.out.println("\tSubject: " + name);
            System.out.println("\tValid:   " + ch.getNotBefore() + " - " + ch.getNotAfter());
            System.out.println("\tIssuer   " + ch.getIssuer());

            CRC32 crc32 = new CRC32();
            try {
                crc32.update(ch.getEncoded());
                System.out.println("\tCRC32:  " + crc32.getValue());
            } catch (IOException e) {
                e.printStackTrace();
            }


        });
        byte[] byteContent = (byte[]) cmsSignedData.getSignedContent().getContent();
        return new String(byteContent, UTF_8);
    }

    public static CMSSignedData signedDataFrom(byte[] signedData) throws CMSException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(signedData);
        return new CMSSignedData(inputStream);
    }
}
