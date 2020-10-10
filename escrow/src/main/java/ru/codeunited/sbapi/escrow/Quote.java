package ru.codeunited.sbapi.escrow;

public final class Quote {
    private final String tariff;
    private final Integer limit;
    private final Integer remaining;

    public Quote(String tariff, Integer limit, Integer remaining) {
        this.tariff = tariff;
        this.limit = limit;
        this.remaining = remaining;
    }

    public String getTariff() {
        return tariff;
    }

    public Integer getLimit() {
        return limit;
    }

    public Integer getRemaining() {
        return remaining;
    }

    @Override
    public String toString() {
        return "Quote{" +
                "tariff='" + tariff + '\'' +
                ", limit=" + limit +
                ", remaining=" + remaining +
                '}';
    }
}
