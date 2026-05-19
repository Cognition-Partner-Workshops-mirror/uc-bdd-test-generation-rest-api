# Auto-generated BDD feature file for PetClinic Owner-Pet-Visit lifecycle
# Tests nested resource creation: Owner → Pet → Visit
Feature: Owner-Pet-Visit nested resource lifecycle

  Background:
    Given http baseUri is /petclinic/api/
    And I set http headers to:
      | Accept        | application/json  |
      | Content-Type  | application/json  |

  Scenario: Full lifecycle - create owner, add pet, add visit
    # Step 1: Create a new owner
    And I set http body to {"firstName":"Integration","lastName":"Tester","address":"789 Test Blvd","city":"Testville","telephone":"5550001111"}
    When I POST /owners
    Then http response code should be 201
    And http response body path $.firstName should be Integration
    And I store the value of http body path $.id as newOwnerId in scenario scope

    # Step 2: Add a pet to the new owner
    And I set http body to {"name":"Buddy","birthDate":"2020-01-15","type":{"id":2,"name":"dog"}}
    When I POST /owners/`$newOwnerId`/pets
    Then http response code should be 201
    And http response body path $.name should be Buddy
    And I store the value of http body path $.id as newPetId in scenario scope

    # Step 3: Schedule a vet visit for the pet
    And I set http body to {"date":"2025-07-01","description":"Vaccination appointment"}
    When I POST /owners/`$newOwnerId`/pets/`$newPetId`/visits
    Then http response code should be 201
    And http response body path $.description should be Vaccination appointment

    # Step 4: Verify the owner now has the pet
    When I GET /owners/`$newOwnerId`
    Then http response code should be 200
    And http response body path $.pets should exists
