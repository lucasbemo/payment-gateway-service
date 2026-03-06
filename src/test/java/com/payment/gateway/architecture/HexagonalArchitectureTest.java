package com.payment.gateway.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@DisplayName("Hexagonal Architecture Tests")
class HexagonalArchitectureTest {

    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.payment.gateway");

    @Test
    @DisplayName("Cyclic dependencies should not exist between main packages")
    void noCyclicDependenciesBetweenPackages() {
        slices().matching("com.payment.gateway.(*)..")
                .should().beFreeOfCycles()
                .check(classes);
    }

    @Nested
    @DisplayName("Domain Layer Tests")
    class DomainLayerTests {

        @Test
        @DisplayName("Domain layer should not depend on Application or Infrastructure layers")
        void domainLayerShouldNotDependOnApplicationOrInfrastructure() {
            noClasses()
                    .that().resideInAPackage("com.payment.gateway.domain..")
                    .should().dependOnClassesThat().resideInAPackage("com.payment.gateway.application..")
                    .check(classes);

            noClasses()
                    .that().resideInAPackage("com.payment.gateway.domain..")
                    .should().dependOnClassesThat().resideInAPackage("com.payment.gateway.infrastructure..")
                    .check(classes);
        }
    }

    @Nested
    @DisplayName("Application Layer Tests")
    class ApplicationLayerTests {

        @Test
        @DisplayName("Application layer should only depend on Domain layer")
        void applicationLayerShouldOnlyDependOnDomain() {
            noClasses()
                    .that().resideInAPackage("com.payment.gateway.application..")
                    .should().dependOnClassesThat().resideInAPackage("com.payment.gateway.infrastructure..")
                    .check(classes);
        }
    }

    @Nested
    @DisplayName("Package Structure Tests")
    class PackageStructureTests {

        @Test
        @DisplayName("Bounded contexts should be properly isolated")
        void boundedContextsShouldBeProperlyIsolated() {
            slices().matching("com.payment.gateway.domain.(payment|merchant|transaction|refund|customer|idempotency|reconciliation|outbox).(*)..")
                    .should().beFreeOfCycles()
                    .check(classes);
        }
    }
}
