package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
            try (S3Client s3Client = S3Client.builder().region(Region.EU_CENTRAL_1).build()) {
                String json = new Gson().toJson(Map.of("ids", ids));
                s3Client.putObject(PutObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(filename)
                        .build(), RequestBody.fromString(json));
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return Map.of("statusCode", 201);
    }
}
