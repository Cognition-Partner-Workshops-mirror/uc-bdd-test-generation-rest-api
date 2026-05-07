@internet-banking
Feature: Internet Banking Core Transaction Service API tests

  Background:
    Given http baseUri is /api/v1/transaction/
    And I set http headers to:
      | Accept        | application/json  |
      | Content-Type  | application/json  |

  Scenario: Process fund transfer via core banking service
    And I set http body to {"fromAccount":"100015003001","toAccount":"100015003000","amount":2000.00}
    When I POST /fund-transfer
    Then http response code should be 200
    And http response body should be valid json
    And http response body path $.transactionId should exists
    And http response body path $.message should exists
    And I store the value of http body path $.transactionId as coreTransferId in scenario scope

  Scenario: Process fund transfer with small amount
    And I set http body to {"fromAccount":"100015003001","toAccount":"100015003000","amount":50.00}
    When I POST /fund-transfer
    Then http response code should be 200
    And http response body should be valid json
    And http response body path $.transactionId should exists

  Scenario: Process utility payment via core banking service
    And I set http body to {"providerId":2,"amount":250,"referenceNumber":"0712402547","account":"100015003000"}
    When I POST /util-payment
    Then http response code should be 200
    And http response body should be valid json
    And http response body path $.transactionId should exists
    And http response body path $.message should exists
    And I store the value of http body path $.transactionId as corePaymentId in scenario scope

  Scenario: Process utility payment with different reference number
    And I set http body to {"providerId":2,"amount":500,"referenceNumber":"0776543210","account":"100015003000"}
    When I POST /util-payment
    Then http response code should be 200
    And http response body should be valid json
    And http response body path $.transactionId should exists

  Scenario: Fund transfer with same source and destination should fail
    And I set http body to {"fromAccount":"100015003000","toAccount":"100015003000","amount":100.00}
    When I POST /fund-transfer
    Then http response code should not be 200

  Scenario: Fund transfer with missing fields should fail
    And I set http body to {"fromAccount":"100015003001"}
    When I POST /fund-transfer
    Then http response code should not be 200

  Scenario: Utility payment with missing fields should fail
    And I set http body to {"providerId":2}
    When I POST /util-payment
    Then http response code should not be 200
