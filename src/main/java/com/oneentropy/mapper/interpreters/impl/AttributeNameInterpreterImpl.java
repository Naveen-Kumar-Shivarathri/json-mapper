package com.oneentropy.mapper.interpreters.impl;

import com.oneentropy.mapper.exceptions.InterpretException;
import com.oneentropy.mapper.exceptions.MandatoryAttributeException;
import com.oneentropy.mapper.interpreters.ValueInterpreter;
import com.oneentropy.mapper.model.AttributeMap;
import com.oneentropy.mapper.util.MappingUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Qualifier("attrNameInterpreter")
public class AttributeNameInterpreterImpl implements ValueInterpreter {

    private static final String MANDATORY_ATTRIBUTE = "mandatory";

    @Override
    public String interpret(AttributeMap attributeMap, Map<String, String> data) throws InterpretException {

        String meta = extractMetaInformation(attributeMap.getOriginalKey());
        if (MappingUtil.hasContent(meta)) {
            return handleMetaProcessing(attributeMap.getOriginalKey(), meta, data);
        }
        return attributeMap.getOriginalKey();
    }

    private String handleMetaProcessing(String input, String meta, Map<String, String> data) throws MandatoryAttributeException {

        if (meta.equals(MANDATORY_ATTRIBUTE)) {
            String attrKey = extractNonMetaInformation(input);
            if (!data.containsKey(attrKey)) {
                throw new MandatoryAttributeException("Excepting a valid value for attribute:" + attrKey);
            }
            return input;
        }
        return input;
    }

    private String extractNonMetaInformation(String input) {
        if (input.contains(","))
            return input.substring(0, input.lastIndexOf(","));
        return input;
    }

    private String extractMetaInformation(String input) {
        if (input.contains(",")) {
            return input.substring(input.lastIndexOf(","));
        }
        return "";
    }
}
