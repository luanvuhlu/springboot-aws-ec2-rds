package com.luanvv.springboot.aws.ec2andrds.configuration;

import java.io.IOException;
import java.text.MessageFormat;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.env.Environment;
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

@Component
public class DataSourceUtils {

	public enum Route {
		PRIMARY, READ
	}
	
	public DataSource getBySecrets(String secretName, String region) {
		AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard().withRegion(region).build();
		GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
		GetSecretValueResult getSecretValueResponse = getSecretResponse(secretName, client, getSecretValueRequest);
		JsonNode secretsJson = getSecretsJson(getSecretValueResponse);
		String engine = secretsJson.get("engine").textValue();
		String host = secretsJson.get("host").textValue();
		String dbname = secretsJson.get("dbname").textValue();
		String username = secretsJson.get("username").textValue();
		String password = secretsJson.get("password").textValue();
		// Note: additional parameters only applying for MySQL
		String url = MessageFormat.format("jdbc:{0}://{1}:3306/{2}?useSSL=false&useUnicode=yes&characterEncoding=UTF-8",
				engine, host, dbname);
		return DataSourceBuilder.create()
				.username(username)
				.password(password)
				.url(url)
				.build();
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
	
	public DataSource getByProperties(Environment env, Route route) {
		DataSourceBean dataSourceBean = getBeanByRoute(env, route);
		return DataSourceBuilder.create()
				.username(dataSourceBean.getUsername())
				.password(dataSourceBean.getPassword())
				.url(dataSourceBean.getUrl())
				.build();
	}
	
	private DataSourceBean getBeanByRoute(Environment env, Route route) {
		switch (route) {
			case PRIMARY:
				return new DataSourceBean(
						env.getProperty("spring.datasource.primary.url"), 
						env.getProperty("spring.datasource.primary.username"), 
						env.getProperty("spring.datasource.primary.password")
				);
			case READ:
				return new DataSourceBean(
						env.getProperty("spring.datasource.read.url"), 
						env.getProperty("spring.datasource.read.username"), 
						env.getProperty("spring.datasource.read.password")
				);
		}
		throw new IllegalArgumentException("Invalid route");
	}
}
