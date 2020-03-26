package javatutor.feathers.model;

import static com.feathersjs.client.FeathersTools.await;

import java.util.List;

import com.feathersjs.client.FeathersException;

import javatutor.feathers.App;

public class Student {
	public String _id;
	public int serialNumber; 
	public List<Task> tasks;

	public static Student create() throws InterruptedException, FeathersException {
		return await(c -> App.get().service("students", Student.class).create(new Student(), c), Student.class);
	}
}
