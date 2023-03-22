package it.polimi.tiw.courses;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/*
 * The class CourseManager acts as a "mock-up" of the database.
 * As you advance in the course you will learn to create classes to write and read from the database 
 * and that will be the standard for the course (you will deliver the project using the database).
 * 
 * */

public class CoursesManager {

	private static @Nullable CoursesManager instance = null;
	private Map<Integer, Course> courses;

	private CoursesManager() {
		super();
		this.courses = new HashMap<Integer, Course>();
		this.courses.put(1, new Course(1, "1248BHW", "Tecnologie informatiche per il web", "Piero Fraternali", "Intro to web programming", 40));
		this.courses.put(2, new Course(2, "2348BHW", "Base di Dati 2", "Piero Fraternali", "Data bases 2 description...", 45));
		this.courses.put(3, new Course(3, "4348BHW", "Advanced Web Technologies", "Piero Fraternali", "More advanced topics of web technologies", 44));
	}

	public static CoursesManager getCoursesManagerInstance() {
		if (instance == null) {
			instance = new CoursesManager();
		}
		return instance;
	}
	
	public Collection<Course> getCourses() {
		return  courses.values();
	}
	
	public Course getCourseById(int id) {
		return Objects.requireNonNull(courses.get(id), "Missing course for id " + id);
	}

	public boolean existCourseWithId(Integer id) {
		return courses.containsKey(id);
	}
	
	public void insertCourse(Course c) {
		courses.put(c.getId(), c);
	}
	
	public Integer getNextId() {
		// DUMMY IMPLEMENTATION -- THIS WOULD BE AN AUTOINCREMENTAL FIELD IN THE DATABASE --
		return courses.size() + 1;
	}

}
