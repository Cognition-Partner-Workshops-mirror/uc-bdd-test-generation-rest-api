# Knowledge Base — cucumber-restapi (BDD REST API Testing Library)

## 1. Architecture Overview

### Project Identity

| Attribute | Value |
|---|---|
| **Group / Artifact** | `fr.redfroggy.test.bdd` / `cucumber-restapi` |
| **Version** | `1.13.2-SNAPSHOT` |
| **License** | MIT |
| **Organization** | RedFroggy (redfroggy.fr) |
| **Type** | Reusable Java library (JAR) — not a standalone application |

### Purpose

`cucumber-restapi` is a **BDD testing library** that provides pre-built Cucumber/Gherkin step definitions for validating RESTful APIs. Consumers add this library as a Maven dependency and immediately gain access to a rich set of Given/When/Then steps for:

- Making HTTP requests (GET, POST, PUT, PATCH, DELETE, HEAD)
- Asserting response status codes, headers, and JSON body content via JsonPath
- Storing and reusing values across scenarios (scenario scope)
- Mocking third-party APIs via WireMock
- Multipart file upload testing
- Custom authentication via an SPI interface

### Component Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Consumer Test Suite                       │
│  ┌──────────────────┐   ┌──────────────────────────────┐    │
│  │ CucumberTest     │   │ DefaultStepDefinition        │    │
│  │ (@RunWith)       │   │ (@CucumberContextConfiguration) │ │
│  │ features = ...   │   │ implements BddRestTemplate   │    │
│  │ glue = ...       │   │ Authentication               │    │
│  └──────────────────┘   └──────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                         ▼ depends on ▼
┌─────────────────────────────────────────────────────────────┐
│              cucumber-restapi Library (this repo)           │
│                                                             │
│  ┌──────────────────────────────────────────┐               │
│  │ DefaultRestApiBddStepDefinition          │               │
│  │  - 30+ Gherkin step definitions          │               │
│  │  - HTTP verb methods (GET/POST/PUT/etc.) │               │
│  │  - Response assertion methods            │               │
│  │  - Header/body/JsonPath checks           │               │
│  │  - WireMock integration steps            │               │
│  └────────────────┬─────────────────────────┘               │
│                   │ extends                                  │
│  ┌────────────────▼─────────────────────────┐               │
│  │ AbstractBddStepDefinition                │               │
│  │  - TestRestTemplate HTTP execution       │               │
│  │  - WireMock server lifecycle             │               │
│  │  - JsonPath parsing & assertion logic    │               │
│  │  - Dynamic parameter replacement         │               │
│  │  - Multipart request support             │               │
│  └──────────────────────────────────────────┘               │
│                                                             │
│  ┌──────────────────────┐  ┌─────────────────────────────┐  │
│  │ ScenarioScope        │  │ BddRestTemplateAuthentication│ │
│  │  - headers map       │  │  (SPI interface)             │ │
│  │  - jsonPaths map     │  │  authenticate(login, pwd)    │ │
│  └──────────────────────┘  └─────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Source Layout

| Path | Description |
|---|---|
| `src/main/java/fr/redfroggy/bdd/restapi/authentication/` | SPI interface for pluggable authentication |
| `src/main/java/fr/redfroggy/bdd/restapi/glue/` | Core step definitions (`AbstractBddStepDefinition`, `DefaultRestApiBddStepDefinition`) |
| `src/main/java/fr/redfroggy/bdd/restapi/scope/` | `ScenarioScope` — cross-step state storage |
| `src/test/java/fr/redfroggy/bdd/restapi/` | Test application, Cucumber runner, sample controller & DTOs |
| `src/test/resources/features/` | Gherkin `.feature` files (users.feature, users-import.feature) |
| `src/test/resources/fixtures/` | JSON fixture files for request/response payloads |
| `src/test/resources/upload/` | CSV file for multipart upload test |

---

## 2. Data Models

### Library Internal Models

| Class | Package | Purpose | Key Fields |
|---|---|---|---|
| `ScenarioScope` | `scope` | Stores state shared across Gherkin steps within a scenario | `headers: Map<String,Object>`, `jsonPaths: Map<String,Object>` |

### Test-Only Models (Example Application)

These classes exist solely under `src/test/` to validate the library itself:

| Class | Purpose | Key Fields |
|---|---|---|
| `UserDTO` | Primary user entity (extends `PartialUserDTO`) | `id: String`, `firstName: String`, `age: int`, `relatedTo: UserDTO`, `sessionIds: List<String>`, `details: UserDetailsDTO` |
| `PartialUserDTO` | Base class for partial updates (PATCH) | `lastName: String` |
| `UserDetailsDTO` | Third-party enrichment data (from WireMock) | `comicName: String`, `city: String`, `mainColors: List<String>` |
| `UserCsvLine` | CSV import mapping via OpenCSV | `id: String`, `firstName: String`, `lastName: String`, `age: int` |

### Entity Relationships

```
UserDTO ──relatedTo──► UserDTO (self-referencing)
UserDTO ──details──► UserDetailsDTO (fetched from mocked third-party API)
UserCsvLine ──mapped to──► UserDTO (during CSV import)
```

---

## 3. API Surface Map

### Library-Provided Gherkin Steps (Public API)

These are the reusable steps consumers get from the library:

#### Setup Steps (Given)

| Step Pattern | Method | Description |
|---|---|---|
| `http baseUri is {uri}` | `baseUri()` | Set base URI for all requests |
| `I set http body to {json}` | `setBodyTo()` | Set JSON request body |
| `I set http body with file {path}` | `setBodyWithFile()` | Load body from classpath file |
| `I set http body path {jsonPath} to {value}` | `setBodyWithJsonPath()` | Modify body via JsonPath |
| `I set {name} http header to {value}` | `header()` | Set single HTTP header |
| `I set http headers to: {table}` | `headers()` | Set multiple headers from data table |
| `I set http query parameter {param} to {value}` | `queryParameter()` | Add URL query parameter |
| `I authenticate with login/password {login}/{password}` | `setAuthenticateUser()` | Trigger SPI authentication |
| `I mock third party api call {method} {url} with return code {status}, content type: {type} and body: {json}` | `mockThirdPartyApiCallWithJSON()` | Set up WireMock stub with inline JSON |
| `I mock third party api call {method} {url} with return code {status}, content type: {type} and file: {file}` | `mockThirdPartyApiCallWithFileContent()` | Set up WireMock stub with file content |

#### Action Steps (When)

| Step Pattern | Method | HTTP Method |
|---|---|---|
| `I GET {resource}` | `get()` | GET |
| `I POST {resource}` | `post()` | POST |
| `I PUT {resource}` | `put()` | PUT |
| `I DELETE {resource}` | `delete()` | DELETE |
| `I PATCH {resource}` | `patch()` | PATCH |
| `I HEAD {resource}` | `head()` | HEAD |
| `I send a multipart {method} request to {uri} with: {table}` | `uploadFile()` | Any (multipart) |

#### Assertion Steps (Then)

| Step Pattern | Method | Description |
|---|---|---|
| `http response code should be {code}` | `responseCode()` | Assert exact status code |
| `http response code should not be {code}` | `notResponseCode()` | Assert status code is not X |
| `http response header {name} should exist` | `headerExists()` | Assert header presence |
| `http response header {name} should not exist` | `headerNotExists()` | Assert header absence |
| `http response header {name} should be {value}` | `headerEqual()` | Assert header value |
| `http response header {name} should not be {value}` | `headerNotEqual()` | Assert header is not value |
| `http response body should be valid json` | `bodyIsValid()` | Validate JSON format |
| `http response body should contain {value}` | `bodyContains()` | Assert body contains string |
| `http response body path {jsonPath} should exists` | `bodyPathExists()` | Assert JsonPath exists |
| `http response body path {jsonPath} should not exist` | `bodyPathDoesntExist()` | Assert JsonPath absent |
| `http response body path {jsonPath} should be {value}` | `bodyPathShouldBeEqualTo()` | Soft assert (skip if null) |
| `http response body path {jsonPath} should not be {value}` | `bodyPathShouldNotBeEqualTo()` | Soft assert not-equal |
| `http response body path {jsonPath} must be {value}` | `bodyPathMustBeEqualTo()` | Hard assert (fail if null) |
| `http response body path {jsonPath} must not be {value}` | `bodyPathMustNotBeEqualTo()` | Hard assert not-equal |
| `http response body path {jsonPath} should not have content` | `bodyPathShouldNotHaveContent()` | Assert empty collection |
| `http response body is typed as array for path {jsonPath}` | `bodyPathIsArray()` | Assert array type |
| `http response body is typed as array using path {jsonPath} with length {n}` | `bodyPathIsArrayWithLength()` | Assert array type + size |
| `I store the value of http response header {name} as {alias} in scenario scope` | `storeResponseHeader()` | Store header for reuse |
| `I store the value of http body path {jsonPath} as {alias} in scenario scope` | `storeResponseJsonPath()` | Store JsonPath value for reuse |
| `http value of scenario variable {name} should be {value}` | `scenarioVariableIsValid()` | Assert stored variable value |

### Test Application REST Endpoints (src/test only)

| Method | Path | Description | Request Body | Response |
|---|---|---|---|---|
| GET | `/api/users` | List all users (optional `?name=` filter) | — | `List<UserDTO>` |
| GET | `/api/users/{id}` | Get user by ID (enriched with third-party details) | — | `UserDTO` |
| POST | `/api/users` | Create user (JSON) | `UserDTO` | `UserDTO` (201) |
| POST | `/api/users` | Import users (multipart CSV) | `multipart/form-data` | `List<UserDTO>` |
| PUT | `/api/users/{id}` | Full update user | `UserDTO` | `UserDTO` |
| PATCH | `/api/users/{id}` | Partial update (lastName only) | `PartialUserDTO` | `UserDTO` |
| DELETE | `/api/users/{id}` | Delete user by ID | — | 200 / 404 |
| HEAD | `/api/authenticated` | Check authentication status | — | 200 / 401 |

---

## 4. Business Logic Inventory

### Core Library Logic

| Component | Logic | Location |
|---|---|---|
| **Dynamic Parameter Replacement** | Replaces `` `$varName` `` syntax with values stored in ScenarioScope (headers or jsonPaths). Recursive replacement for nested variables. | `AbstractBddStepDefinition.replaceDynamicParameters()` |
| **HTTP Request Execution** | Builds URI from baseUri + resource, attaches headers, body (for write methods), and query params. Uses `TestRestTemplate.exchange()`. | `AbstractBddStepDefinition.request()` |
| **JsonPath Assertion** | Reads JSON response body via Jayway JsonPath. Supports both soft assertions (`should be` — skips if path is null) and hard assertions (`must be` — fails if null). Handles collections and scalar values separately. | `AbstractBddStepDefinition.checkJsonPath()` |
| **WireMock Stubbing** | Parses URL query parameters, creates WireMock stubs with configurable method, URL pattern, status code, content type, and body. Supports both inline JSON and file-based responses. | `AbstractBddStepDefinition.mockThirdPartyApiCall()` |
| **Multipart Upload** | Constructs `LinkedMultiValueMap` with file entities from classpath resources. Sets `MULTIPART_FORM_DATA` content type. | `AbstractBddStepDefinition.postMultipart()` |
| **Scenario Scope** | Static singleton storing key-value pairs for headers and JsonPath results. Shared across all scenarios in a test run. | `ScenarioScope` |
| **Authentication SPI** | Consumer implements `BddRestTemplateAuthentication.authenticate()` to return a configured `TestRestTemplate` (e.g., with Basic Auth or JWT interceptors). | `BddRestTemplateAuthentication` interface |

### Test Application Logic (Example Only)

| Logic | Description |
|---|---|
| In-memory user storage | `UserController.users` is a static `ArrayList<UserDTO>` |
| Name-based search | Filters users by first or last name (case-insensitive contains) |
| Third-party enrichment | `UserDetailService` calls WireMock-mocked Marvel API to enrich user details |
| CSV import | OpenCSV parses multipart CSV file into `UserCsvLine`, mapped to `UserDTO` |
| Basic Auth check | HEAD `/api/authenticated` checks for `Authorization` header presence |

---

## 5. Integration Points

| Integration | Technology | Configuration | Purpose |
|---|---|---|---|
| **Spring Boot Test** | `spring-boot-starter-test` 2.5.3 | `@SpringBootTest(RANDOM_PORT)` or `DEFINED_PORT` | Embedded server for integration testing |
| **Cucumber** | `cucumber-java` / `cucumber-spring` / `cucumber-junit` 6.10.4 | `@RunWith(Cucumber.class)`, `@CucumberOptions` | BDD test execution framework |
| **WireMock** | `wiremock-standalone` 2.27.2 | Port configurable via `redfroggy.cucumber.restapi.wiremock.port` (default: 8888) | Mock third-party HTTP APIs |
| **JsonPath** | `json-path` (Jayway) 2.6.0 | Used in step definitions | JSON response body navigation and assertion |
| **AssertJ** | `assertj-core` 3.20.2 | Used in all assertion steps | Fluent assertion library |
| **Apache HttpClient** | `httpclient` 4.5.13 | `HttpComponentsClientHttpRequestFactory` | PATCH method support for RestTemplate |
| **OpenCSV** | `opencsv` 5.5.1 (test scope) | `@CsvBindByName` annotations | CSV file parsing for import tests |
| **Codecov** | codecov.io | `.codecov.yml` | Code coverage reporting |
| **Semantic Release** | `@conveyal/maven-semantic-release` | `.travis.yml` | Automated versioning and publishing |
| **Commitlint + Husky** | Node.js via `package.json` | `commitlint.config.js`, `.husky/commit-msg` | Conventional commit enforcement |
| **Renovate** | `renovate.json` | Monthly dependency update PRs | Automated dependency management |

---

## 6. Build and Deployment Summary

### Build System: Maven

| Phase | Plugin | Purpose |
|---|---|---|
| **compile** | Default (javac) | Compile Java source (Java 11 source/target from Spring Boot parent) |
| **test** | `maven-surefire-plugin` 2.22.2 | Run JUnit 4 tests in alphabetical order |
| **test** | `jacoco-maven-plugin` 0.8.7 | Enforce **100% line and branch coverage** per class |
| **verify** | `maven-gpg-plugin` 3.0.1 | GPG-sign artifacts (skip locally with `-Dgpg.skip=true`) |
| **package** | `maven-source-plugin` 3.2.1 | Attach source JAR |
| **package** | `maven-javadoc-plugin` 2.10.4 | Attach Javadoc JAR |
| **deploy** | `nexus-staging-maven-plugin` 1.6.8 | Deploy to Sonatype OSSRH → Maven Central |
| **revision** | `git-commit-id-plugin` 4.9.10 | Embed Git metadata in build |

### CI/CD: Travis CI

| Stage | Action |
|---|---|
| `before_install` | Import GPG keys (master branch only) |
| `install` | Skipped (`install: true`) |
| `script` | `mvn package` |
| `after_success` | Semantic release + Codecov upload |

### Key Build Commands

```bash
# Run tests (with 100% coverage enforcement)
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
mvn test -Dgpg.skip=true

# Build library JAR
mvn package -Dgpg.skip=true -DskipTests

# Install to local Maven repo
mvn install -Dgpg.skip=true
```

### Publishing

- Artifacts are published to **Maven Central** via Sonatype OSSRH
- Versioning is automated via **semantic-release** (triggered on master branch merges)
- Commit messages must follow **Conventional Commits** format (enforced by commitlint + Husky)

### Node.js Tooling

| Tool | Version | Purpose |
|---|---|---|
| Husky | 7.0.1 | Git hook management |
| @commitlint/cli | 13.1.0 | Commit message linting |
| @commitlint/config-conventional | 13.1.0 | Conventional commits ruleset |
