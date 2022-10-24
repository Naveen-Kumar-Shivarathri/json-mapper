package com.oneentropy.mapper.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.oneentropy.mapper.exceptions.InterpretException;
import com.oneentropy.mapper.functional.PostMapFunction;
import com.oneentropy.mapper.functional.PreMapFunction;
import com.oneentropy.mapper.interpreters.ValueInterpreter;
import com.oneentropy.mapper.model.AttributeMap;
import com.oneentropy.mapper.service.MappingService;
import com.oneentropy.mapper.util.MappingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MappingServiceImpl implements MappingService {

    @Autowired
    @Qualifier("attrNameInterpreter")
    private ValueInterpreter attributeNameInterpreter;

    @Autowired
    @Qualifier("defaultValueInterpreter")
    private ValueInterpreter defaultValueInterpreter;


    @Override
    public JsonNode mapDataToJsonNode(List<Map<String, String>> dataList, List<AttributeMap> attributeMapList, PreMapFunction preMapFunction, PostMapFunction postMapFunction) throws InterpretException {

        ObjectNode template = JsonNodeFactory.instance.objectNode();
        int iteration = 0;
        for (Map<String, String> data : dataList) {
            mapDataToJsonNode(template, data, iteration, attributeMapList, preMapFunction, postMapFunction);
            iteration++;
        }
        return template;
    }

    @Override
    public JsonNode mapDataToJsonNode(JsonNode template, Map<String, String> data, int iteration, List<AttributeMap> attributeMapList, PreMapFunction preMapFunction, PostMapFunction postMapFunction) throws InterpretException {
        if (preMapFunction != null)
            preMapFunction.apply(data, attributeMapList);
        if (!MappingUtil.hasContent(template))
            template = JsonNodeFactory.instance.objectNode();

        processMapping((ObjectNode) template, data, iteration, attributeMapList);

        if (postMapFunction != null)
            postMapFunction.apply(template);
        return template;
    }

    private void processMapping(ObjectNode workingCopy, Map<String, String> data, int iteration, List<AttributeMap> attributeMapList) throws InterpretException {

        if (!MappingUtil.hasContent(new Object[]{data, attributeMapList})) {
            throw new InterpretException("Data and Attributes Mapping list are required for processing");
        }

        for (AttributeMap attributeMap : attributeMapList) {
            if (attributeNameInterpreter.interpret(attributeMap, data) != null) {
                String value = defaultValueInterpreter.interpret(attributeMap, data);
                if (value == null) {
                    value = data.get(extractNonMetaInformation(attributeMap.getOriginalKey()));
                }
                if (value != null)
                    insertValue(attributeMap.getPath(), value, iteration, workingCopy, attributeMap.getOriginalKey());
            }
        }

    }

    private void insertValue(List<String> pathTokens, String value, int iteration, ObjectNode workingCopy, String attributeKey) {
        if (!MappingUtil.hasContent(pathTokens)) {
            log.error("Error while inserting data for attribute:{}", attributeKey);
        }

        ObjectNode handle = workingCopy;

        for (int pathIterator = 0; pathIterator < pathTokens.size() - 1; pathIterator++) {
            handle = navigatePath(handle, pathTokens.get(pathIterator), iteration);
        }
        String leafPathToken = pathTokens.get(pathTokens.size() - 1);
        if (MappingUtil.isAnArray(leafPathToken)) {

            insertIntoArray(leafPathToken, value, handle, iteration);
        } else {
            handle.put(leafPathToken, value);
        }

    }

    private ObjectNode navigatePath(ObjectNode workingCopy, String pathToken, int iteration) {

        if (!MappingUtil.isAnArray(pathToken)) {
            if (workingCopy.has(pathToken))
                workingCopy = (ObjectNode) workingCopy.get(pathToken);
            else {
                workingCopy.set(pathToken, JsonNodeFactory.instance.objectNode());
                workingCopy = (ObjectNode) workingCopy.get(pathToken);
            }
        } else {
            workingCopy = navigateArrayPath(pathToken, workingCopy, iteration);
        }
        return workingCopy;
    }

    private ObjectNode navigateArrayPath(String pathToken, ObjectNode workingCopy, int iteration) {
        String key = MappingUtil.getArrayKey(pathToken);
        if (MappingUtil.arrayTokenContainsIndex(pathToken)) {
            int index = MappingUtil.getIndexFromArrayToken(pathToken);
            workingCopy = (ObjectNode) workingCopy.get(key);
            ArrayNode arrayNode = workingCopy.arrayNode();
            if (index != -1) {
                if (index > arrayNode.size()) {
                    for (int indexIterator = arrayNode.size() - 1; indexIterator <= index; indexIterator++)
                        arrayNode.set(index, JsonNodeFactory.instance.objectNode());
                }
                workingCopy = (ObjectNode) arrayNode.get(index);
            } else {
                if (arrayNode.size() > 1)
                    workingCopy = (ObjectNode) arrayNode.get(arrayNode.size() - 1);
                else
                    workingCopy = (ObjectNode) arrayNode.set(0, JsonNodeFactory.instance.objectNode());
            }
        } else {
            workingCopy = (ObjectNode) workingCopy.get(key);
            ArrayNode arrayNode = workingCopy.arrayNode();
            if (arrayNode.size() > 1)
                workingCopy = (ObjectNode) arrayNode.get(arrayNode.size() - 1);
            else
                workingCopy = (ObjectNode) arrayNode.set(0, JsonNodeFactory.instance.objectNode());
        }

        return workingCopy;
    }

    private void insertIntoArray(String pathToken, String value, ObjectNode workingCopy, int iteration) {
        String key = MappingUtil.getArrayKey(pathToken);
        if (MappingUtil.arrayTokenContainsIndex(pathToken)) {
            int index = MappingUtil.getIndexFromArrayToken(pathToken);
            if (!workingCopy.has(key))
                workingCopy.putArray(key);
            ArrayNode arrayNode = (ArrayNode) workingCopy.get(key);
            accomodateIndexInsert(arrayNode, index, value);
        } else {
            ArrayNode arrayNode = null;
            if (workingCopy.has(key))
                arrayNode = (ArrayNode) workingCopy.get(key);
            else {
                arrayNode = workingCopy.putArray(key);
            }
            accomodateIndexInsert(arrayNode, iteration, value);

        }

    }

    private void accomodateIndexInsert(ArrayNode arrayNode, int index, String value) {
        if (index != -1) {
            insertElementsIntoArray(arrayNode, index, value);
        } else {
            insertAtLatestIndex(arrayNode, value);
        }
    }

    private void insertElementsIntoArray(ArrayNode arrayNode, int index, String value) {
        if (index > arrayNode.size()-1) {
            for (int arrayIteration = arrayNode.size(); arrayIteration <= index; arrayIteration++)
                arrayNode.insert(arrayIteration, "");
        }
        arrayNode.set(index, value);
    }

    private void insertAtLatestIndex(ArrayNode arrayNode, String value) {
        if (arrayNode.size() > 1)
            arrayNode.set(arrayNode.size() - 1, value);
        else
            arrayNode.insert(0, value);
    }

    private String extractNonMetaInformation(String input) {
        if (input.contains(","))
            return input.substring(0, input.lastIndexOf(","));
        return input;
    }


}
