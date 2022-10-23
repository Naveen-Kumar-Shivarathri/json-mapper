package com.oneentropy.mapper.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.oneentropy.mapper.functional.PostMapFunction;
import com.oneentropy.mapper.functional.PreMapFunction;
import com.oneentropy.mapper.model.AttributeMap;

import java.util.List;
import java.util.Map;

public interface MappingService {


    JsonNode mapDataToJsonNode(List<Map<String, String>> data, List<AttributeMap> attributeMapList, PreMapFunction preMapFunction, PostMapFunction postMapFunction);

    JsonNode mapDataToJsonNode(Map<String, String> data, List<AttributeMap> attributeMapList, PreMapFunction preMapFunction, PostMapFunction postMapFunction);


}
