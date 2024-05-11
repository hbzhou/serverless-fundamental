package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.StreamRecord;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.syndicate.deployment.annotations.events.DynamoDbEvents;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "audit_producer",
        roleName = "audit_producer-role",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DynamoDbTriggerEventSource(
        targetTable = "Configuration", batchSize = 1)
public class AuditProducer implements RequestHandler<DynamodbEvent, Map<String, Object>> {
    private static final String REGION = "eu-central-1";
    private static final String TABLE_NAME = "cmtr-8517cab4-Audit-test";

    public Map<String, Object> handleRequest(DynamodbEvent request, Context context) {
        System.out.println("Hello from AuditProducer lambda, request: " + request);

        DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.standard()
                .withRegion(REGION)
                .build());

        for (DynamodbEvent.DynamodbStreamRecord record : request.getRecords()) {
            Item item = new Item();
            StreamRecord streamRecord = record.getDynamodb();
            System.out.println(streamRecord);
            item.withPrimaryKey("id", UUID.randomUUID().toString());
            item.withString("itemKey", streamRecord.getKeys().get("key").getS());
            item.withString("modificationTime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            if ("INSERT".equals(record.getEventName())) {
                Map<String, AttributeValue> newImage = streamRecord.getNewImage();
                HashMap<String, Object> newValue = new HashMap<>();
                newValue.put("key", newImage.get("key").getS());
                if (newImage.containsKey("value")) {
                    newValue.put("value", Integer.valueOf(newImage.get("value").getN()));
                }
                item.withMap("newValue", newValue);
            }
            if ("MODIFY".equals(record.getEventName())) {
                item.withString("updatedAttribute", "value");
                item.with("oldValue", streamRecord.getOldImage().get("value").getN());
                item.with("newValue", streamRecord.getNewImage().get("value").getN());
            }
            dynamoDB.getTable(TABLE_NAME).putItem(item);
        }
        return Map.of("statusCode", 201);
    }
}
