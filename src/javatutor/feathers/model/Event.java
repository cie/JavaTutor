package javatutor.feathers.model;

import static com.feathersjs.client.FeathersTools.await;

import com.feathersjs.client.FeathersException;

import javatutor.feathers.App;

public class Event {
	public String hintSource;
	public Integer hintPosition;
	public boolean hintVisible;
	public String studentId;
	public String taskId;
	public String source;
	public String button;
	public String internalError;
	public long timestamp;
	
	public void create() throws InterruptedException, FeathersException {
		await(c -> App.get().service("events", Event.class).create(this, c), Event.class);
	}
}
