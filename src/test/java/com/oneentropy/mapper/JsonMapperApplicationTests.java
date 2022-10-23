package com.oneentropy.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.oneentropy.mapper.model.AttributeMap;
import com.oneentropy.mapper.model.MappingConf;
import com.oneentropy.mapper.service.MappingConfReaderService;
import com.oneentropy.mapper.util.MappingUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@SpringBootTest
@Slf4j
class JsonMapperApplicationTests {

	private static final ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private MappingConfReaderService mappingConfReaderService;

	@Test
	void mappingConfDeserialization() throws IOException {
		String path = "mappingConf1.txt";
		String rawData = readFromFile(path);
		MappingConf mappingConf = mappingConfReaderService.parseMappingConf(rawData);
		Assertions.assertEquals(4,mappingConf.getMappings().size());
		List<AttributeMap> attributeMaps = mappingConfReaderService.convertConfToAttributeMap(mappingConf);
		Assertions.assertEquals(4,attributeMaps.size());
	}

	private String readFromFile(String relativePath) throws IOException{
		if(!MappingUtil.hasContent(relativePath))
			throw new IOException("Specify valid resource relative path");
		String path = "./src/test/resources/"+relativePath;
		return Files.readString(Paths.get(path));
	}

}
