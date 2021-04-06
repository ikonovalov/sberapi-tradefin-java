package ru.codeunited.sbapi.escrow.tools;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSTypedData;

import java.io.ByteArrayInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Pkcs7Extractor {

    public static String readContent(byte[] signedData) throws CMSException {
        CMSSignedData cmsSignedData = signedDataFrom(signedData);
        CMSTypedData signedContent = cmsSignedData.getSignedContent();
        if (signedContent == null) {
            throw new IllegalArgumentException("SignedContend is null. Detached signature? Weight is only " + signedData.length + " bytes");
        }
        byte[] byteContent = (byte[]) signedContent.getContent();
        return new String(byteContent, UTF_8);
    }

    public static CMSSignedData signedDataFrom(byte[] signedData) throws CMSException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(signedData);
        return new CMSSignedData(inputStream);
    }
}
