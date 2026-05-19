# Auto-generated BDD feature file for PetClinic Visits API
# Generated from OpenAPI spec by generate-petclinic-features.js
Feature: Visits API CRUD lifecycle and error handling

  Background:
    Given http baseUri is /petclinic/api/
    And I set http headers to:
      | Accept        | application/json  |
      | Content-Type  | application/json  |

  Scenario: List all visits
    When I GET /visits
    Then http response code should be 200
    And http response body should be valid json
    And http response body is typed as array for path $

  Scenario: Create a new visit
    And I set http body to {"date":"2025-06-01","description":"Annual checkup","petId":1}
    When I POST /visits
    Then http response code should be 201
    And http response body should be valid json
    And http response body path $.description should be Annual checkup
    And http response body path $.id should exists
    And I store the value of http body path $.id as createdvisitId in scenario scope

  Scenario: Get visit by ID
    When I GET /visits/1
    Then http response code should be 200
    And http response body should be valid json
    And http response body path $.id should be 1

  Scenario: Update an existing visit
    And I set http body to {"date":"2025-06-15","description":"Follow-up visit","petId":1}
    When I PUT /visits/1
    Then http response code should be 204

  Scenario: Get non-existent visit returns 404
    When I GET /visits/999999
    Then http response code should be 404

  Scenario: Create visit with invalid body returns 400
    And I set http body to {}
    When I POST /visits
    Then http response code should be 400

  Scenario: Delete visit by ID
    And I set http body to {"date":"2025-06-01","description":"Annual checkup","petId":1}
    When I POST /visits
    Then http response code should be 201
    And I store the value of http body path $.id as toDeletevisitId in scenario scope
    When I DELETE /visits/`$toDeletevisitId`
    Then http response code should be 204

  Scenario: Delete non-existent visit returns 404
    When I DELETE /visits/999999
    Then http response code should be 404
