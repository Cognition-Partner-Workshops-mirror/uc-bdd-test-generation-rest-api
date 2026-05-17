package fr.redfroggy.bdd.exceltestrunner.parser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility to parse the Data column value (comma-separated key:'value' pairs)
 * into a Map of field names to their values.
 *
 * @author Ashish,Raut
 */
public class DataColumnParser {

    // Regex pattern matching key:'value' pairs in the Data column
    private static final Pattern DATA_PATTERN = Pattern.compile("(\\w+)\\s*:\\s*'([^']*)'");

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private DataColumnParser() {
    }

    /**
     * Parses a comma-separated string of key:'value' pairs into a Map.
     * Example input: "username:'john', password:'secret123', email:'john@test.com'"
     * Example output: {username=john, password=secret123, email=john@test.com}
     *
     * @param dataColumnValue the raw Data column value from the Excel file
     * @return a LinkedHashMap preserving insertion order of the parsed key-value pairs
     */
    public static Map<String, String> parse(String dataColumnValue) {
        Map<String, String> result = new LinkedHashMap<>();

        if (dataColumnValue == null || dataColumnValue.trim().isEmpty()) {
            return result;
        }

        // Match all key:'value' pairs using regex
        Matcher matcher = DATA_PATTERN.matcher(dataColumnValue);
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String value = matcher.group(2).trim();
            result.put(key, value);
        }

        return result;
    }
}
