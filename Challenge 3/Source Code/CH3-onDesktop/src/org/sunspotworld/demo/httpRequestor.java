package org.sunspotworld.demo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Date;
import java.net.URL;

public class httpRequestor implements Runnable{
	 
	private final String USER_AGENT = "Mozilla/5.0";

	

	public httpRequestor(double temp, String device_id, int transmission_id, long time) throws Exception{
		// TODO Auto-generated constructor stub

		String url = "http://localhost/sunspots/";
		//String url = "http://ec2-54-205-115-111.compute-1.amazonaws.com/Divvy_Login.php";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
 
		String urlParameters = "tag=report_temp&temperature="+temp+"&device_id="+device_id+"&transmission_id="+transmission_id+"&time="+new Date(time);
		//String urlParameters = "tag=report_temp&temperature="+temp+"&"+device_id+"&transmission_id="+transmission_id;
 
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		//print result
		System.out.println(response.toString());

	}

	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
 
}

