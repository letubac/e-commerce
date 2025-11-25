package com.ecommerce.util;

import java.io.IOException;
import java.util.Map;

import com.ecommerce.constant.CommonConstant;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import jakarta.transaction.SystemException;

public class CommonJsonUtil {

    /**
     * convertObjectToJSON.
     *
     * @author baclv
     * @param obj type {@link Object}
     * @return {@link String}
     * @throws JsonProcessingException the json processing exception
     */
    public static String convertObjectToJSON(Object obj) throws JsonProcessingException {
        String resultJson = null;
        ObjectWriter mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).setSerializationInclusion(Include.ALWAYS)
                .writer().withDefaultPrettyPrinter();
        resultJson = mapper.writeValueAsString(obj);
        return resultJson;
    }
    
    /**
     * convertJSONToObject.
     *
     * @author baclv
     * @param <T> the generic type
     * @param json                  type String
     * @param valueType                  type Class
     * @return Class
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static <T> T convertJSONToObject(String json, Class<T> valueType) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return (T) mapper.readValue(json, valueType);
    }
    
    /**
     * convertJSONToMap.
     *
     * @author baclv
     * @param json                  type String
     * @return Map
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Map<String, String> convertJSONToMap(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, new TypeReference<Map<String, String>>() {
        });
    }
    
    /**
     * <p>
     * Convert JSON to map object.
     * </p>
     *
     * @param json
     *            type {@link String}
     * @return {@link Map<String,Object>}
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @author baclv
     */
    public static Map<String, Object> convertJSONToMapObject(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, new TypeReference<Map<String, Object>>() {
        });
    }
    
    /**
     * convertObjectToJsonNode.
     *
     * @author baclv
     * @param obj                  type Object
     * @return JsonNode
     */
    public static JsonNode convertObjectToJsonNode(Object obj) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(obj, JsonNode.class);
    }
    
    
	public static String convertObjectToJsonString(Object object) throws SystemException {
		ObjectMapper mapper = new ObjectMapper();

		String result = CommonConstant.EMPTY;

		if (object != null) {
			try {
				result = mapper.writeValueAsString(object);
			} catch (JsonProcessingException e) {
				throw new SystemException("Error method convertObjectToJson :" + e.getMessage());
			}
		}

		return result;
	}
    
}
