package com.luanvv.springboot.aws.ec2andrds.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.luanvv.springboot.aws.ec2andrds.entities.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer>{

}
