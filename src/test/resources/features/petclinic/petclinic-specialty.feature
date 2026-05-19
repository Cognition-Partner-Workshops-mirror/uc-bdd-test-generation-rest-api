# Auto-generated BDD feature file for PetClinic Specialties API
# Generated from OpenAPI spec by generate-petclinic-features.js
Feature: Specialties API CRUD lifecycle and error handling

  Background:
    Given http baseUri is /petclinic/api/
    And I set http headers to:
      | Accept        | application/json  |
      | Content-Type  | application/json  |

  Scenario: List all specialties
    When I GET /specialties
    Then http response code should be 200
    And http response body should be valid json
    And http response body is typed as array for path $

  Scenario: Create a new specialtie
    And I set http body to {"name":"oncology"}
    When I POST /specialties
    Then http response code should be 201
    And http response body should be valid json
    And http response body path $.name should be oncology
    And http response body path $.id should exists
    And I store the value of http body path $.id as createdspecialtyId in scenario scope

  Scenario: Get specialtie by ID
    When I GET /specialties/1
    Then http response code should be 200
    And http response body should be valid json
    And http response body path $.id should be 1

  Scenario: Update an existing specialtie
    And I set http body to {"name":"oncology-updated"}
    When I PUT /specialties/1
    Then http response code should be 204

  Scenario: Get non-existent specialtie returns 404
    When I GET /specialties/999999
    Then http response code should be 404

  Scenario: Create specialtie with invalid body returns 400
    And I set http body to {}
    When I POST /specialties
    Then http response code should be 400

  Scenario: Delete specialtie by ID
    And I set http body to {"name":"oncology"}
    When I POST /specialties
    Then http response code should be 201
    And I store the value of http body path $.id as toDeletespecialtyId in scenario scope
    When I DELETE /specialties/`$toDeletespecialtyId`
    Then http response code should be 204

  Scenario: Delete non-existent specialtie returns 404
    When I DELETE /specialties/999999
    Then http response code should be 404
