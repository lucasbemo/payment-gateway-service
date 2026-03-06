package com.payment.gateway.infrastructure.refund.adapter.out.kafka;

import com.payment.gateway.domain.refund.model.Refund;
import com.payment.gateway.domain.refund.port.RefundEventPublisherPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StubRefundEventPublisher implements RefundEventPublisherPort {

    @Override
    public void publishRefundCreated(Refund refund) {
        log.info("Stub: Refund created event for {}", refund.getId());
    }

    @Override
    public void publishRefundCompleted(Refund refund) {
        log.info("Stub: Refund completed event for {}", refund.getId());
    }

    @Override
    public void publishRefundFailed(Refund refund) {
        log.info("Stub: Refund failed event for {}", refund.getId());
    }
}
