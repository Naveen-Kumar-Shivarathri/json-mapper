package com.oneentropy.mapper.interpreters.impl;

import com.oneentropy.mapper.exceptions.InterpretException;
import com.oneentropy.mapper.interpreters.ValueInterpreter;
import com.oneentropy.mapper.model.AttributeMap;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PathInterpreterImpl implements ValueInterpreter {

    @Override
    public String interpret(AttributeMap attributeMap, Map<String, String> data) throws InterpretException {
        return null;
    }

}
