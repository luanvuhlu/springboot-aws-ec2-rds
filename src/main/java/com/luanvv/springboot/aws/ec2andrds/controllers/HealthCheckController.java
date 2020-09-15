package com.luanvv.springboot.aws.ec2andrds.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
	
	@GetMapping({ "/healthcheck", "/" })
	public String retrieveAllStudents() {
		return "OK";
	}
}
