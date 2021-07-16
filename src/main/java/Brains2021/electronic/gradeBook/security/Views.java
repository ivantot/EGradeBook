package Brains2021.electronic.gradeBook.security;

public class Views {

	public static class Student {
	}

	public static class Parent extends Student {
	}

	public static class Teacher extends Parent {
	}

	public static class HomeroomT extends Teacher {
	}

	public static class Headmaster extends HomeroomT {
	}

	public static class Admin extends Headmaster {
	}

	public static class SuperAdmin extends Admin {

	}
}
