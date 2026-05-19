# Auto-generated BDD feature file for PetClinic Vets API
# Generated from OpenAPI spec by generate-petclinic-features.js
Feature: Vets API CRUD lifecycle and error handling

  Background:
    Given http baseUri is /petclinic/api/
    And I set http headers to:
      | Accept        | application/json  |
      | Content-Type  | application/json  |

  Scenario: List all vets
    When I GET /vets
    Then http response code should be 200
    And http response body should be valid json
    And http response body is typed as array for path $

  Scenario: Create a new vet
    And I set http body to {"firstName":"Alice","lastName":"Smith","specialties":[]}
    When I POST /vets
    Then http response code should be 201
    And http response body should be valid json
    And http response body path $.lastName should be Smith
    And http response body path $.id should exists
    And I store the value of http body path $.id as createdvetId in scenario scope

  Scenario: Get vet by ID
    When I GET /vets/1
    Then http response code should be 200
    And http response body should be valid json
    And http response body path $.id should be 1

  Scenario: Update an existing vet
    And I set http body to {"firstName":"Alice","lastName":"SmithUpdated","specialties":[]}
    When I PUT /vets/1
    Then http response code should be 204

  Scenario: Get non-existent vet returns 404
    When I GET /vets/999999
    Then http response code should be 404

  Scenario: Create vet with invalid body returns 400
    And I set http body to {}
    When I POST /vets
    Then http response code should be 400

  Scenario: Delete vet by ID
    And I set http body to {"firstName":"Alice","lastName":"Smith","specialties":[]}
    When I POST /vets
    Then http response code should be 201
    And I store the value of http body path $.id as toDeletevetId in scenario scope
    When I DELETE /vets/`$toDeletevetId`
    Then http response code should be 204

  Scenario: Delete non-existent vet returns 404
    When I DELETE /vets/999999
    Then http response code should be 404
