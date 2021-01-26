package ru.codeunited.sbapi.escrow.tools;

import java.util.UUID;

public class UUIDExtractor {

    public static long longFromUUID(String fromString) {
        return UUID.fromString(fromString).getLeastSignificantBits();
    }

    public static UUID uuidFromLong(long val) {
        return new UUID(val, val);
    }
}
