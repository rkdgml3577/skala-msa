package com.lecture.course.repository;

import com.lecture.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByTickerIgnoreCase(String ticker);

    boolean existsByTickerIgnoreCase(String ticker);

    List<Course> findBySectorAndGrade(Course.Sector sector, Course.Grade grade);

    List<Course> findByExchangeId(Long exchangeId);

    List<Course> findByGrade(Course.Grade grade);

    List<Course> findBySectorAndGradeAndTickerNotIn(
            Course.Sector sector,
            Course.Grade grade,
            List<String> excludeTickers
    );
}
