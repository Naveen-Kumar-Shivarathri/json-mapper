package com.oneentropy.mapper.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.oneentropy.mapper.exceptions.InterpretException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;

import javax.naming.spi.ObjectFactory;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MappingUtil {

    public static final ObjectMapper mapper = new ObjectMapper();

    public static final String GROUP_SUBSTITUTION_REGX = "(\\((\\d+)\\))(\\[\\d*\\])?";

    public static boolean hasContent(Object value) {
        if (value == null)
            return false;
        if (value instanceof String) {
            if (value.equals(""))
                return false;
        } else if (value instanceof Set) {
            if (((Set) value).size() == 0)
                return false;
        }

        return true;
    }

    public static boolean hasContent(Object[] values) {
        for (Object value : values) {
            if (!hasContent(value))
                return false;
        }
        return true;
    }

    public static boolean isZeroSized(Object value) {
        if (value == null)
            return true;
        if (value instanceof List) {
            if (((List) value).size() < 1)
                return true;
        }
        return false;
    }

    public static List<String> tokenizeAttributeMapping(String attributeMapping) {
        if (!hasContent(attributeMapping))
            return new LinkedList<>();
        if (attributeMapping.contains("\\="))
            return safeTokenize(attributeMapping);

        return tokenize(attributeMapping, "=");
    }

    public static List<String> safeTokenize(String attributeMapping) {
        char prevChar = '\0';
        List<String> tokens = new LinkedList<>();
        StringBuilder buffer = new StringBuilder();
        for (char currChar : attributeMapping.toCharArray()) {
            if (currChar == '=') {
                if (prevChar != '\\') {
                    tokens.add(buffer.toString().trim());
                    buffer = new StringBuilder();
                } else {
                    buffer.append(currChar);
                }
            } else if (currChar == '\\') {
                if (prevChar == currChar)
                    buffer.append(currChar);
                else
                    prevChar = currChar;
            } else {
                prevChar = currChar;
                buffer.append(currChar);
            }
        }
        tokens.add(buffer.toString().trim());
        return tokens;
    }

    public static List<String> tokenize(String value, String regx) {
        List<String> tokens = new LinkedList<>();
        if (!hasContent(value))
            return tokens;
        String[] pathTokens = value.split(regx, -1);
        if (pathTokens == null) {
            log.error("Value cannot be tokenized, check value {}", value);
            return tokens;
        }
        Arrays.stream(pathTokens).map(token -> token.trim()).forEach(tokens::add);
        return tokens;
    }

    public static boolean isAnArray(String token) {
        return token.matches(".*\\[\\d*\\]");
    }

    public static boolean arrayTokenContainsIndex(String token) {
        return token.matches(".*\\[\\d\\]");
    }

    public static int getIndexFromArrayToken(String token) {
        Pattern pattern = Pattern.compile(".*\\[(\\d)\\]");//NOSONAR Input is config driven
        Matcher matcher = pattern.matcher(token);
        if (matcher.matches()) {
            String rawIndex = matcher.group(1);
            if (hasContent(rawIndex))
                return Integer.parseInt(rawIndex);

        }

        return -1;
    }

    public static String getArrayKey(String token) {
        Pattern pattern = Pattern.compile("(.*)\\[\\d*\\]");//NOSONAR Input is config driven
        Matcher matcher = pattern.matcher(token);
        if (matcher.matches()) {
            String key = matcher.group(1);
            if (key != null)
                return key;

        }

        return "";
    }

    public static List<String> tokenizeExpression(String expression) {
        return tokenize(expression, "\\+");
    }

    public static boolean isSourceValue(String expressionToken) {
        return expressionToken.matches("\\$srcValue");
    }

    public static List<String> tokenizePath(String path) {
        return tokenize(path, "\\.");
    }

    public static String payloadFromJsonNode(JsonNode mappedData) {
        if (!MappingUtil.hasContent(mappedData))
            return "";
        try {
            return MappingUtil.mapper.writeValueAsString(mappedData);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return "";
        }
    }

    public static String extractNonMetaInformation(String input) {
        if (input.contains(","))
            return input.substring(0, input.lastIndexOf(","));
        return input;
    }

    public static String extractMetaInformation(String input) {
        if (input.contains(",")) {
            return input.substring(input.lastIndexOf(",") + 1);
        }
        return "";
    }

    public static Set<Integer> determineGroupIndicesFromPath(List<String> pathTokens) throws InterpretException {
        Set<Integer> groupIndices = new HashSet<>();
        if (!hasContent(pathTokens))
            return groupIndices;
        Pattern pattern = Pattern.compile(GROUP_SUBSTITUTION_REGX);
        Matcher matcher = null;
        for (String pathToken : pathTokens) {
            matcher = pattern.matcher(pathToken);
            if (matcher.matches()) {
                try {
                    int groupIndex = Integer.parseInt(matcher.group(2));
                    groupIndices.add(groupIndex);
                } catch (NumberFormatException e) {
                    throw new InterpretException("Expecting a valid group number, but found:" + matcher.group(2) + ", for path:" + pathToken);
                }
            }
        }
        return groupIndices;
    }

    public static void validateGroupIndices(Matcher matcher, Set<Integer> indices) throws InterpretException {
        if (!MappingUtil.hasContent(indices))
            return;
        for (Integer index : indices) {
            if (!MappingUtil.hasContent(matcher.group(index)))
                throw new InterpretException("No value found at group index:" + index);
        }
    }

    public static List<String> substituteGroupsInPathTokens(String attribute, Matcher matcher, List<String> pathTokens) {
        Pattern pattern = Pattern.compile(GROUP_SUBSTITUTION_REGX);
        Matcher patternMatcher = null;
        List<String> alteredPathTokens = new LinkedList<>();
        for (String pathToken : pathTokens) {
            if (pathToken.equals("*")) {
                alteredPathTokens.add(attribute);
                continue;
            }
            patternMatcher = pattern.matcher(pathToken);
            if (patternMatcher.matches()) {
                int index = Integer.parseInt(patternMatcher.group(2));
                String groupValue = matcher.group(index);
                if (patternMatcher.group(3) != null) {
                    groupValue += patternMatcher.group(3);
                }
                alteredPathTokens.add(groupValue);
            } else
                alteredPathTokens.add(pathToken);
        }
        return alteredPathTokens;
    }

    public static boolean isJsonArray(String value) {
        try {
            if (new JSONArray(value) != null)
                return true;
            return false;
        } catch (JSONException e) {
            return false;
        }

    }

    public static JsonNode parseValueAsTree(String value){
        try{
            return mapper.readTree(value);
        }catch(JsonProcessingException e){
            return JsonNodeFactory.instance.arrayNode();
        }
    }

    public static ArrayNode parseValueAsArrayNode(String value) {
        ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
        try {
            JSONArray array = new JSONArray(value);
            for (int elementIterator = 0; elementIterator < array.length(); elementIterator++) {
                arrayNode.insert(elementIterator, array.get(elementIterator).toString());
            }
        } catch (JSONException e) {
            log.error("Error while parsing JSONArray:{}",e.getMessage());
        }
        return arrayNode;
    }


}
