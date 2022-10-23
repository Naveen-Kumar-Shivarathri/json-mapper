package com.oneentropy.mapper.model;

import lombok.*;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttributeMap {

    private String originalKey;
    private List<String> path=new LinkedList<>();
    private String defaultValue;

    public boolean hasDefaultValue(){
        return this.defaultValue==null;
    }

    public boolean containsExpression(){
        return this.defaultValue.matches(".*\\+.*");
    }
}
