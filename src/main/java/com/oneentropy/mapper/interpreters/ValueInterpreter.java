package com.oneentropy.mapper.interpreters;

import com.oneentropy.mapper.exceptions.InterpretException;
import com.oneentropy.mapper.model.AttributeMap;

import java.util.Map;

public interface ValueInterpreter {

    String interpret(AttributeMap attributeMap, Map<String, String> data) throws InterpretException;

}
