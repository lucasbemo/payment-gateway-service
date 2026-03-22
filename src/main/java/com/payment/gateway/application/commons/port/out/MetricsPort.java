package com.payment.gateway.application.commons.port.out;

public interface MetricsPort {
    
    void recordPaymentApproved();
    
    void recordPaymentFailed();
    
    void recordPaymentAmount(long amountCents);
    
    void recordRefundApproved();
    
    void recordRefundAmount(long amountCents);
}