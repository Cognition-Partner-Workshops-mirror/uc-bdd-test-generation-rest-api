package fr.redfroggy.bdd.exceltestrunner.parser;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class DataColumnParserTest {

    @Test
    public void shouldParseValidDataColumn() {
        String input = "username:'john', email:'john@test.com', password:'secret123'";
        Map<String, String> result = DataColumnParser.parse(input);
        Assert.assertEquals(3, result.size());
        Assert.assertEquals("john", result.get("username"));
        Assert.assertEquals("john@test.com", result.get("email"));
        Assert.assertEquals("secret123", result.get("password"));
    }

    @Test
    public void shouldReturnEmptyMapForNullInput() {
        Map<String, String> result = DataColumnParser.parse(null);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyMapForEmptyString() {
        Map<String, String> result = DataColumnParser.parse("");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyMapForWhitespaceOnly() {
        Map<String, String> result = DataColumnParser.parse("   ");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void shouldParseSinglePair() {
        Map<String, String> result = DataColumnParser.parse("userId:'42'");
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("42", result.get("userId"));
    }

    @Test
    public void shouldHandleSpacesAroundColons() {
        Map<String, String> result = DataColumnParser.parse("name : 'John Doe' , age : '30'");
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("John Doe", result.get("name"));
        Assert.assertEquals("30", result.get("age"));
    }

    @Test
    public void shouldHandleEmptyValue() {
        Map<String, String> result = DataColumnParser.parse("field:''");
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("", result.get("field"));
    }

    @Test
    public void shouldPreserveInsertionOrder() {
        String input = "first:'a', second:'b', third:'c'";
        Map<String, String> result = DataColumnParser.parse(input);
        String[] keys = result.keySet().toArray(new String[0]);
        Assert.assertEquals("first", keys[0]);
        Assert.assertEquals("second", keys[1]);
        Assert.assertEquals("third", keys[2]);
    }

    @Test
    public void shouldPrepareDataForJsonTemplatePlaceholders() {
        // Simulate Data column → parsed Map → JSON template substitution flow
        // Step 1: Parse the Data column value (as it would come from Excel)
        String dataColumnValue = "username:'john_doe', email:'john@example.com', password:'Pass123!'";
        Map<String, String> parsedData = DataColumnParser.parse(dataColumnValue);

        // Step 2: Verify parsed data matches expected key-value pairs
        Assert.assertEquals(3, parsedData.size());
        Assert.assertEquals("john_doe", parsedData.get("username"));
        Assert.assertEquals("john@example.com", parsedData.get("email"));
        Assert.assertEquals("Pass123!", parsedData.get("password"));

        // Step 3: Simulate JSON template substitution (as StepExecutor.buildRequestBody does)
        String jsonTemplate = "{\"username\": \"${username}\", \"email\": \"${email}\", \"password\": \"${password}\"}";
        String resolvedJson = jsonTemplate;
        for (Map.Entry<String, String> entry : parsedData.entrySet()) {
            resolvedJson = resolvedJson.replace("${" + entry.getKey() + "}", entry.getValue());
        }

        // Step 4: Verify the resolved JSON matches expected output for the endpoint
        String expectedJson = "{\"username\": \"john_doe\", \"email\": \"john@example.com\", \"password\": \"Pass123!\"}";
        Assert.assertEquals(expectedJson, resolvedJson);
    }

    @Test
    public void shouldPrepareDataForUrlPlaceholders() {
        // Simulate Data column → parsed Map → URL placeholder substitution flow
        String dataColumnValue = "userId:'42'";
        Map<String, String> parsedData = DataColumnParser.parse(dataColumnValue);

        // Simulate URL endpoint substitution (as StepExecutor.buildUrl does)
        String endpointTemplate = "/api/users/{userId}";
        String resolvedEndpoint = endpointTemplate;
        for (Map.Entry<String, String> entry : parsedData.entrySet()) {
            resolvedEndpoint = resolvedEndpoint.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        // Verify the resolved URL matches expected endpoint
        Assert.assertEquals("/api/users/42", resolvedEndpoint);
    }

    @Test
    public void shouldPrepareDataForBothUrlAndJsonPlaceholders() {
        // Simulate a step that uses data for both URL path and JSON body
        String dataColumnValue = "userId:'10', firstName:'Jane', lastName:'Smith'";
        Map<String, String> parsedData = DataColumnParser.parse(dataColumnValue);

        // URL substitution
        String endpointTemplate = "/api/users/{userId}";
        String resolvedEndpoint = endpointTemplate;
        for (Map.Entry<String, String> entry : parsedData.entrySet()) {
            resolvedEndpoint = resolvedEndpoint.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        Assert.assertEquals("/api/users/10", resolvedEndpoint);

        // JSON body substitution
        String jsonTemplate = "{\"firstName\": \"${firstName}\", \"lastName\": \"${lastName}\"}";
        String resolvedJson = jsonTemplate;
        for (Map.Entry<String, String> entry : parsedData.entrySet()) {
            resolvedJson = resolvedJson.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        Assert.assertEquals("{\"firstName\": \"Jane\", \"lastName\": \"Smith\"}", resolvedJson);
    }

    @Test
    public void shouldHandleSpecialCharactersInDataForJson() {
        // Verify that special characters in data values are preserved for JSON
        String dataColumnValue = "description:'Hello World & Co.', status:'active'";
        Map<String, String> parsedData = DataColumnParser.parse(dataColumnValue);

        Assert.assertEquals("Hello World & Co.", parsedData.get("description"));
        Assert.assertEquals("active", parsedData.get("status"));

        // JSON substitution preserves special characters
        String jsonTemplate = "{\"description\": \"${description}\", \"status\": \"${status}\"}";
        String resolvedJson = jsonTemplate;
        for (Map.Entry<String, String> entry : parsedData.entrySet()) {
            resolvedJson = resolvedJson.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        Assert.assertEquals("{\"description\": \"Hello World & Co.\", \"status\": \"active\"}", resolvedJson);
    }
}
