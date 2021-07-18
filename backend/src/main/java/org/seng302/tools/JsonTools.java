package org.seng302.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.minidev.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonTools {

    private JsonTools() {}

    /**
     * This method identifies all the keys in the JSON object for which the value is null removes those key-value pairs
     * from the JSONObject.
     * @param json A JSON to remove key-value pairs with null values from.
     */
    public static void removeNullsFromJson(JSONObject json) {
        List<String> keysToRemove = new ArrayList<>();
        for (Map.Entry<String, Object> entry : json.entrySet()) {
            if (entry.getValue() == null) {
                keysToRemove.add(entry.getKey());
            }
        }
        for (String key : keysToRemove) {
            json.remove(key);
        }
    }

    /**
     * This method will return the field with the given name from the given json as a long if it can be converted to
     * that format, or will throw a bad request exception if the field cannot be converted to that format.
     * @param json The JSONObject to retrieve the field from.
     * @param fieldName The name of the field to retrieve.
     * @return The value from the field
     */
    public static long parseLongFromJsonField(JSONObject json, String fieldName) {
        try {
            if (json.containsKey(fieldName)) {
                return Long.parseLong(json.getAsString(fieldName));
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("%s is not present", fieldName));
            }
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("%s must be a number", fieldName));
        }
    }

    /**
     * This method will return the field with the given name from the given json as a long[] if it can be converted to
     * that format, or will throw a bad request exception if the field cannot be converted to that format.
     * @param json The JSONObject to retrieve the field from.
     * @param fieldName The name of the field to retrieve.
     * @return The value from the field
     */
    public static long[] parseLongArrayFromJsonField(JSONObject json, String fieldName) {
        ResponseStatusException invalidFormatException = new ResponseStatusException(HttpStatus.BAD_REQUEST,
                String.format("%s must be an array of numbers", fieldName));
        ResponseStatusException notPresentException = new ResponseStatusException(HttpStatus.BAD_REQUEST,
                String.format("%s is not present", fieldName));

        try {
            final ObjectNode node = new ObjectMapper().readValue(json.toJSONString(), ObjectNode.class);
            if (node.has(fieldName)) {
                JsonNode value = node.get(fieldName);
                if (value.getNodeType() != JsonNodeType.ARRAY) {
                    throw invalidFormatException;
                }
                int arrayLength = value.size();
                long[] longArray = new long[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    if (value.get(i).getNodeType() != JsonNodeType.NUMBER) {
                        throw invalidFormatException;
                    }
                    longArray[i] = value.get(i).asLong();
                }
                return longArray;
            } else {
                throw notPresentException;
            }
        } catch (JsonProcessingException e) {
            throw invalidFormatException;
        }
    }
}
