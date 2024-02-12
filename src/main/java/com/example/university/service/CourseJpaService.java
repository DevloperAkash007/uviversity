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
import com.example.university.model.Professor;
import com.example.university.model.Student;
import com.example.university.repository.CourseJpaRepository;
import com.example.university.repository.CourseRepository;
import com.example.university.repository.ProfessorJpaRepository;
import com.example.university.repository.StudentJpaRepository;

@Service
public class CourseJpaService implements CourseRepository {

    @Autowired
    CourseJpaRepository courseJpaRepository;

    @Autowired
    StudentJpaRepository studentJpaRepository;

    @Autowired
    ProfessorJpaRepository professorJpaRepository;

    @Override
    public ArrayList<Course> getCourses() {
        List<Course> cList = courseJpaRepository.findAll();
        ArrayList<Course> courses = new ArrayList<>(cList);
        return courses;
    }

    @Override
    public Course getCourseById(int courseId) {
        try {
            Course course = courseJpaRepository.findById(courseId).get();
            return course;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public Professor getCourseProfessor(int courseId) {
        try {
            Course course = courseJpaRepository.findById(courseId).get();
            return course.getProfessor();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public List<Student> getCourseStudents(int courseId) {
        try {
            Course course = courseJpaRepository.findById(courseId).get();
            return course.getStudents();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public Course addCourse(Course course) {
        try {
        Professor professor = course.getProfessor();
        int professorId = professor.getProfessorId();
        
        Professor fetchedProfessor = professorJpaRepository.findById(professorId).get();
        course.setProfessor(fetchedProfessor);
        

        List<Integer> studentIds = new ArrayList<>();
        for (Student student : course.getStudents()) {
            studentIds.add(student.getStudentId());
        }
        List<Student> students = studentJpaRepository.findAllById(studentIds);
        if (studentIds.size() != students.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        course.setStudents(students);
        Course savedCourse = courseJpaRepository.save(course);
        return savedCourse;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public Course updateCourse(int courseId, Course course) {
        try {
            Course updatedCourse = courseJpaRepository.findById(courseId).get();
            if (course.getCourseName() != null) {
                updatedCourse.setCourseName(course.getCourseName());
            }
            if (course.getCredits() != null) {
                updatedCourse.setCredits(course.getCredits());
            }
            if (course.getProfessor() != null) {
                Professor professor = course.getProfessor();
                int professorId = professor.getProfessorId();

                Professor requriedProfessor = professorJpaRepository.findById(professorId).get();

                updatedCourse.setProfessor(requriedProfessor);
            }
            if (course.getStudents() != null) {

                List<Integer> studentIds = new ArrayList<>();
                for (Student student : course.getStudents()) {
                    studentIds.add(student.getStudentId());
                }
                List<Student> students = studentJpaRepository.findAllById(studentIds);
                if (studentIds.size() != students.size()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                }
                updatedCourse.setStudents(students);

            }
            courseJpaRepository.save(updatedCourse);
            return updatedCourse;

        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public void deleteCourse(int courseId) {
        try {
            courseJpaRepository.deleteById(courseId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        throw new ResponseStatusException(HttpStatus.NO_CONTENT);
    }

}