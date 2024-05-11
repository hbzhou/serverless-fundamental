package com.task07;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@LambdaHandler(lambdaName = "uuid_generator",
        roleName = "uuid_generator-role",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@RuleEventSource(
        targetRule = "uuid_trigger"
)
public class UuidGenerator implements RequestHandler<Object, Map<String, Object>> {
    private static final String REGION = "eu-central-1";
    private static final String BUCKET_NAME = "cmtr-8517cab4-uuid-storage-test";

    public Map<String, Object> handleRequest(Object request, Context context) {
        System.out.println("Hello from UuidGenerator lambda");
        String filename = DateTimeFormatter.ISO_INSTANT.format(LocalDateTime.now().atZone(ZoneId.systemDefault()));
        List<String> ids = IntStream.range(1, 10)
                .mapToObj(i -> UUID.randomUUID().toString())
                .collect(Collectors.toList());
        try {
            byte[] bytes = new Gson().toJson(Map.of("ids", ids)).getBytes(StandardCharsets.UTF_8);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(REGION).build();
            s3.putObject(BUCKET_NAME, filename, inputStream, new ObjectMetadata());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return Map.of("statusCode", 201);
    }
}
