package com.luanvv.springboot.aws.ec2andrds.dto;

import com.luanvv.springboot.aws.ec2andrds.entities.Student;

public class StudentDTO {
	private int id;
	private String name;

	public StudentDTO() {
		super();
	}

	public StudentDTO(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Student toBean() {
		return new Student(this.getId(), this.getName());
	}
}
