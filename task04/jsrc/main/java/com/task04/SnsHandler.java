package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.syndicate.deployment.annotations.events.SnsEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "sns_handler",
	roleName = "sns_handler-role",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@SnsEventSource(targetTopic = "lambda_topic")
public class SnsHandler implements RequestHandler<SNSEvent, Map<String, Object>> {

	@Override
	public Map<String, Object> handleRequest(SNSEvent input, Context context) {
		input.getRecords().forEach(snsRecord -> {
			SNSEvent.SNS sns = snsRecord.getSNS();
			System.out.println("received sns notification with messageId:" + sns.getMessageId() +",subject:" + sns.getSubject() + ", message:" + sns.getMessage());
		});
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("statusCode", 200);
		resultMap.put("body", "Hello from SNS Lambda");
		return resultMap;
	}
}
