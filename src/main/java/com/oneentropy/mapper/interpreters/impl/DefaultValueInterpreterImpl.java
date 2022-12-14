package com.oneentropy.mapper.interpreters.impl;

import com.oneentropy.mapper.exceptions.InterpretException;
import com.oneentropy.mapper.interpreters.ValueInterpreter;
import com.oneentropy.mapper.model.AttributeMap;
import com.oneentropy.mapper.util.MappingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Qualifier("defaultValueInterpreter")
public class DefaultValueInterpreterImpl implements ValueInterpreter {

    @Override
    public String interpret(AttributeMap attributeMap, Map<String, String> data) throws InterpretException {
        if (!attributeMap.hasDefaultValue())
            return null;

        List<String> tokens = MappingUtil.tokenizeExpression(attributeMap.getDefaultValue());
        return handleExpressionEvaluation(tokens, data, attributeMap);
    }

    private String handleExpressionEvaluation(List<String> exprTokens, Map<String, String> data, AttributeMap attributeMap) {
        if (MappingUtil.isZeroSized(exprTokens))
            return "";
        StringBuilder buffer = new StringBuilder();
        for (String expr : exprTokens) {
            if (MappingUtil.isSourceValue(expr)) {
                String key = extractNonMetaInformation(attributeMap.getOriginalKey());
                if (data.containsKey(key)) {
                    buffer.append(data.get(key) == null ? "" : data.get(key));
                }
            } else {
                buffer.append(extractContentFromQuotations(expr));
            }
        }

        return buffer.toString();
    }

    private String extractContentFromQuotations(String input) {
        if (input.startsWith("\"") && input.endsWith("\""))
            return input.substring(1, input.length() - 1);
        return input;
    }

    private String extractNonMetaInformation(String input) {
        if (input.contains(","))
            return input.substring(0, input.lastIndexOf(","));
        return input;
    }

}
