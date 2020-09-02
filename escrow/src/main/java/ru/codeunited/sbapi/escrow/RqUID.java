package ru.codeunited.sbapi.escrow;

import java.util.UUID;

public class RqUID {

    private final UUID uuid;

    public RqUID() {
        this(UUID.randomUUID());
    }

    public RqUID(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID get() {
        return uuid;
    }

    /**
     * Return without -
     * @return
     */
    public String trimmed() {
        return uuid.toString().replaceAll("-", "");
    }

    @Override
    public String toString() {
        return trimmed();
    }
}
