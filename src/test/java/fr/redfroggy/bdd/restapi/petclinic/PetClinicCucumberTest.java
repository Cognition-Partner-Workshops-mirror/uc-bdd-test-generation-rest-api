package fr.redfroggy.bdd.restapi.petclinic;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Cucumber test runner for auto-generated PetClinic feature files.
 * Features are generated from the PetClinic OpenAPI spec by
 * scripts/generate-petclinic-features.js (48 scenarios across 8 files).
 *
 * Prerequisites:
 *   1. Start PetClinic: cd ts-java-spring-petclinic-rest-api && ./mvnw spring-boot:run
 *   2. Run these tests: mvn test -Dtest=PetClinicCucumberTest -Dgpg.skip=true
 *
 * The tests reuse the BDD library step definitions (GET, POST, PUT, DELETE,
 * response code, body path assertions) from DefaultRestApiBddStepDefinition.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"pretty", "html:target/petclinic-cucumber-report.html"},
        features = "src/test/resources/features/petclinic",
        glue = {"fr.redfroggy.bdd.restapi.glue"})
public final class PetClinicCucumberTest {
}
