package com.payment.gateway.infrastructure.commons.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect for audit logging of controller method invocations.
 * Logs entry, exit, and execution time for all REST controller operations.
 */
@Slf4j
@Aspect
@Component
public class AuditLogAspect {

    @Around("execution(* com.payment.gateway.infrastructure..adapter.in.rest..*Controller.*(..))")
    public Object auditControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("AUDIT [ENTER] {}.{}", className, methodName);
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.info("AUDIT [EXIT] {}.{} completed in {}ms", className, methodName, duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("AUDIT [ERROR] {}.{} failed after {}ms: {}", className, methodName, duration, e.getMessage());
            throw e;
        }
    }
}
