package com.luanvv.springboot.aws.ec2andrds.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.luanvv.springboot.aws.ec2andrds.configuration.ReadOnlyRepository;
import com.luanvv.springboot.aws.ec2andrds.entities.Student;

@ReadOnlyRepository
public interface ReadStudentRepository extends JpaRepository<Student, Integer> {

}
