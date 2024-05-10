package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "api_handler",
        roleName = "api_handler-role",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    private static final String TABLE_NAME = "cmtr-8517cab4-Events-test";
    private static final String REGION = "eu-central-1";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> handleRequest(Map<String, Object> request, Context context) {
        try {
            ApiRequestBody apiRequestBody = objectMapper.readValue(objectMapper.writeValueAsString(request), ApiRequestBody.class);
            AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                    .withRegion(REGION)
                    .build();
            DynamoDB dynamoDB = new DynamoDB(client);

            Item item = new Item()
                    .withPrimaryKey("id", UUID.randomUUID().toString())
                    .withNumber("principalId", apiRequestBody.getPrincipalId())
                    .withString("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                    .withMap("body", apiRequestBody.getContent());

            PutItemOutcome putItemOutcome = dynamoDB.getTable(TABLE_NAME).putItem(item);
            return Map.of("statusCode", 201, "event", putItemOutcome);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
