package com.oneentropy.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.oneentropy.mapper.exceptions.InterpretException;
import com.oneentropy.mapper.model.AttributeMap;
import com.oneentropy.mapper.model.MappingConf;
import com.oneentropy.mapper.service.MappingConfReaderService;
import com.oneentropy.mapper.service.MappingService;
import com.oneentropy.mapper.util.MappingUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@SpringBootTest
@Slf4j
class JsonMapperApplicationTests {

	private static final ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private MappingConfReaderService mappingConfReaderService;

	@Autowired
	private MappingService mappingService;

	@Test
	public void mappingConfDeserialization() throws IOException {
		String path = "mappingConf1.txt";
		String rawData = readFromFile(path);
		MappingConf mappingConf = mappingConfReaderService.parseMappingConf(rawData);
		Assertions.assertEquals(4,mappingConf.getMappings().size());
		List<AttributeMap> attributeMaps = mappingConfReaderService.convertConfToAttributeMap(mappingConf);
		Assertions.assertEquals(4,attributeMaps.size());
	}

	@Test
	public void testDataMapping() throws IOException, InterpretException {

		String path = "mappingConf1.txt";
		String rawData = readFromFile(path);
		MappingConf mappingConf = mappingConfReaderService.parseMappingConf(rawData);
		List<AttributeMap> attributeMaps = mappingConfReaderService.convertConfToAttributeMap(mappingConf);
		Map<String, String> data = new HashMap<>();
		data.put("productName","Product-1");
		data.put("version","1.0.0");
		data.put("createdBy","person-1");
		data.put("modifiedBy","person-2");
		JsonNode node = mappingService.mapDataToJsonNode(null,data,-1,attributeMaps,null,null);
		Assertions.assertEquals("Product-1",node.get("gcp").get("product").get("name").asText());

	}

	@Test
	public void testObjectNode(){
		ObjectNode node = JsonNodeFactory.instance.objectNode();
		ObjectNode workingCopy = node;
		workingCopy.set("a",JsonNodeFactory.instance.objectNode());

	}

	private String readFromFile(String relativePath) throws IOException{
		if(!MappingUtil.hasContent(relativePath))
			throw new IOException("Specify valid resource relative path");
		String path = "./src/test/resources/"+relativePath;
		return Files.readString(Paths.get(path));
	}

}