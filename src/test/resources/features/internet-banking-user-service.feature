@internet-banking
Feature: Internet Banking User Service API tests

  Background:
    Given http baseUri is /api/v1/bank-users/
    And I set http headers to:
      | Accept        | application/json  |
      | Content-Type  | application/json  |

  Scenario: Register a new user
    And I set http body to {"email":"john.doe@example.com","identification":"901830556V","password":"securePass123"}
    When I POST /register
    Then http response code should be 200
    And http response body should be valid json
    And http response body path $.email should be john.doe@example.com
    And http response body path $.identification should be 901830556V
    And http response body path $.id should exists
    And I store the value of http body path $.id as userId in scenario scope

  Scenario: Register a second user
    And I set http body to {"email":"jane.smith@example.com","identification":"885421678V","password":"password456"}
    When I POST /register
    Then http response code should be 200
    And http response body should be valid json
    And http response body path $.email should be jane.smith@example.com
    And http response body path $.identification should be 885421678V
    And http response body path $.id should exists
    And I store the value of http body path $.id as secondUserId in scenario scope

  Scenario: Read all registered users
    When I GET /
    Then http response code should be 200
    And http response body should be valid json
    And http response body is typed as array for path $
    And http response body path $.[0].id should exists
    And http response body path $.[0].email should exists

  Scenario: Read user by ID
    When I GET /`$userId`
    Then http response code should be 200
    And http response body should be valid json
    And http response body path $.id should be `$userId`
    And http response body path $.email should be john.doe@example.com
    And http response body path $.identification should be 901830556V

  Scenario: Update user status to APPROVED
    And I set http body to {"status":"APPROVED"}
    When I PATCH /update/`$userId`
    Then http response code should be 200
    And http response body should be valid json
    And http response body path $.status should be APPROVED

  Scenario: Update user status to DISABLED
    And I set http body to {"status":"DISABLED"}
    When I PATCH /update/`$secondUserId`
    Then http response code should be 200
    And http response body should be valid json
    And http response body path $.status should be DISABLED

  Scenario: Register user with duplicate identification should fail
    And I set http body to {"email":"duplicate@example.com","identification":"901830556V","password":"pass789"}
    When I POST /register
    Then http response code should not be 200
