package ru.codeunited.sbapi.escrow.tools;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Pkcs7Extractor {
    public static void main(String[] args) throws IOException, CMSException {
        byte[] encoded = Files.readAllBytes(Paths.get(
                "C:\\Users\\igor_\\Downloads",
                //"individualTerms_2020-09-14T122915.xml (1) (1).sig"
                //"82d8d6af4f0e72605de9f420fcbb3717.log.txt"
                //"file01.txt"
                //"individualTerms_2020-09-14T122915.xml (3).sig"
                "2487c31ae84006e8ad2b582b9a5f41ee.log.txt"
                )
        );
        byte[] decoded = Base64.getDecoder().decode(encoded);
        String xml = readContent(decoded);
        System.out.println(xml);

    }

    public static String readContent(byte[] signedData) throws CMSException {
        CMSSignedData cmsSignedData = signedDataFrom(signedData);
        byte[] byteContent = (byte[]) cmsSignedData.getSignedContent().getContent();
        return new String(byteContent, UTF_8);
    }

    public static CMSSignedData signedDataFrom(byte[] signedData) throws CMSException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(signedData);
        return new CMSSignedData(inputStream);
    }
}
