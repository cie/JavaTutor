package javatutor.feathers;

import com.feathersjs.client.Feathers;
import com.feathersjs.client.plugins.providers.IFeathersProvider;
import com.feathersjs.client.service.FeathersService;

public class App extends Feathers {
	private IFeathersProvider provider;
	private static String BASE_URL = "http://localhost:3030";

	public App() {
		setBaseUrl(BASE_URL);
		provider = new SocketClient(this, null);
	}
	
	@Override
	public <J> FeathersService<J> service(String name, Class<J> jClass) {
		FeathersService<J> service = super.service(name, jClass);

        if (service != null && service.getProvider() == null) {
            service.setProvider(provider);
        }
        return service;
	}

	@Override
	public IFeathersProvider getProvider() {
		return provider;
	}

    private static App instance;

	public synchronized static App get() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }
}
