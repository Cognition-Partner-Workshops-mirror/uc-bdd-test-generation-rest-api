#!/usr/bin/env node

/**
 * PetClinic BDD Feature Validation Script
 *
 * Validates the auto-generated feature files by executing the CRUD lifecycle
 * against a running PetClinic server. This script verifies that:
 *   - All resources can be listed (GET collection)
 *   - Resources can be created (POST)
 *   - Resources can be read by ID (GET single)
 *   - Resources can be updated (PUT)
 *   - Non-existent resources return 404
 *   - Invalid input returns 400
 *   - Resources can be deleted (DELETE)
 *   - Nested lifecycle (Owner → Pet → Visit) works
 *
 * Usage:
 *   node scripts/validate-petclinic-features.js [base-url]
 *   Default base-url: http://localhost:9966/petclinic/api
 */

const http = require('http');
const https = require('https');

const BASE_URL = process.argv[2] || 'http://localhost:9966/petclinic/api';

let passed = 0;
let failed = 0;
const results = [];

/**
 * Make an HTTP request and return { status, body } promise.
 */
function request(method, path, body) {
  return new Promise((resolve, reject) => {
    // Concatenate base URL and path, avoiding double slashes
    const fullUrl = BASE_URL.replace(/\/$/, '') + (path.startsWith('/') ? path : '/' + path);
    const url = new URL(fullUrl);
    const lib = url.protocol === 'https:' ? https : http;

    const options = {
      hostname: url.hostname,
      port: url.port,
      path: url.pathname,
      method,
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json',
      },
    };

    const req = lib.request(options, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        let parsed = null;
        try { parsed = JSON.parse(data); } catch (e) { parsed = data; }
        resolve({ status: res.statusCode, body: parsed });
      });
    });

    req.on('error', reject);
    if (body) req.write(JSON.stringify(body));
    req.end();
  });
}

/**
 * Assert a condition and log the result.
 */
function assert(scenario, condition, detail) {
  if (condition) {
    passed++;
    results.push({ scenario, status: 'PASS', detail });
  } else {
    failed++;
    results.push({ scenario, status: 'FAIL', detail });
  }
}

async function run() {
  console.log(`\nValidating PetClinic BDD scenarios against ${BASE_URL}\n`);
  console.log('='.repeat(60));

  // ── Pet Types CRUD ──
  console.log('\n📋 Pet Types');

  let res = await request('GET', '/pettypes');
  assert('List pet types', res.status === 200, `GET /pettypes → ${res.status}`);

  res = await request('POST', '/pettypes', { name: 'hamster' });
  assert('Create pet type', res.status === 201, `POST /pettypes → ${res.status}`);
  const petTypeId = res.body?.id;
  assert('Create pet type - has id', petTypeId != null, `id=${petTypeId}`);
  assert('Create pet type - name', res.body?.name === 'hamster', `name=${res.body?.name}`);

  res = await request('GET', `/pettypes/${petTypeId}`);
  assert('Get pet type by ID', res.status === 200, `GET /pettypes/${petTypeId} → ${res.status}`);

  res = await request('PUT', `/pettypes/${petTypeId}`, { id: petTypeId, name: 'hamster-updated' });
  assert('Update pet type', res.status === 204, `PUT /pettypes/${petTypeId} → ${res.status}`);

  res = await request('GET', '/pettypes/999999');
  assert('Get non-existent pet type → 404', res.status === 404, `GET /pettypes/999999 → ${res.status}`);

  res = await request('POST', '/pettypes', {});
  assert('Create pet type invalid → 400', res.status === 400, `POST /pettypes {} → ${res.status}`);

  res = await request('DELETE', `/pettypes/${petTypeId}`);
  assert('Delete pet type', res.status === 204, `DELETE /pettypes/${petTypeId} → ${res.status}`);

  res = await request('DELETE', '/pettypes/999999');
  assert('Delete non-existent pet type → 404', res.status === 404, `DELETE /pettypes/999999 → ${res.status}`);

  // ── Specialties CRUD ──
  console.log('\n📋 Specialties');

  res = await request('GET', '/specialties');
  assert('List specialties', res.status === 200, `GET /specialties → ${res.status}`);

  res = await request('POST', '/specialties', { name: 'oncology' });
  assert('Create specialty', res.status === 201, `POST /specialties → ${res.status}`);
  const specId = res.body?.id;
  assert('Create specialty - name', res.body?.name === 'oncology', `name=${res.body?.name}`);

  res = await request('GET', `/specialties/${specId}`);
  assert('Get specialty by ID', res.status === 200, `GET /specialties/${specId} → ${res.status}`);

  res = await request('PUT', `/specialties/${specId}`, { id: specId, name: 'oncology-updated' });
  assert('Update specialty', res.status === 204, `PUT /specialties/${specId} → ${res.status}`);

  res = await request('GET', '/specialties/999999');
  assert('Get non-existent specialty → 404', res.status === 404, `GET /specialties/999999 → ${res.status}`);

  res = await request('DELETE', `/specialties/${specId}`);
  assert('Delete specialty', res.status === 204, `DELETE /specialties/${specId} → ${res.status}`);

  // ── Vets CRUD ──
  console.log('\n📋 Vets');

  res = await request('GET', '/vets');
  assert('List vets', res.status === 200, `GET /vets → ${res.status}`);

  res = await request('POST', '/vets', { firstName: 'Alice', lastName: 'Smith', specialties: [] });
  assert('Create vet', res.status === 201, `POST /vets → ${res.status}`);
  const vetId = res.body?.id;
  assert('Create vet - lastName', res.body?.lastName === 'Smith', `lastName=${res.body?.lastName}`);

  res = await request('GET', `/vets/${vetId}`);
  assert('Get vet by ID', res.status === 200, `GET /vets/${vetId} → ${res.status}`);

  res = await request('PUT', `/vets/${vetId}`, { id: vetId, firstName: 'Alice', lastName: 'SmithUpdated', specialties: [] });
  assert('Update vet', res.status === 204, `PUT /vets/${vetId} → ${res.status}`);

  res = await request('GET', '/vets/999999');
  assert('Get non-existent vet → 404', res.status === 404, `GET /vets/999999 → ${res.status}`);

  res = await request('DELETE', `/vets/${vetId}`);
  assert('Delete vet', res.status === 204, `DELETE /vets/${vetId} → ${res.status}`);

  // ── Owners CRUD ──
  console.log('\n📋 Owners');

  res = await request('GET', '/owners');
  assert('List owners', res.status === 200, `GET /owners → ${res.status}`);

  res = await request('POST', '/owners', { firstName: 'Test', lastName: 'Owner', address: '123 Main St', city: 'Springfield', telephone: '5551234567' });
  assert('Create owner', res.status === 201, `POST /owners → ${res.status}`);
  const ownerId = res.body?.id;
  assert('Create owner - lastName', res.body?.lastName === 'Owner', `lastName=${res.body?.lastName}`);

  res = await request('GET', `/owners/${ownerId}`);
  assert('Get owner by ID', res.status === 200, `GET /owners/${ownerId} → ${res.status}`);

  res = await request('PUT', `/owners/${ownerId}`, { firstName: 'Test', lastName: 'OwnerUpdated', address: '456 Oak Ave', city: 'Shelbyville', telephone: '5559876543' });
  assert('Update owner', res.status === 204, `PUT /owners/${ownerId} → ${res.status}`);

  res = await request('GET', '/owners/999999');
  assert('Get non-existent owner → 404', res.status === 404, `GET /owners/999999 → ${res.status}`);

  res = await request('POST', '/owners', {});
  assert('Create owner invalid → 400', res.status === 400, `POST /owners {} → ${res.status}`);

  // ── Owner → Pet → Visit lifecycle ──
  console.log('\n📋 Owner-Pet-Visit Lifecycle');

  // Create owner for lifecycle
  res = await request('POST', '/owners', { firstName: 'Integration', lastName: 'Tester', address: '789 Test Blvd', city: 'Testville', telephone: '5550001111' });
  assert('Lifecycle: create owner', res.status === 201, `POST /owners → ${res.status}`);
  const lcOwnerId = res.body?.id;

  // Add pet to owner
  res = await request('POST', `/owners/${lcOwnerId}/pets`, { name: 'Buddy', birthDate: '2020-01-15', type: { id: 2, name: 'dog' } });
  assert('Lifecycle: add pet to owner', res.status === 201, `POST /owners/${lcOwnerId}/pets → ${res.status}`);
  const lcPetId = res.body?.id;

  // Add visit to pet
  res = await request('POST', `/owners/${lcOwnerId}/pets/${lcPetId}/visits`, { date: '2025-07-01', description: 'Vaccination appointment' });
  assert('Lifecycle: add visit to pet', res.status === 201, `POST visit → ${res.status}`);

  // Verify owner has pet
  res = await request('GET', `/owners/${lcOwnerId}`);
  assert('Lifecycle: owner has pets', res.status === 200 && res.body?.pets?.length > 0, `pets count=${res.body?.pets?.length}`);

  // ── Visits CRUD ──
  console.log('\n📋 Visits');

  res = await request('GET', '/visits');
  assert('List visits', res.status === 200, `GET /visits → ${res.status}`);

  res = await request('POST', '/visits', { date: '2025-06-01', description: 'Annual checkup', petId: 1 });
  assert('Create visit', res.status === 201, `POST /visits → ${res.status}`);
  const visitId = res.body?.id;

  res = await request('GET', `/visits/${visitId}`);
  assert('Get visit by ID', res.status === 200, `GET /visits/${visitId} → ${res.status}`);

  res = await request('PUT', `/visits/${visitId}`, { id: visitId, date: '2025-06-15', description: 'Follow-up visit', petId: 1 });
  assert('Update visit', res.status === 204, `PUT /visits/${visitId} → ${res.status}`);

  res = await request('GET', '/visits/999999');
  assert('Get non-existent visit → 404', res.status === 404, `GET /visits/999999 → ${res.status}`);

  res = await request('DELETE', `/visits/${visitId}`);
  assert('Delete visit', res.status === 204, `DELETE /visits/${visitId} → ${res.status}`);

  // ── Error endpoint ──
  console.log('\n📋 Error Handling');

  res = await request('GET', '/oops');
  assert('Error endpoint → 500', res.status === 500, `GET /oops → ${res.status}`);

  // ── Pets (standalone) ──
  console.log('\n📋 Pets');

  res = await request('GET', '/pets');
  assert('List pets', res.status === 200, `GET /pets → ${res.status}`);

  res = await request('GET', '/pets/1');
  assert('Get pet by ID', res.status === 200, `GET /pets/1 → ${res.status}`);

  res = await request('GET', '/pets/999999');
  assert('Get non-existent pet → 404', res.status === 404, `GET /pets/999999 → ${res.status}`);

  // ── Print Results ──
  console.log('\n' + '='.repeat(60));
  console.log('\nResults:');
  for (const r of results) {
    const icon = r.status === 'PASS' ? '  PASS' : '  FAIL';
    console.log(`${icon}: ${r.scenario} (${r.detail})`);
  }
  console.log(`\n${passed} passed, ${failed} failed out of ${passed + failed} assertions`);

  if (failed > 0) {
    console.log('\nFailed assertions:');
    for (const r of results.filter(r => r.status === 'FAIL')) {
      console.log(`  - ${r.scenario}: ${r.detail}`);
    }
    process.exit(1);
  }
}

run().catch(err => {
  console.error('Error running validation:', err);
  process.exit(1);
});
