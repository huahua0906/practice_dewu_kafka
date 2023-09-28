package com.dewu.notify.service;

import com.alibaba.fastjson.JSON;
import com.dewu.notify.util.OrderDataUtil;
import com.huahua.dewu.model.Order;
import com.huahua.dewu.model.PaymentRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Component
public class OrderConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(OrderConsumer.class);

    private static final String LOCK = "lock-";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @KafkaListener(topics = {"dewuOrder"})
    public void listenOrder(ConsumerRecord<?, ?> record) {
        // 消息转成 Optional 实例对象
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isEmpty()) {
            // 消息不存在，则返回
            return;
        }

        String message = (String)kafkaMessage.get();
        Order order = JSON.parseObject(message, Order.class);

        // 把订单日期，转换为年月日字符串
        Date date = order.getGmtCreated();
        LocalDate localDate = dateToLocalDate(date);
        String dateString = OrderDataUtil.DF.format(localDate);

        RLock likeLock = redissonClient.getLock(buildOrderLockName(dateString));

        //获取锁
        likeLock.lock();

        try {
            String key = OrderDataUtil.buildOrderKey(dateString);
            Double amount = (Double)redisTemplate.opsForValue().get(key);

            if (amount == null) {
                amount = 0d;
            }

            amount += order.getTotalPrice();

            redisTemplate.opsForValue().set(key, amount);

        } catch (Exception e) {
            LOG.error("compute total price error.", e);
        } finally {
            likeLock.unlock();
        }
    }

    @KafkaListener(topics = {"dewuPayment"})
    public void listenPay(ConsumerRecord<?, ?> record) {
        // 消息转成 Optional 实例对象
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isEmpty()) {
            // 消息不存在，则返回
            return;
        }

        String message = (String)kafkaMessage.get();
        PaymentRecord pay = JSON.parseObject(message, PaymentRecord.class);

        // 把支付日期，转换为年月日字符串
        Date date = pay.getGmtCreated();
        LocalDate localDate = dateToLocalDate(date);
        String dateString = OrderDataUtil.DF.format(localDate);

        RLock likeLock = redissonClient.getLock(buildPayLockName(dateString));

        //获取锁
        likeLock.lock();

        try {
            String key = OrderDataUtil.buildPayKey(dateString);
            Double amount = (Double)redisTemplate.opsForValue().get(key);

            if (amount == null) {
                amount = 0d;
            }

            amount += pay.getAmount();

            redisTemplate.opsForValue().set(key, amount);

        } catch (Exception e) {
            LOG.error("compute total price error.", e);
        } finally {
            likeLock.unlock();
        }
    }

    // Date 类型的日期，转换为 LocalDate 类型
    private static LocalDate dateToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static String buildOrderLockName(String key) {
        return LOCK + OrderDataUtil.ORDER_PREFIX + key;
    }

    private String buildPayLockName(String key) {
        return LOCK + OrderDataUtil.PAY_PREFIX + key;
    }

}
