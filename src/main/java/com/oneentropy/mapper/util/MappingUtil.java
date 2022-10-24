package com.oneentropy.mapper.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MappingUtil {

    public static final ObjectMapper mapper = new ObjectMapper();

    public static boolean hasContent(Object value) {
        if (value == null)
            return false;
        if (value instanceof String) {
            if (value.equals(""))
                return false;
        }

        return true;
    }

    public static boolean hasContent(Object[] values) {
        for(Object value: values){
            if(!hasContent(value))
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
                if(prevChar==currChar)
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
            if(key!=null)
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

}
