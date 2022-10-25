package com.oneentropy.mapper.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.oneentropy.mapper.exceptions.InterpretException;
import com.oneentropy.mapper.functional.PostMapFunction;
import com.oneentropy.mapper.functional.PreMapFunction;
import com.oneentropy.mapper.model.AttributeMap;
import com.oneentropy.mapper.model.Data;
import com.oneentropy.mapper.model.MultiLevelData;
import com.oneentropy.mapper.model.SingleLevelData;

import java.util.List;
import java.util.Map;

public interface MappingService {


    JsonNode mapDataToJsonNode(SingleLevelData singleLevelData, List<AttributeMap> attributeMapList, PreMapFunction preMapFunction, PostMapFunction postMapFunction) throws InterpretException;

    JsonNode mapDataToJsonNode(JsonNode template, Data data, int activationLevel, List<AttributeMap> attributeMapList, PreMapFunction preMapFunction, PostMapFunction postMapFunction) throws InterpretException;

    JsonNode mapDataToJsonNode(JsonNode template, SingleLevelData singleLevelData, int activationLevel, List<AttributeMap> attributeMapList, PreMapFunction preMapFunction, PostMapFunction postMapFunction) throws InterpretException;

    JsonNode mapDataToJsonNode(JsonNode template, SingleLevelData singleLevelData, int activationLevel, String mappingConf, PreMapFunction preMapFunction, PostMapFunction postMapFunction) throws InterpretException;

    JsonNode mapDataToJsonNode(SingleLevelData singleLevelData, String mappingConf,PreMapFunction preMapFunction, PostMapFunction postMapFunction) throws InterpretException;

    JsonNode mapDataToJsonNode(Data data, String mappingConf,PreMapFunction preMapFunction, PostMapFunction postMapFunction) throws InterpretException;

    JsonNode mapDataToJsonNode(MultiLevelData multiLevelData, String[] mappingConf, PreMapFunction preMapFunction, PostMapFunction postMapFunction) throws InterpretException;

    JsonNode mapDataToJsonNode(JsonNode template, MultiLevelData multiLevelData, String[] mappingConf, PreMapFunction preMapFunction, PostMapFunction postMapFunction) throws InterpretException;

}
