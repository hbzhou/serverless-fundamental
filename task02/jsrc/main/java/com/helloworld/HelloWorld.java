package com.helloworld;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "task02",
	roleName = "hello_world-role",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class HelloWorld implements RequestHandler<Map<String, Object>, Map<String, Object>> {

	public Map<String, Object> handleRequest(Map<String, Object> request, Context context) {
		if (request.get("rawPath").equals("/hello")){
			System.out.println("Hello from lambda");
			Map<String, Object> resultMap = new HashMap<>();
			Map<String, Object> body = Map.of("statusCode", 200, "message", "Hello from Lambda");
			try {
				resultMap.put("body", new ObjectMapper().writeValueAsString(body));
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
			resultMap.put("statusCode", 200);
			return resultMap;
		}
		return Map.of("statusCode", 404);
	}
}
