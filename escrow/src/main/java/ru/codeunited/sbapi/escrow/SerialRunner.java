package ru.codeunited.sbapi.escrow;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SerialRunner {

    public static final String INPUT = "780007a49dd830f27227720635cc367ab76be271";

    public static void main(String[] args) {
        try {
            byte[] b = Base64.decode(INPUT);
            String hex = new String(Hex.encode(b), StandardCharsets.UTF_8);
            System.out.println("HEX FROM BASE64: " + hex);
        } catch (Exception e) {
            System.err.println("[SKIP] Can't convert to HEX");
        }

        byte[] decode = null;
        try {
            decode = Base64.decode(INPUT);
            System.out.println(
                    "BIGINT FROM BASE64: " + toBigInt(decode)
            );
        } catch (Exception e) {
            System.err.println("[SKIP] This is not BASE64");
        }
        try {
            decode = Hex.decode(INPUT);
            System.out.println(
                    "BIGINT FROM HEX: " + toBigInt(decode)
            );
        } catch (Exception e) {
            System.err.println("[SKIP] This is not HEX");
        }
    }

    private static BigInteger toBigInt(byte[] arr) {
        byte[] rev = new byte[arr.length + 1];
        for (int i = 0, j = arr.length; j > 0; i++, j--)
            rev[j] = arr[i];
        return new BigInteger(rev);
    }
}
