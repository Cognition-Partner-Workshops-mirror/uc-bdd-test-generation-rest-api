# Auto-generated BDD feature for PetClinic error endpoint
Feature: Failing endpoint error handling

  Background:
    Given http baseUri is /petclinic/api/
    And I set http headers to:
      | Accept        | application/json  |
      | Content-Type  | application/json  |

  Scenario: The /oops endpoint returns a server error
    When I GET /oops
    Then http response code should be 500
