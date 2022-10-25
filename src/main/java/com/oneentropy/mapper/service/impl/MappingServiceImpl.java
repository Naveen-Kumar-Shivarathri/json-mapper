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
import com.oneentropy.mapper.model.Data;
import com.oneentropy.mapper.model.MultiLevelData;
import com.oneentropy.mapper.model.SingleLevelData;
import com.oneentropy.mapper.service.MappingConfReaderService;
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

    @Autowired
    private MappingConfReaderService mappingConfReaderService;


    @Override
    public JsonNode mapDataToJsonNode(SingleLevelData singleLevelData, List<AttributeMap> attributeMapList, PreMapFunction preMapFunction, PostMapFunction postMapFunction) throws InterpretException {

        ObjectNode template = JsonNodeFactory.instance.objectNode();
        int iteration = 0;
        for (Data data : singleLevelData.getSingleLevelData()) {
            mapDataToJsonNode(template, data, 0, attributeMapList, preMapFunction, postMapFunction);
            iteration++;
        }
        return template;
    }

    @Override
    public JsonNode mapDataToJsonNode(JsonNode template, SingleLevelData singleLevelData, int activationLevel, List<AttributeMap> attributeMapList, PreMapFunction preMapFunction, PostMapFunction postMapFunction) throws InterpretException {

        for (Data data : singleLevelData.getSingleLevelData()) {
            mapDataToJsonNode(template, data, activationLevel, attributeMapList, preMapFunction, postMapFunction);
        }
        return template;
    }

    @Override
    public JsonNode mapDataToJsonNode(JsonNode template, SingleLevelData singleLevelData, int activationLevel, String mappingConf, PreMapFunction preMapFunction, PostMapFunction postMapFunction) throws InterpretException {
        List<AttributeMap> attributeMaps = mappingConfReaderService.convertConfToAttributeMap(mappingConf);
        return mapDataToJsonNode(template,singleLevelData,activationLevel,attributeMaps,preMapFunction,postMapFunction);
    }

    @Override
    public JsonNode mapDataToJsonNode(JsonNode template, Data data, int activationLevel, List<AttributeMap> attributeMapList, PreMapFunction preMapFunction, PostMapFunction postMapFunction) throws InterpretException {
        if (preMapFunction != null)
            preMapFunction.apply(data.getData(), attributeMapList);
        if (!MappingUtil.hasContent(template))
            template = JsonNodeFactory.instance.objectNode();

        processMapping((ObjectNode) template, data.getData(), activationLevel, attributeMapList);

        if (postMapFunction != null)
            postMapFunction.apply(template);
        return template;
    }

    @Override
    public JsonNode mapDataToJsonNode(SingleLevelData singleLevelData, String mappingConf, PreMapFunction preMapFunction, PostMapFunction postMapFunction) throws InterpretException {
        List<AttributeMap> attributeMaps = mappingConfReaderService.convertConfToAttributeMap(mappingConf);
        return mapDataToJsonNode(singleLevelData, attributeMaps, preMapFunction, postMapFunction);
    }

    @Override
    public JsonNode mapDataToJsonNode(Data data, String mappingConf, PreMapFunction preMapFunction, PostMapFunction postMapFunction) throws InterpretException {
        List<AttributeMap> attributeMaps = mappingConfReaderService.convertConfToAttributeMap(mappingConf);
        return mapDataToJsonNode(null, data, -1, attributeMaps, preMapFunction, postMapFunction);
    }

    @Override
    public JsonNode mapDataToJsonNode(MultiLevelData multiLevelData, String[] mappingConf, PreMapFunction preMapFunction, PostMapFunction postMapFunction) throws InterpretException {

        if (!MappingUtil.hasContent(mappingConf))
            throw new InterpretException("Mapping Conf cannot have null values");

        ObjectNode template = JsonNodeFactory.instance.objectNode();
        int activationLevel = 0;
        for (SingleLevelData singleLevelData : multiLevelData.getMultiLevelData()) {
            List<AttributeMap> attributeMaps = mappingConfReaderService.convertConfToAttributeMap(mappingConf[activationLevel]);
            mapDataToJsonNode(template, singleLevelData, activationLevel, attributeMaps, preMapFunction, postMapFunction);
            activationLevel++;
        }
        return template;

    }

    @Override
    public JsonNode mapDataToJsonNode(JsonNode template, MultiLevelData multiLevelData, String[] mappingConf, PreMapFunction preMapFunction, PostMapFunction postMapFunction) throws InterpretException {
        if (!MappingUtil.hasContent(mappingConf))
            throw new InterpretException("Mapping Conf cannot have null values");
        if (template == null)
            template = JsonNodeFactory.instance.objectNode();
        int activationLevel = 0;
        for (SingleLevelData singleLevelData : multiLevelData.getMultiLevelData()) {
            List<AttributeMap> attributeMaps = mappingConfReaderService.convertConfToAttributeMap(mappingConf[activationLevel]);
            mapDataToJsonNode(template, singleLevelData, activationLevel, attributeMaps, preMapFunction, postMapFunction);
            activationLevel++;
        }
        return template;
    }

    private void processMapping(ObjectNode workingCopy, Map<String, String> data, int activationLevel, List<AttributeMap> attributeMapList) throws InterpretException {

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
                    insertValue(attributeMap.getPath(), value, activationLevel, workingCopy, attributeMap.getOriginalKey());
            }
        }

    }

    private void insertValue(List<String> pathTokens, String value, int activationLevel, ObjectNode workingCopy, String attributeKey) {
        if (!MappingUtil.hasContent(pathTokens)) {
            log.error("Error while inserting data for attribute:{}", attributeKey);
        }

        ObjectNode handle = workingCopy;
        int currentLevel = -1;

        for (int pathIterator = 0; pathIterator < pathTokens.size() - 1; pathIterator++) {
            boolean levelActivated = false;
            if (MappingUtil.isAnArray(pathTokens.get(pathIterator)) && !MappingUtil.arrayTokenContainsIndex(pathTokens.get(pathIterator)))
                currentLevel++;
            if (currentLevel == activationLevel)
                levelActivated = true;
            handle = navigatePath(handle, pathTokens.get(pathIterator), levelActivated);
        }
        String leafPathToken = pathTokens.get(pathTokens.size() - 1);
        if (MappingUtil.isAnArray(leafPathToken)) {
            insertIntoArray(leafPathToken, value, handle);
        } else {
            handle.put(leafPathToken, value);
        }

    }

    private ObjectNode navigatePath(ObjectNode workingCopy, String pathToken, boolean levelActivated) {

        if (!MappingUtil.isAnArray(pathToken)) {
            if (!workingCopy.has(pathToken))
                workingCopy.set(pathToken, JsonNodeFactory.instance.objectNode());
            workingCopy = (ObjectNode) workingCopy.get(pathToken);
        } else {
            workingCopy = navigateArrayPath(pathToken, workingCopy, levelActivated);
        }
        return workingCopy;
    }

    private ObjectNode navigateArrayPath(String pathToken, ObjectNode workingCopy, boolean levelActivated) {
        String key = MappingUtil.getArrayKey(pathToken);
        if (MappingUtil.arrayTokenContainsIndex(pathToken)) {
            int index = MappingUtil.getIndexFromArrayToken(pathToken);
            if (!workingCopy.has(key))
                workingCopy.putArray(key);
            ArrayNode arrayNode = (ArrayNode) workingCopy.get(key);
            if (index != -1) {
                if (index > arrayNode.size() - 1) {
                    for (int indexIterator = arrayNode.size(); indexIterator <= index; indexIterator++)
                        arrayNode.insert(index, JsonNodeFactory.instance.objectNode());
                }
                workingCopy = (ObjectNode) arrayNode.get(index);
            } else {
                if (arrayNode.size() > 0) {
                    arrayNode.insert(arrayNode.size(), JsonNodeFactory.instance.objectNode());
                    workingCopy = (ObjectNode) arrayNode.get(arrayNode.size() - 1);
                } else {
                    arrayNode.insert(0, JsonNodeFactory.instance.objectNode());
                    workingCopy = (ObjectNode) arrayNode.get(0);
                }
            }
        } else {
            if (!workingCopy.has(key))
                workingCopy.putArray(key);
            ArrayNode arrayNode = (ArrayNode) workingCopy.get(key);
            if (arrayNode.size() > 0) {
                if (levelActivated)
                    arrayNode.insert(arrayNode.size(), JsonNodeFactory.instance.objectNode());
                workingCopy = (ObjectNode) arrayNode.get(arrayNode.size() - 1);
            } else {
                arrayNode.insert(0, JsonNodeFactory.instance.objectNode());
                workingCopy = (ObjectNode) arrayNode.get(0);
            }

        }

        return workingCopy;
    }

    private void insertIntoArray(String pathToken, String value, ObjectNode workingCopy) {
        String key = MappingUtil.getArrayKey(pathToken);
        if (MappingUtil.arrayTokenContainsIndex(pathToken)) {
            int index = MappingUtil.getIndexFromArrayToken(pathToken);
            if (!workingCopy.has(key))
                workingCopy.putArray(key);
            ArrayNode arrayNode = (ArrayNode) workingCopy.get(key);
            accommodateIndexInsert(arrayNode, index, value);
        } else {
            ArrayNode arrayNode = null;
            if (workingCopy.has(key))
                arrayNode = (ArrayNode) workingCopy.get(key);
            else {
                arrayNode = workingCopy.putArray(key);
            }
            accommodateIndexInsert(arrayNode, -1, value);

        }

    }

    private void accommodateIndexInsert(ArrayNode arrayNode, int index, String value) {
        if (index != -1) {
            insertElementsIntoArray(arrayNode, index, value);
        } else {
            insertAtLatestIndex(arrayNode, value);
        }
    }

    private void insertElementsIntoArray(ArrayNode arrayNode, int index, String value) {
        if (index > arrayNode.size() - 1) {
            for (int arrayIteration = arrayNode.size(); arrayIteration <= index; arrayIteration++)
                arrayNode.insert(arrayIteration, "");
        }
        arrayNode.set(index, value);
    }

    private void insertAtLatestIndex(ArrayNode arrayNode, String value) {
        if (arrayNode.size() > 0) {
            arrayNode.insert(arrayNode.size(), value);
            arrayNode.set(arrayNode.size() - 1, value);
        } else
            arrayNode.insert(0, value);
    }

    private String extractNonMetaInformation(String input) {
        if (input.contains(","))
            return input.substring(0, input.lastIndexOf(","));
        return input;
    }


}
