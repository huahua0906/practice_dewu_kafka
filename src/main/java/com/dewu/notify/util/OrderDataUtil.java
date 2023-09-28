package com.dewu.notify.util;

import java.time.format.DateTimeFormatter;

public class OrderDataUtil {
    /**
     * 订单前缀
     */
    public static final String ORDER_PREFIX = "order-";

    /**
     * 支付前缀
     */
    public static final String PAY_PREFIX = "pay-";

    /**
     * 日期格式
     */
    public static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 订单数据 key
     *
     * @param key
     * @return
     */
    public static String buildOrderKey(String key) {
        return ORDER_PREFIX + key;
    }

    /**
     * 支付数据 Key
     *
     * @param key
     * @return
     */
    public static String buildPayKey(String key) {
        return PAY_PREFIX + key;
    }
}
