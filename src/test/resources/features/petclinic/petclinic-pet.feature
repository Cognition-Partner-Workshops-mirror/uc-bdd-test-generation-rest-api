# Auto-generated BDD feature file for PetClinic Pets API
# Generated from OpenAPI spec by generate-petclinic-features.js
Feature: Pets API CRUD lifecycle and error handling

  Background:
    Given http baseUri is /petclinic/api/
    And I set http headers to:
      | Accept        | application/json  |
      | Content-Type  | application/json  |

  Scenario: List all pets
    When I GET /pets
    Then http response code should be 200
    And http response body should be valid json
    And http response body is typed as array for path $

  Scenario: Get pet by ID
    When I GET /pets/1
    Then http response code should be 200
    And http response body should be valid json
    And http response body path $.id should be 1

  Scenario: Update an existing pet
    And I set http body to {"name":"BuddyUpdated","birthDate":"2020-06-15","type":{"id":1,"name":"cat"}}
    When I PUT /pets/1
    Then http response code should be 204

  Scenario: Get non-existent pet returns 404
    When I GET /pets/999999
    Then http response code should be 404

  Scenario: Delete pet by ID
    When I DELETE /pets/1
    Then http response code should be 204

  Scenario: Delete non-existent pet returns 404
    When I DELETE /pets/999999
    Then http response code should be 404
