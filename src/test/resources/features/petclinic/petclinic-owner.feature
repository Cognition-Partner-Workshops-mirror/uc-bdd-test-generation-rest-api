# Auto-generated BDD feature file for PetClinic Owners API
# Generated from OpenAPI spec by generate-petclinic-features.js
Feature: Owners API CRUD lifecycle and error handling

  Background:
    Given http baseUri is /petclinic/api/
    And I set http headers to:
      | Accept        | application/json  |
      | Content-Type  | application/json  |

  Scenario: List all owners
    When I GET /owners
    Then http response code should be 200
    And http response body should be valid json
    And http response body is typed as array for path $

  Scenario: Create a new owner
    And I set http body to {"firstName":"Test","lastName":"Owner","address":"123 Main St","city":"Springfield","telephone":"5551234567"}
    When I POST /owners
    Then http response code should be 201
    And http response body should be valid json
    And http response body path $.lastName should be Owner
    And http response body path $.id should exists
    And I store the value of http body path $.id as createdownerId in scenario scope

  Scenario: Get owner by ID
    When I GET /owners/1
    Then http response code should be 200
    And http response body should be valid json
    And http response body path $.id should be 1

  Scenario: Update an existing owner
    And I set http body to {"firstName":"Test","lastName":"OwnerUpdated","address":"456 Oak Ave","city":"Shelbyville","telephone":"5559876543"}
    When I PUT /owners/1
    Then http response code should be 204

  Scenario: Get non-existent owner returns 404
    When I GET /owners/999999
    Then http response code should be 404

  Scenario: Create owner with invalid body returns 400
    And I set http body to {}
    When I POST /owners
    Then http response code should be 400

  Scenario: Delete owner by ID
    And I set http body to {"firstName":"Test","lastName":"Owner","address":"123 Main St","city":"Springfield","telephone":"5551234567"}
    When I POST /owners
    Then http response code should be 201
    And I store the value of http body path $.id as toDeleteownerId in scenario scope
    When I DELETE /owners/`$toDeleteownerId`
    Then http response code should be 204

  Scenario: Delete non-existent owner returns 404
    When I DELETE /owners/999999
    Then http response code should be 404
