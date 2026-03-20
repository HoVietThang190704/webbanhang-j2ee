package com.example.demo.service;

import com.mservice.config.Environment;
import com.mservice.config.MoMoEndpoint;
import com.mservice.config.PartnerInfo;
import com.mservice.enums.RequestType;
import com.mservice.models.PaymentResponse;
import com.mservice.processor.CreateOrderMoMo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@PropertySource("classpath:momo.properties")
public class MoMoPaymentService {

    @Value("${momo.endpoint}")
    private String endpoint;

    @Value("${momo.partnerCode}")
    private String partnerCode;

    @Value("${momo.accessKey}")
    private String accessKey;

    @Value("${momo.secretKey}")
    private String secretKey;

    @Value("${momo.returnUrl}")
    private String returnUrl;

    @Value("${momo.notifyUrl}")
    private String notifyUrl;

    public String createPaymentUrl(String orderId, long amount, String orderInfo) {
        try {
            // Initialize MoMo Logger
            com.mservice.shared.utils.LogUtils.init();

            // Setup MoMo Environment
            MoMoEndpoint momoEndpoint = new MoMoEndpoint(endpoint, "/create", "/refund", "/query", "/confirm",
                    "/tokenPay", "/tokenBind", "/tokenInquiry", "/tokenDelete");
            PartnerInfo partnerInfo = new PartnerInfo(partnerCode, accessKey, secretKey);
            Environment environment = new Environment(momoEndpoint, partnerInfo, "dev");

            String requestId = UUID.randomUUID().toString();

            // Calling MoMo library to create checkout order
            PaymentResponse response = CreateOrderMoMo.process(
                    environment,
                    orderId,
                    requestId,
                    String.valueOf(amount),
                    orderInfo,
                    returnUrl,
                    notifyUrl,
                    "",
                    RequestType.CAPTURE_WALLET,
                    Boolean.TRUE);

            if (response != null) {
                System.out.println("MoMo API Response Code: " + response.getResultCode());
                System.out.println("MoMo API Response Message: " + response.getMessage());
                System.out.println("MoMo API PayUrl: " + response.getPayUrl());

                if (response.getPayUrl() != null && !response.getPayUrl().isEmpty()) {
                    return response.getPayUrl();
                }
            } else {
                System.out.println("MoMo API returned NULL response!");
            }
        } catch (Exception e) {
            System.out.println("Exception inside createPaymentUrl:");
            e.printStackTrace();
        }
        return null;
    }
}
