package com.oneentropy.mapper.model;

import lombok.*;

import java.util.Map;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Data {

    private Map<String, String> data;

}
