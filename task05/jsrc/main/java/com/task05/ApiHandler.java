package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "api_handler",
	roleName = "api_handler-role",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<APIGatewayV2ProxyRequestEvent, Map<String, Object>> {

	private static final String TABLE_NAME = "Events";
	private final AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard().build();
	private final ObjectMapper objectMapper = new ObjectMapper();

	public Map<String, Object> handleRequest(APIGatewayV2ProxyRequestEvent request, Context context) {
		String body = request.getBody();
		try {
			ApiRequestBody apiRequestBody = objectMapper.readValue(body, ApiRequestBody.class);
			PutItemRequest putItemRequest = new PutItemRequest();
			putItemRequest.setTableName(TABLE_NAME);
			putItemRequest.setItem(Map.of(
					"id", new AttributeValue(UUID.randomUUID().toString()),
					"principalId", new AttributeValue(String.valueOf(apiRequestBody.getPrincipalId())),
					"content",  new AttributeValue(objectMapper.writeValueAsString(apiRequestBody.getContent())),
					"createdAt", new AttributeValue(String.valueOf(LocalDateTime.now()))
					)
			);
			PutItemResult putItemResult = amazonDynamoDB.putItem(putItemRequest);
			return Map.of("statusCode", 201, "event", putItemResult.getAttributes());
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
