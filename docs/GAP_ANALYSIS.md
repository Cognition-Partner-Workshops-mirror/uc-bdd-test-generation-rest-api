# Gap Analysis — cucumber-restapi (BDD REST API Testing Library)

This analysis compares the current codebase against engineering best practices across seven categories. Each gap is rated by **Severity** (Critical / High / Medium / Low) and estimated **Effort** to remediate (Small / Medium / Large).

---

## 1. Code Organization

| # | Gap | Details | Severity | Effort |
|---|---|---|---|---|
| 1.1 | **Outdated Spring Boot version** | Spring Boot 2.5.3 reached end-of-life. `javax.*` namespace (e.g., `javax.annotation.PostConstruct`, `javax.servlet`) will not work on Spring Boot 3.x / Jakarta EE. | Critical | Medium |
| 1.2 | **Static mutable state in ScenarioScope** | `AbstractBddStepDefinition.scenarioScope` is a `static final` field shared across all step definition instances. This creates hidden coupling and prevents parallel scenario execution. | High | Medium |
| 1.3 | **`@SuppressWarnings("unchecked")` on entire class** | `AbstractBddStepDefinition` suppresses all unchecked warnings at the class level, masking potential type-safety issues across 500+ lines. | Medium | Small |
| 1.4 | **Raw types usage** | `Collection` is used without type parameters in `checkJsonCollection()`, `checkJsonPath()`, and `checkScenarioVariable()`. | Medium | Small |
| 1.5 | **Duplicate Jacoco version property** | `pom.xml` defines `jacoco-maven-plugin.version` twice (lines 50 and 52: `0.8.6` and `0.8.7`). The second value silently overrides the first. | Low | Small |
| 1.6 | **No module-info.java** | Library does not declare a Java module descriptor, limiting JPMS compatibility. | Low | Small |
| 1.7 | **Test code uses `Assert` from JUnit** | Test application (`UserController`, `ScenarioScopeTest`) mixes JUnit 4 `Assert` with AssertJ, creating inconsistent assertion style. | Low | Small |

---

## 2. Error Handling

| # | Gap | Details | Severity | Effort |
|---|---|---|---|---|
| 2.1 | **No custom exception types** | All failures are raw AssertJ assertion errors. No library-specific exceptions exist to help consumers diagnose step failures (e.g., missing scenario variable, invalid JsonPath, WireMock setup failure). | High | Medium |
| 2.2 | **Silent null handling in assertions** | `checkJsonPathDoesntExist()` only asserts the jsonPath is not empty when `ctx != null` but does nothing if the body is null — it silently passes when no body exists, which may hide bugs. | High | Small |
| 2.3 | **Unchecked `ReflectionTestUtils.invokeMethod()`** | `checkJsonPath()` calls `ReflectionTestUtils.invokeMethod(currentValue, "valueOf", ...)` which can throw opaque `IllegalArgumentException` or `NullPointerException` if the value type doesn't have a `valueOf` method. | Medium | Medium |
| 2.4 | **No error message customization** | AssertJ assertions lack custom failure messages (`as()` / `describedAs()`), making test output difficult to interpret for library consumers. | Medium | Medium |
| 2.5 | **IOException propagated raw from step definitions** | Steps like `setBodyTo()`, `bodyIsValid()` declare `throws IOException` without wrapping in a descriptive exception. | Low | Small |

---

## 3. Testing

| # | Gap | Details | Severity | Effort |
|---|---|---|---|---|
| 3.1 | **No unit tests for core classes** | `AbstractBddStepDefinition` (the 512-line core class) has zero direct unit tests. All testing is done indirectly through Cucumber feature files. | High | Large |
| 3.2 | **Cucumber tests depend on execution order** | Scenarios in `users.feature` are order-dependent (e.g., "Add tony stark" must run before "Add bruce wayne" due to shared static `ScenarioScope` and static `UserController.users`). This makes tests fragile. | High | Medium |
| 3.3 | **Only one unit test for ScenarioScope** | `ScenarioScopeTest` has a single test (`shouldBeInitialized`) that only checks maps are non-null. No tests for put/get behavior. | Medium | Small |
| 3.4 | **No negative test cases for library steps** | No tests verify that steps fail correctly when given invalid input (e.g., malformed JSON body, invalid JsonPath, null parameters). | Medium | Medium |
| 3.5 | **JUnit 4 only — no JUnit 5 support** | Library is locked to JUnit 4 via `@RunWith(Cucumber.class)` and `surefire-junit4` provider. JUnit 5 is the current standard. | Medium | Medium |
| 3.6 | **100% Jacoco coverage may be misleading** | 100% line/branch coverage is enforced, but since there are no unit tests, all coverage comes from Cucumber integration tests. This means coverage is achieved but edge cases may not be tested. | Medium | Small |
| 3.7 | **No contract tests** | No consumer-driven contract tests or schema validation for the step definitions' expected behavior. | Low | Medium |

---

## 4. Security

| # | Gap | Details | Severity | Effort |
|---|---|---|---|---|
| 4.1 | **Outdated dependencies with known CVEs** | Spring Boot 2.5.3 (July 2021), WireMock 2.27.2, Apache HttpClient 4.5.13, and other dependencies are 3+ years old and likely have known vulnerabilities. | Critical | Medium |
| 4.2 | **GPG key import via base64 environment variable** | `.travis.yml` decodes GPG secret keys from `$GPG_SECRET_KEYS` env var via `base64 --decode | gpg --import`. While standard for Travis, no key rotation or secret scanning is configured. | Medium | Small |
| 4.3 | **OSSRH credentials in plain environment variables** | `maven-settings.xml` references `${env.OSSRH_JIRA_USERNAME}` and `${env.OSSRH_JIRA_PASSWORD}` without any encryption or vault integration. | Medium | Small |
| 4.4 | **No dependency vulnerability scanning** | No OWASP Dependency Check, Snyk, or Dependabot configuration for automated CVE scanning. Renovate handles version updates but does not flag security issues. | Medium | Small |
| 4.5 | **WireMock binds to all interfaces by default** | The WireMock server created in `AbstractBddStepDefinition.setUp()` does not restrict binding to localhost. In test environments this is low risk but could be tightened. | Low | Small |

---

## 5. API Design

| # | Gap | Details | Severity | Effort |
|---|---|---|---|---|
| 5.1 | **Inconsistent "should" vs "must" semantics** | `should be` silently passes when JsonPath is null (soft assert), while `must be` fails (hard assert). This distinction is not intuitive and is poorly documented in step names. | High | Medium |
| 5.2 | **No step for asserting response body equals exact JSON** | Consumers cannot assert an entire response body matches an expected JSON structure — only individual JsonPath queries or string containment. | Medium | Medium |
| 5.3 | **No step for asserting response time / performance** | No built-in step for validating response time thresholds. | Low | Small |
| 5.4 | **No step for asserting JSON Schema compliance** | No support for validating response bodies against a JSON Schema document. | Medium | Medium |
| 5.5 | **Regex-based step patterns** | All steps use regex (`^...$`) instead of Cucumber Expressions, which are the recommended modern approach and provide better readability and type safety. | Medium | Large |
| 5.6 | **No OpenAPI / Javadoc published for steps** | The library's public API (step definitions) is documented only in README.md. No generated API documentation or step catalog is available. | Medium | Medium |
| 5.7 | **No versioning strategy for step syntax** | If step patterns change between versions, consumers' feature files break silently. No deprecation or migration strategy exists. | Medium | Small |

---

## 6. Observability

| # | Gap | Details | Severity | Effort |
|---|---|---|---|---|
| 6.1 | **No logging** | Neither `AbstractBddStepDefinition` nor `DefaultRestApiBddStepDefinition` use any logging framework (SLF4J, Log4j). All debug output goes through WireMock's `ConsoleNotifier` only. | High | Small |
| 6.2 | **No Cucumber reporting plugins configured** | `RestApiCucumberTest` only uses `plugin = {"pretty"}`. No HTML, JSON, or JUnit XML report generation is configured. | Medium | Small |
| 6.3 | **WireMock verbose logging always on** | `ConsoleNotifier(true)` is hardcoded, flooding test output with WireMock request/response details. Not configurable by consumers. | Medium | Small |
| 6.4 | **No request/response logging for debugging** | When a step assertion fails, there's no automatic log of the actual HTTP request sent or full response received. Consumers must manually debug. | Medium | Medium |

---

## 7. Resilience

| # | Gap | Details | Severity | Effort |
|---|---|---|---|---|
| 7.1 | **No timeout configuration for HTTP requests** | `TestRestTemplate` uses default timeouts (effectively infinite). Long-running or hanging test servers will block test execution indefinitely. | High | Small |
| 7.2 | **WireMock port collision risk** | Default WireMock port 8888 is hardcoded with a single configurable property. No automatic port selection or collision detection. | Medium | Small |
| 7.3 | **No retry mechanism for flaky requests** | No built-in retry or polling steps for eventually-consistent APIs. | Low | Medium |
| 7.4 | **No WireMock verification steps** | No steps to verify that expected WireMock stubs were actually called (or called the expected number of times). | Medium | Small |
| 7.5 | **No cleanup between scenarios** | The static `ScenarioScope` is never reset between scenarios (by design for cross-scenario sharing), but this means state leaks can cause cascading test failures. | High | Medium |

---

## Summary Table

| Category | Gaps Found | Critical | High | Medium | Low |
|---|---|---|---|---|---|
| Code Organization | 7 | 1 | 1 | 2 | 3 |
| Error Handling | 5 | 0 | 2 | 2 | 1 |
| Testing | 7 | 0 | 2 | 4 | 1 |
| Security | 5 | 1 | 0 | 3 | 1 |
| API Design | 7 | 0 | 1 | 4 | 1 |
| Observability | 4 | 0 | 1 | 3 | 0 |
| Resilience | 5 | 0 | 2 | 2 | 1 |
| **Total** | **40** | **2** | **9** | **20** | **8** |
