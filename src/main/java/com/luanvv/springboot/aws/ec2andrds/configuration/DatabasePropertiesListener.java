package com.luanvv.springboot.aws.ec2andrds.configuration;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.stereotype.Component;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InvalidParameterException;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luanvv.springboot.aws.ec2andrds.exceptions.DataSourceSecretDoesNotExistException;
import com.luanvv.springboot.aws.ec2andrds.exceptions.DataSourceSecretEmptyOrNullException;
import com.luanvv.springboot.aws.ec2andrds.exceptions.DataSourceSecretInvalidException;

@Profile({ "prod", "stag" })
@Component
public class DatabasePropertiesListener implements ApplicationListener<ApplicationPreparedEvent> {

	private static final String USE_SECRETS = "com.luanvv.springboot.aws.useSecrets";
	private static final String AWS_SECRETS_REGION = "spring.aws.secretsmanager.region";
	private static final String AWS_SECRET_NAME = "spring.aws.secretsmanager.secretName";
	private static final String SPRING_DATASOURCE_USERNAME = "spring.datasource.username";
	private static final String SPRING_DATASOURCE_PASSWORD = "spring.datasource.password";
	private static final String SPRING_DATASOURCE_URL = "spring.datasource.url";

	@Override
	public void onApplicationEvent(ApplicationPreparedEvent event) {
		ConfigurableEnvironment env = event.getApplicationContext().getEnvironment();
		if (isNotUseSecrets(env)) {
			return;
		}
		String secretName = env.getProperty(AWS_SECRET_NAME);
		String region = env.getProperty(AWS_SECRETS_REGION);
		AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard().withRegion(region).build();

		GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
		GetSecretValueResult getSecretValueResponse = getSecretResponse(secretName, client, getSecretValueRequest);
		JsonNode secretsJson = getSecretsJson(getSecretValueResponse);
		String engine = secretsJson.get("engine").textValue();
		String host = secretsJson.get("host").textValue();
		String dbname = secretsJson.get("dbname").textValue();
		String username = secretsJson.get("username").textValue();
		String password = secretsJson.get("password").textValue();
		String url = MessageFormat.format("jdbc:{0}://{1}:3306/{2}?useSSL=false&useUnicode=yes&characterEncoding=UTF-8",
				engine, host, dbname);
		Properties props = new Properties();
		props.put(SPRING_DATASOURCE_USERNAME, username);
		props.put(SPRING_DATASOURCE_PASSWORD, password);
		props.put(SPRING_DATASOURCE_URL, url);
		env.getPropertySources().addFirst(new PropertiesPropertySource(AWS_SECRET_NAME, props));

	}

	private boolean isNotUseSecrets(Environment env) {
		return !"true".equals(env.getProperty(USE_SECRETS));
	}

	private JsonNode getSecretsJson(GetSecretValueResult getSecretValueResponse) {
		String secret = getSecretValueResponse.getSecretString();
		JsonNode secretsJson = null;
		if (secret != null) {
			try {
				secretsJson = new ObjectMapper().readTree(secret);
			} catch (IOException e) {
				throw new DataSourceSecretInvalidException("Exception while retrieving secret values", e);
			}
		} else {
			throw new DataSourceSecretEmptyOrNullException("The Secret String returned is null");
		}
		return secretsJson;
	}

	private GetSecretValueResult getSecretResponse(String secretName, AWSSecretsManager client,
			GetSecretValueRequest getSecretValueRequest) {
		GetSecretValueResult getSecretValueResponse = null;
		try {
			getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
		} catch (ResourceNotFoundException e) {
			throw new DataSourceSecretInvalidException("The requested secret " + secretName + " was not found", e);
		} catch (InvalidRequestException e) {
			throw new DataSourceSecretInvalidException("The request was invalid", e);
		} catch (InvalidParameterException e) {
			throw new DataSourceSecretInvalidException("The request had invalid params", e);
		}
		if (getSecretValueResponse == null) {
			throw new DataSourceSecretDoesNotExistException();
		}
		return getSecretValueResponse;
	}

}
