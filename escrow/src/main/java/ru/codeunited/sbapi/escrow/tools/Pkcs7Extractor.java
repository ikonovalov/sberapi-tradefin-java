package ru.codeunited.sbapi.escrow.tools;

import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.*;
import org.bouncycastle.util.Selector;
import org.bouncycastle.util.encoders.Base64;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Collection;

public class Pkcs7Extractor {

    public static String readContent(byte[] signedData, Charset charset) throws CMSException {
        CMSSignedData cmsSignedData = signedDataFrom(signedData);
        dumpSignatureRelatedInfo(cmsSignedData, System.out);
        CMSTypedData signedContent = cmsSignedData.getSignedContent();
        if (signedContent == null) {
            throw new IllegalArgumentException("SignedContend is null. Detached signature? Weight is only " + signedData.length + " bytes");
        }
        byte[] byteContent = (byte[]) signedContent.getContent();
        return new String(byteContent, charset);
    }

    public static CMSSignedData signedDataFrom(byte[] signedData) throws CMSException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(signedData);
        return new CMSSignedData(inputStream);
    }

    public static void dumpSignatureRelatedInfo(CMSSignedData signedData, PrintStream out) {
        out.println("CMS version: " + signedData.getVersion());

        SignerInformationStore signerInfos = signedData.getSignerInfos();
        out.println("CMS signers (" + signerInfos.size() + "):");

        // Print and iterate SignerInfos
        signerInfos.forEach(si -> {
            SignerId sid = si.getSID();
            if (sid.getSerialNumber() != null)
                out.println("\tserial: " + sid.getSerialNumber().toString(16));
            else
                out.println("\tserial is not specified");

            if (sid.getSubjectKeyIdentifier() != null)
                out.println("\tski:    " + Base64.toBase64String(sid.getSubjectKeyIdentifier()));
            out.println("\tissuer: " + sid.getIssuer());

            out.println();
        });

        // Print certificates
        Collection<X509CertificateHolder> certs = signedData.getCertificates().getMatches(new WhateverSelector<>());
        out.println("CMS certificates (" + certs.size() + "):");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        certs.forEach(x509 -> {
            out.println("\tsubject: " + x509.getSubject());
            out.println("\tissuer : " + x509.getIssuer());
            out.println("\tserial : " + x509.getSerialNumber().toString(16));
            out.println("\tvalid  : " + sdf.format(x509.getNotBefore()) + " - " + sdf.format(x509.getNotAfter()));
            out.println();
        });

        // Print CRLs
        Collection<X509CRLHolder> crls = signedData.getCRLs().getMatches(new WhateverSelector<>());
        out.println("CMS CRLs (" + crls.size() + ")");
        out.println();
    }

    static class WhateverSelector<T> implements Selector<T> {
        @Override
        public boolean match(T x509CertificateHolder) {
            return true;
        }

        @Override
        public Object clone() {
            return this;
        }
    }
}
