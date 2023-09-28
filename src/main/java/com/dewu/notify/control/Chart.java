package com.dewu.notify.control;

import com.dewu.notify.util.OrderDataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class Chart {

    private static final int BEFORE_NOW = -6;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping("/chart")
    public String chart(Model model) {
        return "chart";
    }

    @RequestMapping("/sales")
    @ResponseBody
    public Map<String, List> salesAmount() {

        List<String> dates = new ArrayList<>();
        List<Double> orderAmounts = new ArrayList<>();
        List<Double> payAmounts = new ArrayList<>();

        for (int i = BEFORE_NOW; i <= 0; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            String dateString = OrderDataUtil.DF.format(date);
            dates.add(dateString);

            String orderKey = OrderDataUtil.buildOrderKey(dateString);
            Double orderAmount = (Double)redisTemplate.opsForValue().get(orderKey);

            if (orderAmount == null) {
                orderAmount = 0d;
            }

            orderAmounts.add(orderAmount);

            String payKey = OrderDataUtil.buildPayKey(dateString);
            Double payAmount = (Double)redisTemplate.opsForValue().get(payKey);

            if (payAmount == null) {
                payAmount = 0d;
            }

            payAmounts.add(payAmount);
        }

        Map<String, List> result = new HashMap();
        result.put("dates", dates);
        result.put("orderAmounts", orderAmounts);
        result.put("payAmounts", payAmounts);

        return result;
    }
}
