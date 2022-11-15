package com.oneentropy.mapper.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MappingConf {

   private List<String> mappings=new ArrayList<>();

}
