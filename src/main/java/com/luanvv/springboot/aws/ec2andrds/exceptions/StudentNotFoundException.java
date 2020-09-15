package com.luanvv.springboot.aws.ec2andrds.exceptions;

public class StudentNotFoundException extends RuntimeException {

	public StudentNotFoundException(String exception) {
		super(exception);
	}

}
