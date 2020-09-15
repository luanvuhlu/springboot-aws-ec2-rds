package com.luanvv.springboot.aws.ec2andrds.exceptions;

public class DataSourceSecretEmptyOrNullException extends DataSourceSecretInvalidException {

	public DataSourceSecretEmptyOrNullException(String message) {
		super(message);
	}

}
