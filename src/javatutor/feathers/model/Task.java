package javatutor.feathers.model;

import static com.feathersjs.client.FeathersTools.awaitResult;

import com.feathersjs.client.FeathersException;
import com.feathersjs.client.service.Result;

import javatutor.feathers.App;

public class Task {
	public String packageName;
	public String className;
	public String instructions;
	public String initialSource;

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return className;
	}
	
	public static Result<Task> find() throws InterruptedException, FeathersException {
		return awaitResult(c -> App.get().service("tasks", Task.class).find(c), Task.class);
	}
}
