package com.luanvv.springboot.aws.ec2andrds.controllers;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.luanvv.springboot.aws.ec2andrds.entities.Student;
import com.luanvv.springboot.aws.ec2andrds.exceptions.StudentNotFoundException;
import com.luanvv.springboot.aws.ec2andrds.repositories.StudentRepository;

@RestController
public class StudentController {

	@Autowired
	private StudentRepository studentRepository;

	@GetMapping("/students")
	public List<Student> retrieveAllStudents() {
		return studentRepository.findAll();
	}

	@GetMapping("/student/{id}")
	public Student retrieveStudent(@PathVariable int id) {
		return studentRepository.findById(id)
				.orElseThrow(() -> new StudentNotFoundException("id-" + id));
	}

	@DeleteMapping("/student/{id}")
	public void deleteStudent(@PathVariable int id) {
		studentRepository.deleteById(id);
	}

	@PostMapping("/student")
	public ResponseEntity<Object> createStudent(@RequestBody Student student) {
		Student savedStudent = studentRepository.save(student);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(savedStudent.getId()).toUri();
		return ResponseEntity.created(location).build();

	}
	
	@PutMapping("/student/{id}")
	public ResponseEntity<Object> updateStudent(@RequestBody Student student, @PathVariable int id) {
		Optional<Student> studentOptional = studentRepository.findById(id);
		if (!studentOptional.isPresent()) {
			return ResponseEntity.notFound().build();
		}
		student.setId(id);
		studentRepository.save(student);
		return ResponseEntity.noContent().build();
	}
}
