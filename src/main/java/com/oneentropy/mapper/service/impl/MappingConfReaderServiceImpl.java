package com.oneentropy.mapper.service.impl;

import com.oneentropy.mapper.exceptions.AttributeMappingException;
import com.oneentropy.mapper.model.AttributeMap;
import com.oneentropy.mapper.model.MappingConf;
import com.oneentropy.mapper.service.MappingConfReaderService;
import com.oneentropy.mapper.util.MappingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Service
@Slf4j
public class MappingConfReaderServiceImpl implements MappingConfReaderService {



    @Override
    public MappingConf parseMappingConf(String conf) {
        if(!MappingUtil.hasContent(conf)){
            return new MappingConf();
        }
        String[] mappings = conf.split("\\r|\\n");
        List<String> mappingList = new ArrayList<>();
        Arrays.stream(mappings).filter(token->MappingUtil.hasContent(token)&&!isComment(token)).forEach(mappingList::add);
        return new MappingConf(mappingList);
    }

    @Override
    public List<AttributeMap> convertConfToAttributeMap(String conf) {
        MappingConf mappingConf = parseMappingConf(conf);
        return convertConfToAttributeMap(mappingConf);
    }

    @Override
    public List<AttributeMap> convertConfToAttributeMap(MappingConf mappingConf) {
        List<AttributeMap> attributeMaps = new LinkedList<>();
        for(String attributeMapping: mappingConf.getMappings()){
            List<String> mappingTokens = MappingUtil.tokenizeAttributeMapping(attributeMapping);
            try {
                AttributeMap attributeMap = processAttributeMapping(mappingTokens);
                if(attributeMap!=null)
                    attributeMaps.add(attributeMap);
            } catch (AttributeMappingException e) {
                log.error("Error while processing attribute mapping:{}, error:{}",attributeMapping,e.getMessage());
            }
        }
        return attributeMaps;
    }

    private AttributeMap processAttributeMapping(List<String> mappingTokens) throws AttributeMappingException {
        if(MappingUtil.isZeroSized(mappingTokens)|| mappingTokens.size()<2)
            throw new AttributeMappingException("Expecting at least 2 mapping tokens");
        if(mappingTokens.size()==2){
            List<String> pathTokens = MappingUtil.tokenizePath(mappingTokens.get(1));
            return AttributeMap.builder().originalKey(mappingTokens.get(0)).path(pathTokens).build();
        }else if(mappingTokens.size()==3){
            List<String> pathTokens = MappingUtil.tokenizePath(mappingTokens.get(1));
            return AttributeMap.builder().originalKey(mappingTokens.get(0)).path(pathTokens).defaultValue(mappingTokens.get(2)).build();
        }else{
           throw new AttributeMappingException("Expecting at most 3 mapping tokens, but found:"+mappingTokens.size()+" tokens");
        }

    }

    private boolean isComment(String token){
        return token.startsWith("#");
    }

}
