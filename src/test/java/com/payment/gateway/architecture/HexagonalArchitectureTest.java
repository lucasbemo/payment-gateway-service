package com.payment.gateway.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

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
}
