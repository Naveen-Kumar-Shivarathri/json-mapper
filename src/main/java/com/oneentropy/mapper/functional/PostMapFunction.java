package com.oneentropy.mapper.functional;

import com.fasterxml.jackson.databind.JsonNode;

public interface PostMapFunction<T extends JsonNode> {

    void apply(T mappedData);

}
