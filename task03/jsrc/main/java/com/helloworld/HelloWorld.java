package com.helloworld;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "hello_world",
	roleName = "hello_world-role",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class HelloWorld implements RequestHandler<Object, Map<String, Object>> {

	public Map<String, Object> handleRequest(Object request, Context context) {
		System.out.println("Hello from lambda");
		Map<String, Object> resultMap = new HashMap<>();
		Map<String, Object> body = Map.of("statusCode", 200, "message", "Hello from Lambda");
		try {
			resultMap.put("body", new ObjectMapper().writeValueAsString(body));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		resultMap.put("statusCode", 200);
		resultMap.put("message", "Hello from Lambda");
		return resultMap;
	}
}
