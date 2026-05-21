# Remediation Roadmap — cucumber-restapi (BDD REST API Testing Library)

This roadmap prioritizes gaps from the [Gap Analysis](GAP_ANALYSIS.md) into three phases:
- **Phase 1 — Quick Wins**: High severity / low effort items that deliver immediate value
- **Phase 2 — Important**: High severity / medium effort items that require more work but are essential
- **Phase 3 — Polish**: Lower severity improvements for long-term maintainability

---

## Phase 1 — Quick Wins (1–2 weeks)

### 1.1 Add SLF4J Logging to Core Classes
**Gap Ref:** 6.1 | **Severity:** High | **Effort:** Small

Add structured logging for HTTP requests, responses, and assertion outcomes to aid debugging.

**Devin Prompt:**
> Add SLF4J logging to `AbstractBddStepDefinition` and `DefaultRestApiBddStepDefinition` in the `uc-bdd-test-generation-rest-api` repo. Add a private `Logger` field using `LoggerFactory.getLogger()`. Log at DEBUG level: the HTTP method and URI before each request in `request()`, the response status code and body length after each response, and the JsonPath query and result in `getJsonPathValue()`. Log at WARN level when `checkJsonPathDoesntExist()` encounters a null body. Do not add logging to assertion pass/fail — let AssertJ handle that. Ensure tests still pass with `mvn test -Dgpg.skip=true`.

---

### 1.2 Configure HTTP Request Timeouts
**Gap Ref:** 7.1 | **Severity:** High | **Effort:** Small

Prevent tests from hanging indefinitely by setting default connection and read timeouts.

**Devin Prompt:**
> In `AbstractBddStepDefinition` in the `uc-bdd-test-generation-rest-api` repo, configure the `HttpComponentsClientHttpRequestFactory` in the constructor with a default connect timeout of 10 seconds and read timeout of 30 seconds. Make both values configurable via Spring `@Value` properties: `redfroggy.cucumber.restapi.timeout.connect` (default 10000) and `redfroggy.cucumber.restapi.timeout.read` (default 30000). Update the existing Cucumber feature tests to ensure they still pass. Add a comment explaining the timeout defaults.

---

### 1.3 Make WireMock ConsoleNotifier Configurable
**Gap Ref:** 6.3 | **Severity:** Medium | **Effort:** Small

Allow consumers to disable verbose WireMock logging.

**Devin Prompt:**
> In `AbstractBddStepDefinition` in the `uc-bdd-test-generation-rest-api` repo, change the hardcoded `new ConsoleNotifier(true)` to use a Spring `@Value` property `redfroggy.cucumber.restapi.wiremock.verbose` (default: `false`). Pass this boolean to `ConsoleNotifier`. Ensure all existing tests pass.

---

### 1.4 Add Cucumber Reporting Plugins
**Gap Ref:** 6.2 | **Severity:** Medium | **Effort:** Small

Generate HTML and JSON test reports for better test result visibility.

**Devin Prompt:**
> In `RestApiCucumberTest.java` in the `uc-bdd-test-generation-rest-api` repo, update the `@CucumberOptions` annotation to add JSON and HTML reporting plugins: `plugin = {"pretty", "json:target/cucumber-reports/cucumber.json", "html:target/cucumber-reports/cucumber.html"}`. Verify the reports are generated after running `mvn test -Dgpg.skip=true`. Add `target/cucumber-reports/` to `.gitignore`.

---

### 1.5 Fix Duplicate Jacoco Version Property
**Gap Ref:** 1.5 | **Severity:** Low | **Effort:** Small

Remove the duplicate property definition in `pom.xml`.

**Devin Prompt:**
> In `pom.xml` of the `uc-bdd-test-generation-rest-api` repo, remove the duplicate `<jacoco-maven-plugin.version>0.8.6</jacoco-maven-plugin.version>` on line 50 (keep the `0.8.7` version on line 52). Run `mvn test -Dgpg.skip=true` to confirm no breakage.

---

### 1.6 Add Dependency Vulnerability Scanning
**Gap Ref:** 4.4 | **Severity:** Medium | **Effort:** Small

Add OWASP Dependency Check to the Maven build to catch known CVEs.

**Devin Prompt:**
> In `pom.xml` of the `uc-bdd-test-generation-rest-api` repo, add the `org.owasp:dependency-check-maven` plugin (latest stable version) in the `<build><plugins>` section. Configure it to run during the `verify` phase with `<failBuildOnCVSS>7</failBuildOnCVSS>` to fail the build on high-severity CVEs. Add a suppressions XML file at `dependency-check-suppressions.xml` if needed. Run `mvn verify -Dgpg.skip=true -DskipTests` to validate. Add a comment in pom.xml explaining the purpose.

---

### 1.7 Add WireMock Verification Steps
**Gap Ref:** 7.4 | **Severity:** Medium | **Effort:** Small

Allow consumers to verify that mocked endpoints were actually called.

**Devin Prompt:**
> In `DefaultRestApiBddStepDefinition` in the `uc-bdd-test-generation-rest-api` repo, add two new Gherkin step definitions: (1) `@Then("^the mocked (.*) (.*) should have been called$")` — uses `WireMock.verify(getRequestedFor(urlPathMatching(resource)))` to confirm a mock was hit, and (2) `@Then("^the mocked (.*) (.*) should have been called (\\d+) times$")` — verifies exact call count. Expose the `wireMockServer` field (or make it accessible) in `AbstractBddStepDefinition` so verification methods work. Add test scenarios in `users.feature` to cover the new steps. Ensure 100% coverage is maintained.

---

## Phase 2 — Important (2–4 weeks)

### 2.1 Upgrade to Spring Boot 3.x and Jakarta EE
**Gap Ref:** 1.1, 4.1 | **Severity:** Critical | **Effort:** Medium

Migrate from Spring Boot 2.5.3 to Spring Boot 3.x to resolve EOL status and known CVEs.

**Devin Prompt:**
> Upgrade the `uc-bdd-test-generation-rest-api` repo from Spring Boot 2.5.3 to the latest Spring Boot 3.2.x. This requires: (1) Update the `<parent>` version in `pom.xml`. (2) Replace all `javax.*` imports with `jakarta.*` (specifically `javax.annotation.PostConstruct` → `jakarta.annotation.PostConstruct`, `javax.annotation.PreDestroy` → `jakarta.annotation.PreDestroy`, `javax.servlet` → `jakarta.servlet`). (3) Update Cucumber to version 7.x for Jakarta compatibility. (4) Update WireMock to version 3.x. (5) Update the Surefire plugin and Jacoco plugin to latest compatible versions. (6) Ensure the Java source/target is set to 17. (7) Run `mvn test -Dgpg.skip=true` and fix any compilation or test failures. Maintain 100% code coverage.

---

### 2.2 Refactor ScenarioScope to Non-Static Instance
**Gap Ref:** 1.2, 7.5 | **Severity:** High | **Effort:** Medium

Replace the static `ScenarioScope` with a Spring-managed bean to enable proper lifecycle management.

**Devin Prompt:**
> In the `uc-bdd-test-generation-rest-api` repo, refactor `ScenarioScope` from a static singleton to a Cucumber-scoped Spring bean. (1) Annotate `ScenarioScope` with `@Component` and `@io.cucumber.spring.ScenarioScope` (Cucumber's scenario-scoped bean annotation). (2) In `AbstractBddStepDefinition`, replace the `static final ScenarioScope` field with a constructor-injected `ScenarioScope` bean. (3) Update `DefaultRestApiBddStepDefinition` constructor to pass it through. (4) Update the test `DefaultRestApiStepDefinitionTest` if needed. (5) Run all Cucumber tests and ensure they pass. This change enables parallel scenario execution in the future and eliminates state leakage between scenarios.

---

### 2.3 Add Custom Exception Types
**Gap Ref:** 2.1, 2.5 | **Severity:** High | **Effort:** Medium

Create library-specific exceptions for better error diagnosis.

**Devin Prompt:**
> In the `uc-bdd-test-generation-rest-api` repo, create a new package `fr.redfroggy.bdd.restapi.exception` with three exception classes: (1) `BddStepException extends RuntimeException` — base exception with constructor accepting message and cause. (2) `BddJsonPathException extends BddStepException` — thrown when JsonPath evaluation fails, includes the JsonPath expression and response body snippet. (3) `BddScopeException extends BddStepException` — thrown when a scenario scope variable is not found. Wrap the raw errors in `AbstractBddStepDefinition.getJsonPathValue()`, `checkJsonPath()`, `replaceDynamicParameters()`, and `checkScenarioVariable()` with these custom exceptions. Add descriptive messages including context (e.g., "JsonPath '$.name' not found in response body"). Add unit tests for each exception class. Maintain 100% coverage.

---

### 2.4 Add Descriptive AssertJ Error Messages
**Gap Ref:** 2.4 | **Severity:** Medium | **Effort:** Medium

Improve assertion failure output with context about what was being tested.

**Devin Prompt:**
> In `AbstractBddStepDefinition` in the `uc-bdd-test-generation-rest-api` repo, add `.as()` / `.describedAs()` messages to all AssertJ assertions. For example: `assertThat(responseEntity.getStatusCodeValue()).as("Expected HTTP status %s but got %s for %s %s", sanitizedStatus, responseEntity.getStatusCodeValue(), method, resource).isEqualTo(sanitizedStatus)`. Cover at minimum: `checkStatus()`, `checkHeaderEqual()`, `checkHeaderExists()`, `checkJsonPath()`, `checkJsonPathIsArray()`, `checkBodyContains()`, and `checkScenarioVariable()`. Ensure all tests still pass.

---

### 2.5 Fix Silent Null Handling in checkJsonPathDoesntExist
**Gap Ref:** 2.2 | **Severity:** High | **Effort:** Small

Correct the assertion logic that silently passes on null bodies.

**Devin Prompt:**
> In `AbstractBddStepDefinition.checkJsonPathDoesntExist()` in the `uc-bdd-test-generation-rest-api` repo, the current implementation only checks `assertThat(jsonPath).isNotEmpty()` when `ctx != null`, but does nothing when ctx is null. Fix this by: if the body document is null, the path trivially doesn't exist — this is correct. But when the body IS present, actually verify the JsonPath does NOT resolve to a value (currently it only checks the jsonPath string is non-empty, never that the path is absent). Use a try/catch on `ctx.read(jsonPath)` — if it throws `PathNotFoundException`, the assertion passes; if it returns a value, fail with a descriptive message. Add a negative test scenario in `users.feature` to validate this behavior. Maintain 100% coverage.

---

### 2.6 Replace ReflectionTestUtils.invokeMethod with Direct Type Handling
**Gap Ref:** 2.3 | **Severity:** Medium | **Effort:** Medium

Remove the fragile reflection-based value comparison.

**Devin Prompt:**
> In `AbstractBddStepDefinition.checkJsonPath()` in the `uc-bdd-test-generation-rest-api` repo, replace the `ReflectionTestUtils.invokeMethod(currentValue, "valueOf", sanitizedExpectedValue)` call with explicit type handling. Check if `currentValue` is an instance of `Integer`, `Long`, `Double`, `Boolean`, or `String`, and parse `sanitizedExpectedValue` accordingly using `Integer.valueOf()`, `Long.valueOf()`, etc. Add a fallback to `String.valueOf()` for unknown types. This eliminates the dependency on `ReflectionTestUtils` for production code and provides clear error messages when type conversion fails. Add test cases for each type. Maintain 100% coverage.

---

### 2.7 Add Unit Tests for AbstractBddStepDefinition
**Gap Ref:** 3.1 | **Severity:** High | **Effort:** Large

Create direct unit tests for the core class that don't depend on Cucumber.

**Devin Prompt:**
> In the `uc-bdd-test-generation-rest-api` repo, create `src/test/java/fr/redfroggy/bdd/restapi/glue/AbstractBddStepDefinitionTest.java`. Write unit tests using MockMvc or a mock TestRestTemplate to directly test the following methods without going through Cucumber: (1) `replaceDynamicParameters()` — test variable substitution, nested variables, missing variable error. (2) `setBody()` / `setBodyPathWithValue()` — test valid JSON, invalid JSON, JsonPath modification. (3) `checkStatus()` — test matching and non-matching codes. (4) `checkHeaderEqual()` / `checkHeaderExists()` — test present/absent/wrong headers. (5) `checkJsonPath()` — test scalar values, collections, null values, soft vs hard mode. (6) `mockThirdPartyApiCall()` — test URL with and without query params. (7) `getFilenameFromPath()` — test path extraction. Aim for at least 90% coverage of `AbstractBddStepDefinition`. Maintain overall 100% Jacoco coverage.

---

### 2.8 Add JSON Schema Validation Step
**Gap Ref:** 5.4 | **Severity:** Medium | **Effort:** Medium

Allow consumers to validate response bodies against JSON Schema documents.

**Devin Prompt:**
> In the `uc-bdd-test-generation-rest-api` repo, add a new Gherkin step: `@Then("^http response body should match json schema (.*)$")` that validates the response body against a JSON Schema file loaded from the classpath. Add the `org.everit.json.schema:json-schema` (or `com.networknt:json-schema-validator`) dependency to `pom.xml` with `provided` scope so consumers opt in. Implement the validation in `AbstractBddStepDefinition`. Create a test JSON schema file under `src/test/resources/schemas/user-schema.json` and add a test scenario in `users.feature`. Maintain 100% coverage.

---

## Phase 3 — Polish (4–8 weeks)

### 3.1 Migrate Step Patterns from Regex to Cucumber Expressions
**Gap Ref:** 5.5 | **Severity:** Medium | **Effort:** Large

Modernize step definitions to use Cucumber Expressions for better readability.

**Devin Prompt:**
> In the `uc-bdd-test-generation-rest-api` repo, migrate all step definitions in `DefaultRestApiBddStepDefinition` from regex patterns (e.g., `@When("^I GET (.*)$")`) to Cucumber Expressions (e.g., `@When("I GET {string}")`). This requires Cucumber 7.x. Update all corresponding `.feature` files in `src/test/resources/features/` to match the new syntax. Keep backward compatibility by documenting the migration in README.md. Run all tests to verify.

---

### 3.2 Add JUnit 5 Support
**Gap Ref:** 3.5 | **Severity:** Medium | **Effort:** Medium

Support JUnit 5 (Jupiter) as the test runner alongside JUnit 4.

**Devin Prompt:**
> In the `uc-bdd-test-generation-rest-api` repo, add JUnit 5 support. (1) Add `cucumber-junit-platform-engine` dependency alongside the existing `cucumber-junit` dependency. (2) Update `maven-surefire-plugin` to use the JUnit Platform provider. (3) Create a `src/test/java/fr/redfroggy/bdd/restapi/RestApiCucumberJUnit5Test.java` that uses `@Suite` and `@SelectClasspathResource("features")` from JUnit Platform Suite. (4) Update README.md with both JUnit 4 and JUnit 5 usage examples. (5) Keep JUnit 4 `RestApiCucumberTest` for backward compatibility. Run all tests to verify.

---

### 3.3 Add Exact JSON Body Assertion Step
**Gap Ref:** 5.2 | **Severity:** Medium | **Effort:** Medium

Allow comparing the entire response body against an expected JSON structure.

**Devin Prompt:**
> In the `uc-bdd-test-generation-rest-api` repo, add two new Gherkin steps: (1) `@Then("^http response body should be (.*)$")` — compares the entire response body against an inline JSON string (using JSONAssert strict mode). (2) `@Then("^http response body should match file (.*)$")` — compares against a JSON file from the classpath. Add the `org.skyscreamer:jsonassert` dependency. Implement in `AbstractBddStepDefinition`. Add test scenarios. Maintain 100% coverage.

---

### 3.4 Add Request/Response Debug Logging Step
**Gap Ref:** 6.4 | **Severity:** Medium | **Effort:** Medium

Allow consumers to opt into detailed request/response logging for debugging.

**Devin Prompt:**
> In the `uc-bdd-test-generation-rest-api` repo, add a new Gherkin step: `@Given("^I enable http request/response logging$")` that sets a flag in the step definition. When enabled, `request()` in `AbstractBddStepDefinition` should log (at INFO level via SLF4J) the full request (method, URI, headers, body) before sending and the full response (status, headers, body) after receiving. Add a corresponding `@Given("^I disable http request/response logging$")` step. Add test scenarios. Maintain 100% coverage.

---

### 3.5 Clarify and Document "should" vs "must" Semantics
**Gap Ref:** 5.1 | **Severity:** High | **Effort:** Medium

Make the soft/hard assertion distinction clear and document it thoroughly.

**Devin Prompt:**
> In the `uc-bdd-test-generation-rest-api` repo, improve the documentation and naming around "should" vs "must" assertion semantics. (1) Add a section to README.md titled "Assertion Modes" explaining: "should be" = soft assertion (passes if path is null/missing, fails only if present with wrong value), "must be" = hard assertion (fails if path is null/missing OR has wrong value). (2) Add Javadoc comments to `bodyPathShouldBeEqualTo()`, `bodyPathMustBeEqualTo()`, and their "not" variants explaining the behavior. (3) Add example scenarios in README.md demonstrating when to use each mode.

---

### 3.6 Fix Raw Types and Remove Unchecked Suppression
**Gap Ref:** 1.3, 1.4 | **Severity:** Medium | **Effort:** Small

Add proper generics and remove the class-level `@SuppressWarnings`.

**Devin Prompt:**
> In `AbstractBddStepDefinition` in the `uc-bdd-test-generation-rest-api` repo, (1) Remove `@SuppressWarnings("unchecked")` from the class declaration. (2) Add proper generic type parameters to all `Collection` usages — change `Collection` to `Collection<?>` or the appropriate specific type. (3) Fix the unchecked casts in `checkJsonCollection()`, `checkJsonPath()`, and `checkScenarioVariable()`. (4) Add targeted `@SuppressWarnings("unchecked")` only on the specific lines where casts are unavoidable (e.g., JsonPath returns `Object`). Run tests to verify.

---

### 3.7 Migrate CI from Travis CI to GitHub Actions
**Gap Ref:** — (Infrastructure improvement) | **Severity:** Low | **Effort:** Medium

Travis CI is no longer free for open-source. Migrate to GitHub Actions.

**Devin Prompt:**
> In the `uc-bdd-test-generation-rest-api` repo, create a GitHub Actions workflow at `.github/workflows/ci.yml` that replicates the Travis CI pipeline. Steps: (1) Trigger on push to master and pull requests. (2) Set up JDK 17 using `actions/setup-java`. (3) Cache Maven dependencies with `actions/cache`. (4) Run `mvn package -Dgpg.skip=true`. (5) Upload Jacoco coverage report to Codecov using `codecov/codecov-action`. (6) On master branch merges, run semantic-release. Keep `.travis.yml` with a deprecation comment until the migration is validated. Add a note to README.md about the CI migration.

---

### 3.8 Add Automatic WireMock Port Selection
**Gap Ref:** 7.2 | **Severity:** Medium | **Effort:** Small

Eliminate port collision issues by dynamically selecting an available port.

**Devin Prompt:**
> In `AbstractBddStepDefinition` in the `uc-bdd-test-generation-rest-api` repo, when the `redfroggy.cucumber.restapi.wiremock.port` property is set to `0` (or not set), configure WireMock to use `WireMockConfiguration.wireMockConfig().dynamicPort()` instead of a fixed port. After `wireMockServer.start()`, store the actual port via `wireMockServer.port()` and make it available to consumers. Update the default to `0` (dynamic) and document the change. Ensure all tests pass.

---

## Summary

| Phase | Items | Estimated Time | Key Outcomes |
|---|---|---|---|
| **Phase 1** | 7 items | 1–2 weeks | Logging, timeouts, vulnerability scanning, reporting, quick fixes |
| **Phase 2** | 8 items | 2–4 weeks | Spring Boot 3.x upgrade, proper error handling, unit tests, scope refactor |
| **Phase 3** | 8 items | 4–8 weeks | Modern Cucumber syntax, JUnit 5, CI migration, API polish |
| **Total** | **23 items** | **7–14 weeks** | Production-grade library with modern stack and comprehensive testing |
