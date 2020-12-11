package ru.codeunited.sbapi.escrow;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;

public final class Quote {
    private final String tariff;
    private final Integer limit;
    private final Integer remaining;

    public Quote(String tariff, Integer limit, Integer remaining) {
        this.tariff = tariff;
        this.limit = limit;
        this.remaining = remaining;
    }

    public static Quote from(CloseableHttpResponse response) {
        Header limit = response.getHeaders("X-RateLimit-Limit")[0];
        Header remaining = response.getHeaders("X-RateLimit-Remaining")[0];
        String[] splited = limit.getValue().replaceAll("name=", "").split(",");
        String tariff = splited[0];
        Integer limitNum = Integer.valueOf(limit.getValue().split(",")[1].replaceAll(";", ""));
        Integer remainingNum = Integer.valueOf(remaining.getValue().split(",")[1].replaceAll(";", ""));
        return new Quote(tariff, limitNum, remainingNum);
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
