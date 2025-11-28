package org.me.gcu.scott_euan_s2132201;

public class CurrencyRate {
    private String code;       // "USD"
    private String name;       // "Pound Sterling / US Dollar"
    private double rateToGbp;  // 1 GBP = 1.2345 USD

    public CurrencyRate(String code, String name, double rateToGbp) {
        this.code = code;
        this.name = name;
        this.rateToGbp = rateToGbp;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public double getRateToGbp() {
        return rateToGbp;
    }
}
