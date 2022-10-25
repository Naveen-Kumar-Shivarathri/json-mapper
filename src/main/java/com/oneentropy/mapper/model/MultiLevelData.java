package com.oneentropy.mapper.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiLevelData {

    private List<SingleLevelData> multiLevelData;

}
