package javatutor.feathers;

import com.feathersjs.client.plugins.providers.FeathersSocketClient;

import io.socket.client.Socket;

public class SocketClient extends FeathersSocketClient {

	public SocketClient(App app, Socket socket) {
		super(app, null, socket);
	}
	
	@Override
	protected void authenticate() {
		// no authentication for now
		/*create(null, "authentication", new Auth(), new FeathersCallback<AuthenticationResult>() {

			@Override
			public void onError(String errorMessage) {
				Log.e("SOCKET:", "Authentication failed");
			}

			@Override
			public void onSuccess(AuthenticationResult t) {
				Log.d("SOCKET:", "Authenticated");
			}
		}, AuthenticationResult.class);*/
	}

}
