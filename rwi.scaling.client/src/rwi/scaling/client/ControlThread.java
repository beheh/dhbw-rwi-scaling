package rwi.scaling.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import rwi.scaling.common.interfaces.IInstanceService;

public class ControlThread extends Thread {

	private final IInstanceService service;
	private final URL rootUrl;
	private final int objectsBeforeScale = 20;

	public ControlThread(IInstanceService service, URL rootUrl) {
		this.service = service;
		this.rootUrl = rootUrl;
	}

	private JSONObject requestServerInfo(URL url) throws IOException, JSONException {
		// open remote
		URLConnection urlconnection = new URL(url.toString() + "/p").openConnection();
		BufferedReader reader = new BufferedReader(new InputStreamReader(urlconnection.getInputStream()));

		// read from remote
		String line = "";
		String result = "";
		while((line = reader.readLine()) != null) {
			result += line;
		}

		return new JSONObject(result);
	}

	private int getServerCount(URL url) throws IOException, JSONException {
		int count = 0;

		// parse result
		JSONObject result = requestServerInfo(url);
		JSONArray members = (JSONArray) result.get("members");
		int length = members.length();
		for(int i = 0; i < length; i++) {
			JSONObject object = (JSONObject) members.get(i);
			if(object.has("addr")) {
				count += getServerCount(new URL(object.getString("addr")));
			}
		}

		// we are one server
		count++;

		return count;
	}

	private int getRequiredAdditionalServers(URL url) throws IOException, JSONException {
		int count = 0;

		// parse result
		JSONObject result = requestServerInfo(url);
		JSONArray members = (JSONArray) result.get("members");
		int length = members.length();
		int objects = 0;
		for(int i = 0; i < length; i++) {
			JSONObject object = (JSONObject) members.get(i);
			if(object.has("addr")) {
				// existing address means this node is a dispatcher
				count += getRequiredAdditionalServers(new URL(object.getString("addr")));
			}
			else {
				// we identified this node as datanode 
				objects++;
			}
		}

		if(objects > objectsBeforeScale) {
			count++;
		}

		return count;
	}

	@Override
	public void run() {
		System.out.println("Launching instance scaling control thread");
		int lastServerCount = 0;
		int requested = 0;
		while(true) {
			try {
				// check existing machine count
				int serverCount = getServerCount(rootUrl);
				if(lastServerCount != serverCount) {
					lastServerCount = serverCount;
					if(serverCount > lastServerCount) {
						int difference = serverCount - lastServerCount;
						requested = Math.max(0, requested - difference);
						System.out.println("Detected " + difference + " additional servers in system");
					}
					else {
						System.out.println("Detected system downscaling");
					}
				}
				if(requested == 0) {
					// recursively check requirements if we are not waiting for instances
					System.out.print("Checking instances...");
					int i = getRequiredAdditionalServers(rootUrl);
					System.out.println(" done");
					if(i > 0) {
						if(i > 1) {
							System.out.println("Requesting " + i + " new instances from scaling instance service");
						}
						else {
							System.out.println("Requesting single new instance from scaling instance service");
						}
						requested = i;
						service.requestInstances(i);
					}
				}
				else {
					System.out.println("Waiting for instance startup");
				}
			}
			catch(Exception ex) {
				ex.printStackTrace(System.err);
			}
			try {
				// query the server every 10 seconds
				Thread.sleep(10 * 1000);
			}
			catch(InterruptedException e) {
			}

		}
	}
}
