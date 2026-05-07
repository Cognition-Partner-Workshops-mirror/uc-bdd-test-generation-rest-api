@internet-banking
Feature: Internet Banking Fund Transfer Service API tests

  Background:
    Given http baseUri is /api/v1/transfer/
    And I set http headers to:
      | Accept        | application/json  |
      | Content-Type  | application/json  |

  Scenario: Process a fund transfer between accounts
    And I set http body to {"fromAccount":"100015003001","toAccount":"100015003000","amount":2000.00}
    When I POST /
    Then http response code should be 200
    And http response body should be valid json
    And http response body path $.transactionId should exists
    And I store the value of http body path $.transactionId as transferId in scenario scope

  Scenario: Process a fund transfer with decimal amount
    And I set http body to {"fromAccount":"100015003001","toAccount":"100015003000","amount":1250.34}
    When I POST /
    Then http response code should be 200
    And http response body should be valid json
    And http response body path $.transactionId should exists

  Scenario: Read all fund transfers
    When I GET /
    Then http response code should be 200
    And http response body should be valid json
    And http response body is typed as array for path $.content
    And http response body path $.content.[0].transactionId should exists

  Scenario: Fund transfer with insufficient balance should fail
    And I set http body to {"fromAccount":"100015003001","toAccount":"100015003000","amount":99999999.00}
    When I POST /
    Then http response code should not be 200

  Scenario: Fund transfer with invalid source account should fail
    And I set http body to {"fromAccount":"INVALID_ACCOUNT","toAccount":"100015003000","amount":100.00}
    When I POST /
    Then http response code should not be 200

  Scenario: Fund transfer with invalid destination account should fail
    And I set http body to {"fromAccount":"100015003001","toAccount":"INVALID_ACCOUNT","amount":100.00}
    When I POST /
    Then http response code should not be 200

  Scenario: Fund transfer with zero amount should fail
    And I set http body to {"fromAccount":"100015003001","toAccount":"100015003000","amount":0}
    When I POST /
    Then http response code should not be 200

  Scenario: Fund transfer with negative amount should fail
    And I set http body to {"fromAccount":"100015003001","toAccount":"100015003000","amount":-500.00}
    When I POST /
    Then http response code should not be 200
