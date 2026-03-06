package com.payment.gateway.infrastructure.transaction.adapter.out.provider;

import com.payment.gateway.application.transaction.port.out.ExternalTransactionProviderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class StubExternalTransactionProvider implements ExternalTransactionProviderPort {

    @Override
    public CaptureResult capture(CaptureRequest request) {
        log.info("StubExternalTransactionProvider.capture: transactionId={}, amount={}, currency={}",
                request.transactionId(), request.amount(), request.currency());
        return new CaptureResult(true, "gtw-" + UUID.randomUUID().toString().substring(0, 8), null, null);
    }
}
