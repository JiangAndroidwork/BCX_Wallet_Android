package com.cocos.bcx_sdk.bcx_utils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public final class Bitcoins implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final long SATOSHIS_PER_BITCOIN = 100000000L;
    private static final BigDecimal SATOSHIS_PER_BITCOIN_BD = BigDecimal.valueOf(SATOSHIS_PER_BITCOIN);
    public static final long MAX_VALUE = 21000000 * SATOSHIS_PER_BITCOIN;
    // public static final String BITCOIN_SYMBOL = "\u0243"; // Ƀ
    // public static final String BITCOIN_SYMBOL = "\u0E3F"; // ฿
    public static final String BITCOIN_SYMBOL = "BTC"; // BTC

    private final long satoshis;

    /*    *//**
     * if used properly, also valueOf(input) should be provided ideally,
     * BitcoinJ would already output Bitcoins instead of BigInteger
     *
     * @param output
     *           Object from BitcoiJ transaction
     * @return a value prepresentation of a Bitcoin domain object
     */
    /*
     * public static Bitcoins valueOf(TransactionOutput output) { return
     * Bitcoins.valueOf(output.getValue().longValue()); }
     */

    /**
     * @param btc double Value in full bitcoins. must be an exact represenatation
     * @return bitcoin value representation
     * @throws IllegalArgumentException if the given double value loses precision when converted to
     *                                  long
     */
    public static Bitcoins valueOf(double btc) {
        return valueOf(toLongExact(btc));
    }

    public static Bitcoins valueOf(String btc) {
        return Bitcoins.valueOf(new BigDecimal(btc).multiply(SATOSHIS_PER_BITCOIN_BD).longValueExact());
    }

    public static Bitcoins nearestValue(double v) {
        return new Bitcoins(Math.round(v * SATOSHIS_PER_BITCOIN));
    }

    public static Bitcoins nearestValue(BigDecimal bitcoinAmount) {
        BigDecimal satoshis = bitcoinAmount.multiply(SATOSHIS_PER_BITCOIN_BD);
        long satoshisExact = satoshis.setScale(0, RoundingMode.HALF_UP).longValueExact();
        return new Bitcoins(satoshisExact);
    }

    public static Bitcoins valueOf(long satoshis) {
        return new Bitcoins(satoshis);
    }

    private static long toLongExact(double origValue) {
        double satoshis = origValue * SATOSHIS_PER_BITCOIN; // possible loss of
        // precision here
        return Math.round(satoshis);
    }

    /**
     * XXX Jan: Commented out the below as this gives unnecessary runtime faults.
     * There may be rounding errors on the last decimals, and that is how life
     * is. The above simple conversion ois used instead.
     */

    // private static long toLongExact(double origValue) {
    // double satoshis = origValue * SATOSHIS_PER_BITCOIN; // possible loss of
    // // precision here?
    // long longSatoshis = Math.round(satoshis);
    // if (satoshis != (double) longSatoshis) {
    // double error = longSatoshis - satoshis;
    // throw new IllegalArgumentException("the given double value " + origValue
    // + " was not convertable to a precise value." + " error: " + error +
    // " satoshis");
    // }
    // return longSatoshis;
    // }
    private Bitcoins(long satoshis) {
        if (satoshis < 0)
            throw new IllegalArgumentException(String.format("Bitcoin values must be debt-free and positive, but was %s",
                    satoshis));
        if (satoshis >= MAX_VALUE)
            throw new IllegalArgumentException(String.format(
                    "Bitcoin values must be smaller than 21 Million BTC, but was %s", satoshis));
        this.satoshis = satoshis;
    }

    public BigDecimal multiply(BigDecimal pricePerBtc) {
        return toBigDecimal().multiply(BigDecimal.valueOf(satoshis));
    }

    protected Bitcoins parse(String input) {
        return Bitcoins.valueOf(input);
    }

    @Override
    public String toString() {
        // this could surely be implented faster without using BigDecimal. but it
        // is good enough for now.
        // this could be cached
        return toBigDecimal().toPlainString();
    }

    public String toString(int decimals) {
        // this could surely be implented faster without using BigDecimal. but it
        // is good enough for now.
        // this could be cached
        return toBigDecimal().setScale(decimals, RoundingMode.DOWN).toPlainString();
    }

    public BigDecimal toBigDecimal() {
        return BigDecimal.valueOf(satoshis).divide(SATOSHIS_PER_BITCOIN_BD);
    }

    @Override
    public int hashCode() {
        return (int) (satoshis ^ (satoshis >>> 32));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Bitcoins bitcoins = (Bitcoins) o;

        return satoshis == bitcoins.satoshis;
    }

    public BigInteger toBigInteger() {
        return BigInteger.valueOf(satoshis);
    }

    public long getLongValue() {
        return satoshis;
    }

    public String toCurrencyString() {
        return BITCOIN_SYMBOL + ' ' + toString();
    }

    public String toCurrencyString(int decimals) {
        return BITCOIN_SYMBOL + ' ' + toString(decimals);
    }

    public Bitcoins roundToSignificantFigures(int n) {
        return Bitcoins.valueOf(roundToSignificantFigures(satoshis, n));
    }

    private static long roundToSignificantFigures(long num, int n) {
        if (num == 0) {
            return 0;
        }
        // todo check if these are equal, take LongMath
        // int d = LongMath.log10(Math.abs(num), RoundingMode.CEILING);
        final double d = Math.ceil(Math.log10(num < 0 ? -num : num));
        final int power = n - (int) d;

        final double magnitude = Math.pow(10, power);
        final long shifted = Math.round(num * magnitude);
        return (long) (shifted / magnitude);
    }
}
