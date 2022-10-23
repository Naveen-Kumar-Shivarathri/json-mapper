package com.oneentropy.mapper.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.oneentropy.mapper.functional.PostMapFunction;
import com.oneentropy.mapper.functional.PreMapFunction;
import com.oneentropy.mapper.model.AttributeMap;
import com.oneentropy.mapper.service.MappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MappingServiceImpl implements MappingService {

    @Override
    public JsonNode mapDataToJsonNode(List<Map<String, String>> data, List<AttributeMap> attributeMapList, PreMapFunction preMapFunction, PostMapFunction postMapFunction) {

        return null;
    }

    @Override
    public JsonNode mapDataToJsonNode(Map<String, String> data, List<AttributeMap> attributeMapList, PreMapFunction preMapFunction, PostMapFunction postMapFunction) {
        return null;
    }
}
