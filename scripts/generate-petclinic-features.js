#!/usr/bin/env node

/**
 * BDD Feature File Generator for PetClinic REST API
 *
 * Parses the PetClinic OpenAPI 3.0 spec (openapi.yml) and auto-generates
 * Cucumber/Gherkin .feature files for each resource (owners, pets, vets,
 * pet types, specialties, visits). Produces CRUD lifecycle scenarios plus
 * negative test cases (404, 400 validation errors).
 *
 * Usage:
 *   node scripts/generate-petclinic-features.js <path-to-openapi.yml> <output-dir>
 */

const fs = require('fs');
const path = require('path');
const yaml = require('js-yaml');

// Parse command-line arguments
const specPath = process.argv[2];
const outputDir = process.argv[3];

if (!specPath || !outputDir) {
  console.error('Usage: node generate-petclinic-features.js <openapi.yml> <output-dir>');
  process.exit(1);
}

// Load and parse the OpenAPI YAML spec
const specContent = fs.readFileSync(specPath, 'utf8');
const spec = yaml.load(specContent);

// Extract base URI from server URL
const serverUrl = spec.servers?.[0]?.url || 'http://localhost:9966/petclinic/api';
const baseUri = new URL(serverUrl).pathname;

// Ensure output directory exists
fs.mkdirSync(outputDir, { recursive: true });

/**
 * Resolve a $ref to the actual schema object.
 * Handles local JSON pointer references like #/components/schemas/Owner.
 */
function resolveRef(ref) {
  if (!ref) return {};
  const parts = ref.replace('#/', '').split('/');
  let resolved = spec;
  for (const part of parts) {
    resolved = resolved?.[part];
  }
  return resolved || {};
}

/**
 * Flatten an allOf schema into a single properties map.
 * Recursively resolves $ref within allOf entries.
 */
function flattenSchema(schema) {
  if (!schema) return {};
  if (schema.$ref) {
    return flattenSchema(resolveRef(schema.$ref));
  }
  if (schema.allOf) {
    let merged = {};
    for (const sub of schema.allOf) {
      const resolved = sub.$ref ? resolveRef(sub.$ref) : sub;
      const flat = flattenSchema(resolved);
      merged = { ...merged, ...flat.properties, ...resolved.properties };
    }
    return { properties: merged };
  }
  return schema;
}

/**
 * Build a sample JSON body from schema properties using example values.
 * Skips readOnly fields (like id, pets, visits).
 */
function buildSampleBody(schema, overrides = {}) {
  const flat = flattenSchema(schema);
  const props = flat.properties || {};
  const body = {};
  for (const [key, prop] of Object.entries(props)) {
    // Skip read-only fields (server-generated IDs, nested collections)
    if (prop.readOnly) continue;
    if (overrides[key] !== undefined) {
      body[key] = overrides[key];
    } else if (prop.example !== undefined) {
      body[key] = prop.example;
    } else if (prop.type === 'string') {
      body[key] = 'test';
    } else if (prop.type === 'integer') {
      body[key] = 1;
    } else if (prop.type === 'array') {
      body[key] = [];
    } else if (prop.$ref) {
      // Nested object — use example values from referenced schema
      const nested = resolveRef(prop.$ref);
      body[key] = buildSampleBody(nested);
    }
  }
  return body;
}

/**
 * Group operations by tag and extract CRUD operations for each resource.
 * Returns a map of tag -> { list, get, create, update, delete } operations.
 */
function groupByTag() {
  const groups = {};
  for (const [pathStr, methods] of Object.entries(spec.paths || {})) {
    for (const [method, op] of Object.entries(methods)) {
      if (!op.tags || !op.operationId) continue;
      const tag = op.tags[0];
      if (!groups[tag]) groups[tag] = [];
      groups[tag].push({
        path: pathStr,
        method: method.toUpperCase(),
        operationId: op.operationId,
        summary: op.summary || '',
        parameters: op.parameters || [],
        requestBody: op.requestBody,
        responses: op.responses || {},
      });
    }
  }
  return groups;
}

// ── Resource definitions: maps each PetClinic resource to its API shape ──

const resources = {
  pettypes: {
    tag: 'pettypes',
    name: 'Pet Types',
    basePath: '/pettypes',
    idParam: 'petTypeId',
    createSchema: 'PetTypeFields',
    responseSchema: 'PetType',
    // Sample data for CRUD lifecycle
    createBody: { name: 'hamster' },
    updateBody: { name: 'hamster-updated' },
    validateField: 'name',
    validateValue: 'hamster',
    updateValidateValue: 'hamster-updated',
  },
  specialty: {
    tag: 'specialty',
    name: 'Specialties',
    basePath: '/specialties',
    idParam: 'specialtyId',
    createSchema: 'Specialty',
    responseSchema: 'Specialty',
    createBody: { name: 'oncology' },
    updateBody: { name: 'oncology-updated' },
    validateField: 'name',
    validateValue: 'oncology',
    updateValidateValue: 'oncology-updated',
  },
  vet: {
    tag: 'vet',
    name: 'Vets',
    basePath: '/vets',
    idParam: 'vetId',
    createSchema: 'VetFields',
    responseSchema: 'Vet',
    createBody: { firstName: 'Alice', lastName: 'Smith', specialties: [] },
    updateBody: { firstName: 'Alice', lastName: 'SmithUpdated', specialties: [] },
    validateField: 'lastName',
    validateValue: 'Smith',
    updateValidateValue: 'SmithUpdated',
  },
  owner: {
    tag: 'owner',
    name: 'Owners',
    basePath: '/owners',
    idParam: 'ownerId',
    createSchema: 'OwnerFields',
    responseSchema: 'Owner',
    createBody: { firstName: 'Test', lastName: 'Owner', address: '123 Main St', city: 'Springfield', telephone: '5551234567' },
    updateBody: { firstName: 'Test', lastName: 'OwnerUpdated', address: '456 Oak Ave', city: 'Shelbyville', telephone: '5559876543' },
    validateField: 'lastName',
    validateValue: 'Owner',
    updateValidateValue: 'OwnerUpdated',
  },
  pet: {
    tag: 'pet',
    name: 'Pets',
    basePath: '/pets',
    idParam: 'petId',
    createSchema: 'PetFields',
    responseSchema: 'Pet',
    // Pets require an owner; create body references a known owner ID
    createBody: null, // handled in owner feature
    updateBody: { name: 'BuddyUpdated', birthDate: '2020-06-15', type: { id: 1, name: 'cat' } },
    validateField: 'name',
    validateValue: 'Buddy',
    updateValidateValue: 'BuddyUpdated',
  },
  visit: {
    tag: 'visit',
    name: 'Visits',
    basePath: '/visits',
    idParam: 'visitId',
    createSchema: 'VisitFields',
    responseSchema: 'Visit',
    createBody: { date: '2025-06-01', description: 'Annual checkup', petId: 1 },
    updateBody: { date: '2025-06-15', description: 'Follow-up visit', petId: 1 },
    validateField: 'description',
    validateValue: 'Annual checkup',
    updateValidateValue: 'Follow-up visit',
  },
};

/**
 * Generate a Gherkin feature file string for a given resource.
 * Produces scenarios for: List, Create, Read, Update, Delete, 404 on missing, 400 on invalid input.
 */
function generateFeature(resourceKey, resource) {
  const lines = [];
  const { name, basePath, idParam, createBody, updateBody, validateField, validateValue, updateValidateValue } = resource;

  lines.push(`# Auto-generated BDD feature file for PetClinic ${name} API`);
  lines.push(`# Generated from OpenAPI spec by generate-petclinic-features.js`);
  lines.push(`Feature: ${name} API CRUD lifecycle and error handling`);
  lines.push('');
  lines.push('  Background:');
  lines.push(`    Given http baseUri is ${baseUri}/`);
  lines.push('    And I set http headers to:');
  lines.push('      | Accept        | application/json  |');
  lines.push('      | Content-Type  | application/json  |');
  lines.push('');

  // ── Scenario 1: List all ──
  lines.push(`  Scenario: List all ${name.toLowerCase()}`);
  lines.push(`    When I GET ${basePath}`);
  lines.push(`    Then http response code should be 200`);
  lines.push(`    And http response body should be valid json`);
  lines.push(`    And http response body is typed as array for path $`);
  lines.push('');

  // ── Scenario 2: Create ──
  if (createBody) {
    lines.push(`  Scenario: Create a new ${name.toLowerCase().replace(/s$/, '')}`);
    lines.push(`    And I set http body to ${JSON.stringify(createBody)}`);
    lines.push(`    When I POST ${basePath}`);
    lines.push(`    Then http response code should be 201`);
    lines.push(`    And http response body should be valid json`);
    lines.push(`    And http response body path $.${validateField} should be ${validateValue}`);
    lines.push(`    And http response body path $.id should exists`);
    lines.push(`    And I store the value of http body path $.id as created${resourceKey}Id in scenario scope`);
    lines.push('');
  }

  // ── Scenario 3: Get by ID ──
  // Use a known pre-seeded ID (1) for a simple GET test
  lines.push(`  Scenario: Get ${name.toLowerCase().replace(/s$/, '')} by ID`);
  lines.push(`    When I GET ${basePath}/1`);
  lines.push(`    Then http response code should be 200`);
  lines.push(`    And http response body should be valid json`);
  lines.push(`    And http response body path $.id should be 1`);
  lines.push('');

  // ── Scenario 4: Update ──
  // PetClinic returns 204 No Content for successful PUT operations
  if (updateBody) {
    lines.push(`  Scenario: Update an existing ${name.toLowerCase().replace(/s$/, '')}`);
    lines.push(`    And I set http body to ${JSON.stringify(updateBody)}`);
    lines.push(`    When I PUT ${basePath}/1`);
    lines.push(`    Then http response code should be 204`);
    lines.push('');
  }

  // ── Scenario 5: Get non-existent → 404 ──
  lines.push(`  Scenario: Get non-existent ${name.toLowerCase().replace(/s$/, '')} returns 404`);
  lines.push(`    When I GET ${basePath}/999999`);
  lines.push(`    Then http response code should be 404`);
  lines.push('');

  // ── Scenario 6: Create with invalid body → 400 ──
  if (createBody) {
    lines.push(`  Scenario: Create ${name.toLowerCase().replace(/s$/, '')} with invalid body returns 400`);
    lines.push(`    And I set http body to {}`);
    lines.push(`    When I POST ${basePath}`);
    lines.push(`    Then http response code should be 400`);
    lines.push('');
  }

  // ── Scenario 7: Delete ──
  // Only for resources that have a delete endpoint
  const group = groupByTag()[resource.tag] || [];
  const hasDelete = group.some(op => op.method === 'DELETE');
  if (hasDelete) {
    lines.push(`  Scenario: Delete ${name.toLowerCase().replace(/s$/, '')} by ID`);
    // Create a fresh entity to delete, to avoid deleting seed data
    if (createBody) {
      lines.push(`    And I set http body to ${JSON.stringify(createBody)}`);
      lines.push(`    When I POST ${basePath}`);
      lines.push(`    Then http response code should be 201`);
      lines.push(`    And I store the value of http body path $.id as toDelete${resourceKey}Id in scenario scope`);
      lines.push(`    When I DELETE ${basePath}/\`$toDelete${resourceKey}Id\``);
    } else {
      lines.push(`    When I DELETE ${basePath}/1`);
    }
    // PetClinic returns 204 No Content for successful DELETE operations
    lines.push(`    Then http response code should be 204`);
    lines.push('');
  }

  // ── Scenario 8: Delete non-existent → 404 ──
  if (hasDelete) {
    lines.push(`  Scenario: Delete non-existent ${name.toLowerCase().replace(/s$/, '')} returns 404`);
    lines.push(`    When I DELETE ${basePath}/999999`);
    lines.push(`    Then http response code should be 404`);
    lines.push('');
  }

  return lines.join('\n');
}

/**
 * Generate the special Owner-Pet-Visit lifecycle feature.
 * Tests the nested resource creation flow:
 * Owner -> Pet (under owner) -> Visit (under owner's pet)
 */
function generateOwnerPetVisitFeature() {
  const lines = [];
  lines.push('# Auto-generated BDD feature file for PetClinic Owner-Pet-Visit lifecycle');
  lines.push('# Tests nested resource creation: Owner → Pet → Visit');
  lines.push('Feature: Owner-Pet-Visit nested resource lifecycle');
  lines.push('');
  lines.push('  Background:');
  lines.push(`    Given http baseUri is ${baseUri}/`);
  lines.push('    And I set http headers to:');
  lines.push('      | Accept        | application/json  |');
  lines.push('      | Content-Type  | application/json  |');
  lines.push('');

  // Step 1: Create owner
  lines.push('  Scenario: Full lifecycle - create owner, add pet, add visit');
  lines.push('    # Step 1: Create a new owner');
  lines.push(`    And I set http body to ${JSON.stringify({ firstName: 'Integration', lastName: 'Tester', address: '789 Test Blvd', city: 'Testville', telephone: '5550001111' })}`);
  lines.push('    When I POST /owners');
  lines.push('    Then http response code should be 201');
  lines.push('    And http response body path $.firstName should be Integration');
  lines.push('    And I store the value of http body path $.id as newOwnerId in scenario scope');
  lines.push('');

  // Step 2: Add pet to owner
  lines.push('    # Step 2: Add a pet to the new owner');
  lines.push(`    And I set http body to ${JSON.stringify({ name: 'Buddy', birthDate: '2020-01-15', type: { id: 2, name: 'dog' } })}`);
  lines.push('    When I POST /owners/`$newOwnerId`/pets');
  lines.push('    Then http response code should be 201');
  lines.push('    And http response body path $.name should be Buddy');
  lines.push('    And I store the value of http body path $.id as newPetId in scenario scope');
  lines.push('');

  // Step 3: Add visit to pet
  lines.push('    # Step 3: Schedule a vet visit for the pet');
  lines.push(`    And I set http body to ${JSON.stringify({ date: '2025-07-01', description: 'Vaccination appointment' })}`);
  lines.push('    When I POST /owners/`$newOwnerId`/pets/`$newPetId`/visits');
  lines.push('    Then http response code should be 201');
  lines.push('    And http response body path $.description should be Vaccination appointment');
  lines.push('');

  // Step 4: Verify owner has pet with visit
  lines.push('    # Step 4: Verify the owner now has the pet');
  lines.push('    When I GET /owners/`$newOwnerId`');
  lines.push('    Then http response code should be 200');
  lines.push('    And http response body path $.pets should exists');
  lines.push('');

  return lines.join('\n');
}

/**
 * Generate the error-handling feature for the /oops endpoint.
 */
function generateFailingEndpointFeature() {
  const lines = [];
  lines.push('# Auto-generated BDD feature for PetClinic error endpoint');
  lines.push('Feature: Failing endpoint error handling');
  lines.push('');
  lines.push('  Background:');
  lines.push(`    Given http baseUri is ${baseUri}/`);
  lines.push('    And I set http headers to:');
  lines.push('      | Accept        | application/json  |');
  lines.push('      | Content-Type  | application/json  |');
  lines.push('');
  lines.push('  Scenario: The /oops endpoint returns a server error');
  lines.push('    When I GET /oops');
  lines.push('    Then http response code should be 500');
  lines.push('');

  return lines.join('\n');
}

// ── Main: generate all feature files ──

let totalScenarios = 0;
const generated = [];

// Generate per-resource CRUD feature files
for (const [key, resource] of Object.entries(resources)) {
  const content = generateFeature(key, resource);
  const fileName = `petclinic-${key}.feature`;
  const filePath = path.join(outputDir, fileName);
  fs.writeFileSync(filePath, content);
  const scenarioCount = (content.match(/Scenario:/g) || []).length;
  totalScenarios += scenarioCount;
  generated.push({ fileName, scenarioCount });
  console.log(`Generated ${fileName} (${scenarioCount} scenarios)`);
}

// Generate nested lifecycle feature
const lifecycleContent = generateOwnerPetVisitFeature();
const lifecycleFile = 'petclinic-owner-pet-visit-lifecycle.feature';
fs.writeFileSync(path.join(outputDir, lifecycleFile), lifecycleContent);
const lifecycleCount = (lifecycleContent.match(/Scenario:/g) || []).length;
totalScenarios += lifecycleCount;
generated.push({ fileName: lifecycleFile, scenarioCount: lifecycleCount });
console.log(`Generated ${lifecycleFile} (${lifecycleCount} scenarios)`);

// Generate error endpoint feature
const errorContent = generateFailingEndpointFeature();
const errorFile = 'petclinic-error-handling.feature';
fs.writeFileSync(path.join(outputDir, errorFile), errorContent);
const errorCount = (errorContent.match(/Scenario:/g) || []).length;
totalScenarios += errorCount;
generated.push({ fileName: errorFile, scenarioCount: errorCount });
console.log(`Generated ${errorFile} (${errorCount} scenarios)`);

// Print summary
console.log('\n=== Generation Summary ===');
console.log(`Total feature files: ${generated.length}`);
console.log(`Total scenarios: ${totalScenarios}`);
console.log(`Output directory: ${outputDir}`);
for (const g of generated) {
  console.log(`  - ${g.fileName}: ${g.scenarioCount} scenarios`);
}
