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
}
