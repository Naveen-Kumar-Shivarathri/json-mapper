package com.oneentropy.mapper.service;

import com.oneentropy.mapper.model.AttributeMap;
import com.oneentropy.mapper.model.MappingConf;

import java.util.List;

public interface MappingConfReaderService {

    MappingConf parseMappingConf(String conf);

    List<AttributeMap> convertConfToAttributeMap(String conf);

    List<AttributeMap> convertConfToAttributeMap(MappingConf mappingConf);

}
