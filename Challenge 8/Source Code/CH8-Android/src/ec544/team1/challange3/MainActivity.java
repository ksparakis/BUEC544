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
    LinearLayout mom;
    Button speed1;
    Button speed2;
    Button speed3;
    Button forward;
    Button stop;
    Button back;
    Button slightLeft;
    Button slightRight;
    Button sharpRight;
    Button sharpLeft;
    Button followLeft;
    Button followRight;
    Button straight;
    Button localization;
    ImageView locer;
    int displayLoc;
    int followCount =0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		
		
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(Html.fromHtml("<font color='#6f5499'>EC544 Challenge 8: Car Controller</font>"));
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setBackgroundDrawable(new ColorDrawable(0xffffffff));
		 context = this.getApplicationContext();
		Button clickButton = (Button) findViewById(R.id.connectBTN);
	         speed1 =(Button) findViewById(R.id.speed1BTN);
		     speed2= (Button) findViewById(R.id.speed2BTN);
		     speed3 =(Button) findViewById(R.id.speed3BTN);
		     forward =(Button) findViewById(R.id.fowardBTN);
		     stop =(Button) findViewById(R.id.stopBTN);
		     back =(Button) findViewById(R.id.backwardsBTN);
		     slightLeft =(Button) findViewById(R.id.slightLBTN);
		     slightRight =(Button) findViewById(R.id.slightRBTN);
		     sharpRight =(Button) findViewById(R.id.sharpRBTN);
		     sharpLeft=(Button) findViewById(R.id.sharpLBTN);
		     followLeft=(Button) findViewById(R.id.followLBTN);
		     followRight =(Button) findViewById(R.id.followRBTN);
		     straight=(Button) findViewById(R.id.straightBTN);
		     localization=(Button) findViewById(R.id.loc);
		     locer =(ImageView) findViewById(R.id.imageView1);
		     mom = (LinearLayout) findViewById(R.id.mother);
		     displayLoc =0;
		     localization.setOnClickListener( new OnClickListener() {
		    	
		            @Override
		            public void onClick(View v) {       	
		    
		                // TODO Auto-generated method stub
		
						   if(displayLoc ==0)
						   {
							  System.out.println("WAFASD");
							  mom.setVisibility(View.GONE);
							  locer.setVisibility(View.VISIBLE); 
							  displayLoc++;
						   }
						   else
						   {
							   locer.setVisibility(View.GONE); 
								  mom.setVisibility(View.VISIBLE); 
								  displayLoc =0;
						   }
							 
						
		            	

		            }
		        });
		  straight.setOnClickListener( new OnClickListener() {

	            @Override
	            public void onClick(View v) {       	
	    
	                // TODO Auto-generated method stub
	            	
	            	if(connected == false)
	            	{	
	            		
	            		
	    
								
	            	}
					else
	            	{

						try {
							
							//first number is led number , word == on or off
							String str = "STRAIGHT";
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

	            }
	        });
		
		  sharpRight.setOnClickListener( new OnClickListener() {

	            @Override
	            public void onClick(View v) {       	
	    
	                // TODO Auto-generated method stub
	            	
	            	if(connected == false)
	            	{	
	            		
	            		
	    
								
	            	}
					else
	            	{

						try {
							
							//first number is led number , word == on or off
							String str = "SHARPR";
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

	            }
	        });
		 sharpLeft.setOnClickListener( new OnClickListener() {

	            @Override
	            public void onClick(View v) {       	
	    
	                // TODO Auto-generated method stub
	            	
	            	if(connected == false)
	            	{	
	            		
	            		
	    
								
	            	}
					else
	            	{

						try {
							
							//first number is led number , word == on or off
							String str = "SHARPL";
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

	            }
	        });

		  followRight.setOnClickListener( new OnClickListener() {

	            @Override
	            public void onClick(View v) {       	
	    
	                // TODO Auto-generated method stub
	            	
	            	if(connected == false)
	            	{	
	            		
	            		
	    
								
	            	}
					else
	            	{

						try {
							
							//first number is led number , word == on or off
							String str = "FOLLOWR";
							followCount ++;
							updateImage();
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

	            }
	        });
		
		  followLeft.setOnClickListener( new OnClickListener() {

	            @Override
	            public void onClick(View v) {       	
	    
	                // TODO Auto-generated method stub
	            	
	            	if(connected == false)
	            	{	
	            		
	            		
	    
								
	            	}
					else
	            	{

						try {
							
							//first number is led number , word == on or off
							String str = "FOLLOWL";
							followCount++;
							updateImage();
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

	            }
	        });
		
		  slightRight.setOnClickListener( new OnClickListener() {

	            @Override
	            public void onClick(View v) {       	
	    
	                // TODO Auto-generated method stub
	            	
	            	if(connected == false)
	            	{	
	            		
	            		
	    
								
	            	}
					else
	            	{

						try {
							
							//first number is led number , word == on or off
							String str = "SLIGHTR";
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

	            }
	        });
		
		  slightLeft.setOnClickListener( new OnClickListener() {

	            @Override
	            public void onClick(View v) {       	
	    
	                // TODO Auto-generated method stub
	            	
	            	if(connected == false)
	            	{	
	            		
	            		
	    
								
	            	}
					else
	            	{

						try {
							
							//first number is led number , word == on or off
							String str = "SLIGHTL";
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

	            }
	        });
		  
	    slightRight.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {       	
    
                // TODO Auto-generated method stub
            	
            	if(connected == false)
            	{	
            		
            		
    
							
            	}
				else
            	{

					try {
						
						//first number is led number , word == on or off
						String str = "SLIGHTR";
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

            }
        });
		
		stop.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {       	
    
                // TODO Auto-generated method stub
            	
            	if(connected == false)
            	{	
            		
            		
    
							
            	}
				else
            	{

					try {
						
						//first number is led number , word == on or off
						String str = "STOP";
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

            }
        });
		
		
		back.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {       	
    
                // TODO Auto-generated method stub
            	
            	if(connected == false)
            	{	
            		
            		
    
							
            	}
				else
            	{

					try {
						
						//first number is led number , word == on or off
						String str = "BACKWARDS";
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

            }
        });
		
		
		forward.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {       	
    
                // TODO Auto-generated method stub
            	
            	if(connected == false)
            	{	
            		
            		
    
							
            	}
				else
            	{

					try {
						
						//first number is led number , word == on or off
						String str = "FORWARD";
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

            }
        });
		
		speed1.setOnClickListener( new OnClickListener() {

		            @Override
		            public void onClick(View v) {
            
		                // TODO Auto-generated method stub
		            	
		            	if(connected == false)
		            	{	
		            		
		            		
		    
									
		            	}
						else
		            	{

							try {
								
								//first number is led number , word == on or off
								String str = "SPEED1";
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

		            }
		        });
		
		speed2.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
            	
            	if(connected == false)
            	{	
            		
            		
    
							
            	}
				else
            	{

					try {
						
						//first number is led number , word == on or off
						String str = "SPEED2";
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

            }
        });
		
		speed3.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {
            
            	
    
                // TODO Auto-generated method stub
            	
            	if(connected == false)
            	{	
            		
            		
    
							
            	}
				else
            	{

					try {
						
						//first number is led number , word == on or off
						String str = "SPEED3";
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

            }
        });
		 
		clickButton.setOnClickListener( new OnClickListener() {

		            @Override
		            public void onClick(View v) {
		            	Button clickButton = (Button) findViewById(R.id.connectBTN);
		            	
            
		                // TODO Auto-generated method stub
		            	
		            	if(connected == false)
		            	{	
		            		
		            		
		            		new Thread(new ClientThread()).start();
									
		            		 clickButton.setText("Disconnect");
									connected = true;
									followCount =0;
									updateImage();
									
		            	}
						else
		            	{
							clickButton.setText("Connect");
							
							
		            		try {
								socket.close();				
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		            		connected = false;
		            		followCount =0;
		            		updateImage();
						
		            	}

		            }
		        });
		
	
	}
	
	public void updateImage()
	{
		if(followCount > 8)
		{
			followCount =0;
		}
		switch(followCount)
		{
		case 0:
			locer.setImageResource(R.drawable.image1);
			break;
		case 1:
			locer.setImageResource(R.drawable.image2);
			break;
		case 2:
			locer.setImageResource(R.drawable.image3);
			break;
		case 3:
			locer.setImageResource(R.drawable.image4);
			break;
		case 4:
			locer.setImageResource(R.drawable.image5);
			break;
		case 5:
			locer.setImageResource(R.drawable.image6);
			break;
		case 6:
			locer.setImageResource(R.drawable.image7);
			break;
		case 7:
			locer.setImageResource(R.drawable.image8);
			break;
		case 8:
			locer.setImageResource(R.drawable.image9);
			break;
		}
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
