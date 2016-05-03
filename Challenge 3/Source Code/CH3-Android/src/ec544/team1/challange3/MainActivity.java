package ec544.team1.challange3;

import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity {

    private Socket socket;
    boolean connected = false;
    boolean status1 = false;
    boolean status2 = false;
    boolean status3 = false;    
    private static final int SERVERPORT = 5000;
    private static String SERVER_IP = "";
    private Context context;
    InetAddress giriAddress;
    LinearLayout switches;
    ImageView switch1;
    ImageView switch2;
    ImageView switch3;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		
	    switches = (LinearLayout) findViewById(R.id.switchLayout);
	    switch1 = (ImageView) findViewById(R.id.imageView1);
	    switch2 = (ImageView) findViewById(R.id.imageView2);
	    switch3 = (ImageView) findViewById(R.id.imageView3);
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(Html.fromHtml("<font color='#6f5499'>EC544 Challenge 3: Remote Access </font>"));
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setBackgroundDrawable(new ColorDrawable(0xffffffff));
		 context = this.getApplicationContext();
		Button clickButton = (Button) findViewById(R.id.connectBTN);
		
		
		 switch1.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					switch (event.getAction()) {
	                case MotionEvent.ACTION_DOWN: {
	                 

	                	if(status1 == false)
	                	{	
	                		status1 = true;
	                		switch1.setImageResource(R.drawable.toggle_two);
	                		//Send command to turn on here

			        		try {
			        			
			        			//first number is led number , word == on or off
			        			String str = "1ON";
			        			PrintWriter out = new PrintWriter(new BufferedWriter(
			        					new OutputStreamWriter(socket.getOutputStream())),
			        					true);
			        			out.println(str);
			        		} catch (UnknownHostException e) {
			        			e.printStackTrace();
			        		} catch (IOException e) {
			        			e.printStackTrace();
			        		} catch (Exception e) {
			        			e.printStackTrace();
			        		}
	                	}
	    				else
	                	{
	    					status1 = false;
	                		switch1.setImageResource(R.drawable.toggle_one);
	                		//Send command to turn off here

			        		try {
			        			
			        			//first number is led number , word == on or off
			        			String str = "1OFF";
			        			PrintWriter out = new PrintWriter(new BufferedWriter(
			        					new OutputStreamWriter(socket.getOutputStream())),
			        					true);
			        			out.println(str);
			        		} catch (UnknownHostException e) {
			        			e.printStackTrace();
			        		} catch (IOException e) {
			        			e.printStackTrace();
			        		} catch (Exception e) {
			        			e.printStackTrace();
			        		}
	    				
	                	}
	                	
	                	
	                    break;
	                }
	             
	                }
	                return true;
					
				}
	        });
		 

			
		 switch2.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					switch (event.getAction()) {
	                case MotionEvent.ACTION_DOWN: {
	                 

	                	if(status2 == false)
	                	{	
	                		status2 = true;
	                		switch2.setImageResource(R.drawable.toggle_two);
	                		//Send command to turn on here

			        		try {
			        			
			        			//first number is led number , word == on or off
			        			String str = "2ON";
			        			PrintWriter out = new PrintWriter(new BufferedWriter(
			        					new OutputStreamWriter(socket.getOutputStream())),
			        					true);
			        			out.println(str);
			        		} catch (UnknownHostException e) {
			        			e.printStackTrace();
			        		} catch (IOException e) {
			        			e.printStackTrace();
			        		} catch (Exception e) {
			        			e.printStackTrace();
			        		}
	                	}
	    				else
	                	{
	    					status2 = false;
	                		switch2.setImageResource(R.drawable.toggle_one);
	                		//Send command to turn off here

			        		try {
			        			
			        			//first number is led number , word == on or off
			        			String str = "2OFF";
			        			PrintWriter out = new PrintWriter(new BufferedWriter(
			        					new OutputStreamWriter(socket.getOutputStream())),
			        					true);
			        			out.println(str);
			        		} catch (UnknownHostException e) {
			        			e.printStackTrace();
			        		} catch (IOException e) {
			        			e.printStackTrace();
			        		} catch (Exception e) {
			        			e.printStackTrace();
			        		}
	    				
	                	}
	                	
	                	
	                    break;
	                }
	             
	                }
	                return true;
					
				}
	        });
		 
			
		 switch3.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					switch (event.getAction()) {
	                case MotionEvent.ACTION_DOWN: {
	                 

	                	if(status3 == false)
	                	{	
	                		status3 = true;
	                		switch3.setImageResource(R.drawable.toggle_two);
	                		
			        		try {
			        			
			        			//first number is led number , word == on or off
			        			String str = "3ON";
			        			PrintWriter out = new PrintWriter(new BufferedWriter(
			        					new OutputStreamWriter(socket.getOutputStream())),
			        					true);
			        			out.println(str);
			        		} catch (UnknownHostException e) {
			        			e.printStackTrace();
			        		} catch (IOException e) {
			        			e.printStackTrace();
			        		} catch (Exception e) {
			        			e.printStackTrace();
			        		}
	                		//Send command to turn on here
	                	}
	    				else
	                	{
	    					status3 = false;
	                		switch3.setImageResource(R.drawable.toggle_one);
	                		//Send command to turn off here

			        		try {
			        			
			        			//first number is led number , word == on or off
			        			String str = "3OFF";
			        			PrintWriter out = new PrintWriter(new BufferedWriter(
			        					new OutputStreamWriter(socket.getOutputStream())),
			        					true);
			        			out.println(str);
			        		} catch (UnknownHostException e) {
			        			e.printStackTrace();
			        		} catch (IOException e) {
			        			e.printStackTrace();
			        		} catch (Exception e) {
			        			e.printStackTrace();
			        		}
	    				
	                	}
	                	
	                	
	                    break;
	                }
	             
	                }
	                return true;
					
				}
	        });
		
		
		
		clickButton.setOnClickListener( new OnClickListener() {

		            @Override
		            public void onClick(View v) {
		            	Button clickButton = (Button) findViewById(R.id.connectBTN);
		            	
            
		                // TODO Auto-generated method stub
		            	
		            	if(connected == false)
		            	{	
		            		switch1.setImageResource(R.drawable.toggle_one);
		            		switch2.setImageResource(R.drawable.toggle_one);
		            		switch3.setImageResource(R.drawable.toggle_one);
		            		status1 = false;
		            		status2 = false;
		            		status3 = false;
		            		
		            		new Thread(new ClientThread()).start();
									
		            		 switches.setVisibility(View.VISIBLE);
		            		 clickButton.setText("Disconnect");
									
									
		            	}
						else
		            	{
							clickButton.setText("Connect");
							 switches.setVisibility(View.GONE);
							
							
		            		try {
								socket.close();				
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		            		connected = false;
						
		            	}

		            }
		        });
		
	
	}


	class ClientThread implements Runnable {

		@Override
		public void run() {

			
			
			try {
				giriAddress = java.net.InetAddress.getByName("sparakis.ddns.net");
				SERVER_IP = giriAddress.getHostAddress();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
			
				InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
			
				
				socket = new Socket(serverAddr, SERVERPORT);

			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		
			if(socket.isConnected())
			{
				connected = true;
				//Button clickButton = (Button) findViewById(R.id.connectBTN);
				 //
				 
			}
			else
			{
				//Button clickButton = (Button) findViewById(R.id.connectBTN);
				 //clickButton.setText("Connection Failed... Try Again");
				connected = false;
			}
		}

	}

	

}
