package com.oneentropy.mapper.functional;

import com.oneentropy.mapper.model.AttributeMap;

import java.util.List;
import java.util.Map;

public interface PreMapFunction<T extends Map<String,String>, U extends List<AttributeMap>> {

    void apply(T data, U attributeMaps);

}
