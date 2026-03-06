package com.payment.gateway.infrastructure.commons.monitoring;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect that creates observation spans for service method calls.
 */
@Slf4j
@Aspect
@Component("serviceObservationAspect")
@RequiredArgsConstructor
public class SpanAspect {

    private final ObservationRegistry observationRegistry;

    @Around("execution(* com.payment.gateway.application..service..*(..))")
    public Object observeServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String observationName = "service." + className + "." + methodName;

        return Observation.createNotStarted(observationName, observationRegistry)
                .lowCardinalityKeyValue("class", className)
                .lowCardinalityKeyValue("method", methodName)
                .observe(() -> {
                    try {
                        return joinPoint.proceed();
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
