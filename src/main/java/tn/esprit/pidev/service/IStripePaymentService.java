package tn.esprit.pidev.service;

import tn.esprit.pidev.dto.PaymentInitRequest;
import tn.esprit.pidev.dto.PaymentInitResponse;
import tn.esprit.pidev.dto.SubscriptionResponse;

public interface IStripePaymentService {
    PaymentInitResponse initiatePayment(PaymentInitRequest request);
    SubscriptionResponse confirmPayment(String sessionId);
}