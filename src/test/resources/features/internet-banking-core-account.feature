@internet-banking
Feature: Internet Banking Core Account Service API tests

  Background:
    Given http baseUri is /api/v1/account/
    And I set http headers to:
      | Accept        | application/json  |
      | Content-Type  | application/json  |

  Scenario: Get bank account by valid account number
    When I GET /bank-account/100015003000
    Then http response code should be 200
    And http response body should be valid json
    And http response body path $.number should be 100015003000
    And http response body path $.id should exists
    And I store the value of http body path $.number as bankAccountNumber in scenario scope

  Scenario: Get another bank account by account number
    When I GET /bank-account/100015003001
    Then http response code should be 200
    And http response body should be valid json
    And http response body path $.number should be 100015003001
    And http response body path $.id should exists

  Scenario: Get bank account with invalid account number should return not found
    When I GET /bank-account/INVALID_NUMBER
    Then http response code should not be 200

  Scenario: Get utility account by valid provider name
    When I GET /util-account/HUTCH
    Then http response code should be 200
    And http response body should be valid json
    And http response body path $.providerName should be HUTCH
    And http response body path $.id should exists

  Scenario: Get utility account with invalid provider name should return not found
    When I GET /util-account/INVALID_PROVIDER
    Then http response code should not be 200
