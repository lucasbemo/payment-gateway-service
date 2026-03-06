package com.payment.gateway.infrastructure.commons.monitoring;

import io.micrometer.tracing.Tracer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Distributed tracing configuration.
 * Configures Micrometer Tracing with Brave/Zipkin bridge.
 */
@Configuration
public class TracingConfig {

    /**
     * Aspect for payment service methods.
     */
    @Aspect
    static class PaymentTracingAspect {
        private final Tracer tracer;

        PaymentTracingAspect(Tracer tracer) {
            this.tracer = tracer;
        }

        @Around("execution(* com.payment.gateway.application.payment.service..*.*(..))")
        public Object tracePaymentMethods(ProceedingJoinPoint joinPoint) throws Throwable {
            String methodName = joinPoint.getSignature().getName();
            var span = tracer.nextSpan().name("payment." + methodName);
            span.start();
            try (var scope = tracer.withSpan(span)) {
                return joinPoint.proceed();
            } catch (Throwable e) {
                span.error(e);
                throw e;
            } finally {
                span.end();
            }
        }
    }

    /**
     * Aspect for refund service methods.
     */
    @Aspect
    static class RefundTracingAspect {
        private final Tracer tracer;

        RefundTracingAspect(Tracer tracer) {
            this.tracer = tracer;
        }

        @Around("execution(* com.payment.gateway.application.refund.service..*.*(..))")
        public Object traceRefundMethods(ProceedingJoinPoint joinPoint) throws Throwable {
            String methodName = joinPoint.getSignature().getName();
            var span = tracer.nextSpan().name("refund." + methodName);
            span.start();
            try (var scope = tracer.withSpan(span)) {
                return joinPoint.proceed();
            } catch (Throwable e) {
                span.error(e);
                throw e;
            } finally {
                span.end();
            }
        }
    }

    /**
     * Aspect for transaction service methods.
     */
    @Aspect
    static class TransactionTracingAspect {
        private final Tracer tracer;

        TransactionTracingAspect(Tracer tracer) {
            this.tracer = tracer;
        }

        @Around("execution(* com.payment.gateway.application.transaction.service..*.*(..))")
        public Object traceTransactionMethods(ProceedingJoinPoint joinPoint) throws Throwable {
            String methodName = joinPoint.getSignature().getName();
            var span = tracer.nextSpan().name("transaction." + methodName);
            span.start();
            try (var scope = tracer.withSpan(span)) {
                return joinPoint.proceed();
            } catch (Throwable e) {
                span.error(e);
                throw e;
            } finally {
                span.end();
            }
        }
    }

    @Bean
    public PaymentTracingAspect paymentTracingAspect(Tracer tracer) {
        return new PaymentTracingAspect(tracer);
    }

    @Bean
    public RefundTracingAspect refundTracingAspect(Tracer tracer) {
        return new RefundTracingAspect(tracer);
    }

    @Bean
    public TransactionTracingAspect transactionTracingAspect(Tracer tracer) {
        return new TransactionTracingAspect(tracer);
    }
}
