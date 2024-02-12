/*
 *
 * You can use the following import statements
 * 
 * import org.springframework.beans.factory.annotation.Autowired;
 * import org.springframework.http.HttpStatus;
 * import org.springframework.stereotype.Service;
 * import org.springframework.web.server.ResponseStatusException;
 * import java.util.ArrayList;
 * import java.util.List;
 * 
 */
package com.example.university.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import com.example.university.model.Course;
import com.example.university.model.Student;
import com.example.university.repository.CourseJpaRepository;
import com.example.university.repository.StudentJpaRepository;
import com.example.university.repository.StudentRepository;

@Service
public class StudentJpaService implements StudentRepository {

    @Autowired
    StudentJpaRepository studentJpaRepository;

    @Autowired
    CourseJpaRepository courseJpaRepository;

    @Override
    public ArrayList<Student> getStudents() {
        ArrayList<Student> students = new ArrayList<>(studentJpaRepository.findAll());
        return students;
    }

    @Override
    public Student getStudentById(int studentId) {
        try {
            Student student = studentJpaRepository.findById(studentId).get();
            return student;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public List<Course> getStudentCourses(int studentId) {
        try {
            Student student = studentJpaRepository.findById(studentId).get();
            return student.getCourses();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public Student addStudent(Student student) {
        List<Integer> courseIds = new ArrayList<>();
        for (Course course : student.getCourses()) {
            courseIds.add(course.getCourseId());
        }
        List<Course> courses = courseJpaRepository.findAllById(courseIds);
        if (courses.size() != courseIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        student.setCourses(courses);
        Student savedStudent = studentJpaRepository.save(student);

        for (Course course : courses) {
            course.getStudents().add(savedStudent);
        }
        courseJpaRepository.saveAll(courses);
        return savedStudent;
    }

    @Override
    public Student updateStudent(int studentId, Student student) {
        try {
            Student updatedStudent = studentJpaRepository.findById(studentId).get();
            if (student.getStudentName() != null) {
                updatedStudent.setStudentName(student.getStudentName());
            }
            if (student.getEmail() != null) {
                updatedStudent.setEmail(student.getEmail());
            }
            if (student.getCourses() != null) {
                // remove ole entries
                List<Course> existingCourses = updatedStudent.getCourses();
                for (Course course : existingCourses) {
                    course.getStudents().remove(updatedStudent);
                }
                courseJpaRepository.saveAll(existingCourses);

                List<Integer> courseIds = new ArrayList<>();
                for (Course course : student.getCourses()) {
                    courseIds.add(course.getCourseId());
                }
                List<Course> courses = courseJpaRepository.findAllById(courseIds);
                if (courses.size() != courseIds.size()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                }
                updatedStudent.setCourses(courses);

                for (Course course : courses) {
                    course.getStudents().add(updatedStudent);
                }
                courseJpaRepository.saveAll(courses);
            }

            studentJpaRepository.save(updatedStudent);
            return updatedStudent;
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public void deleteStudent(int studentId) {
        try {
            Student student = studentJpaRepository.findById(studentId).get();
            List<Course> courses = student.getCourses();
            for (Course course : courses) {
                course.getStudents().remove(student);
            }
            courseJpaRepository.saveAll(courses);
            studentJpaRepository.deleteById(studentId);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        throw new ResponseStatusException(HttpStatus.NO_CONTENT);

    }

}