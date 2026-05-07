@internet-banking
Feature: Internet Banking Utility Payment Service API tests

  Background:
    Given http baseUri is /api/v1/utility-payment/
    And I set http headers to:
      | Accept        | application/json  |
      | Content-Type  | application/json  |

  Scenario: Process a utility payment
    And I set http body to {"providerId":2,"amount":250,"referenceNumber":"0712402547","account":"100015003000"}
    When I POST /
    Then http response code should be 200
    And http response body should be valid json
    And http response body path $.transactionId should exists
    And I store the value of http body path $.transactionId as paymentId in scenario scope

  Scenario: Process a utility payment with large amount
    And I set http body to {"providerId":2,"amount":2000,"referenceNumber":"0712402547","account":"100015003000"}
    When I POST /
    Then http response code should be 200
    And http response body should be valid json
    And http response body path $.transactionId should exists

  Scenario: Read all utility payments
    When I GET /
    Then http response code should be 200
    And http response body should be valid json
    And http response body is typed as array for path $.content
    And http response body path $.content.[0].transactionId should exists

  Scenario: Process utility payment with invalid provider should fail
    And I set http body to {"providerId":99999,"amount":250,"referenceNumber":"0712402547","account":"100015003000"}
    When I POST /
    Then http response code should not be 200

  Scenario: Process utility payment with invalid account should fail
    And I set http body to {"providerId":2,"amount":250,"referenceNumber":"0712402547","account":"INVALID_ACCOUNT"}
    When I POST /
    Then http response code should not be 200

  Scenario: Process utility payment with zero amount should fail
    And I set http body to {"providerId":2,"amount":0,"referenceNumber":"0712402547","account":"100015003000"}
    When I POST /
    Then http response code should not be 200

  Scenario: Process utility payment with negative amount should fail
    And I set http body to {"providerId":2,"amount":-100,"referenceNumber":"0712402547","account":"100015003000"}
    When I POST /
    Then http response code should not be 200
