# Auto-generated BDD feature file for PetClinic Pet Types API
# Generated from OpenAPI spec by generate-petclinic-features.js
Feature: Pet Types API CRUD lifecycle and error handling

  Background:
    Given http baseUri is /petclinic/api/
    And I set http headers to:
      | Accept        | application/json  |
      | Content-Type  | application/json  |

  Scenario: List all pet types
    When I GET /pettypes
    Then http response code should be 200
    And http response body should be valid json
    And http response body is typed as array for path $

  Scenario: Create a new pet type
    And I set http body to {"name":"hamster"}
    When I POST /pettypes
    Then http response code should be 201
    And http response body should be valid json
    And http response body path $.name should be hamster
    And http response body path $.id should exists
    And I store the value of http body path $.id as createdpettypesId in scenario scope

  Scenario: Get pet type by ID
    When I GET /pettypes/1
    Then http response code should be 200
    And http response body should be valid json
    And http response body path $.id should be 1

  Scenario: Update an existing pet type
    And I set http body to {"name":"hamster-updated"}
    When I PUT /pettypes/1
    Then http response code should be 204

  Scenario: Get non-existent pet type returns 404
    When I GET /pettypes/999999
    Then http response code should be 404

  Scenario: Create pet type with invalid body returns 400
    And I set http body to {}
    When I POST /pettypes
    Then http response code should be 400

  Scenario: Delete pet type by ID
    And I set http body to {"name":"hamster"}
    When I POST /pettypes
    Then http response code should be 201
    And I store the value of http body path $.id as toDeletepettypesId in scenario scope
    When I DELETE /pettypes/`$toDeletepettypesId`
    Then http response code should be 204

  Scenario: Delete non-existent pet type returns 404
    When I DELETE /pettypes/999999
    Then http response code should be 404
