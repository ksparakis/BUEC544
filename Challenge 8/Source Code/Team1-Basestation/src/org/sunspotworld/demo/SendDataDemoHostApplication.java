 /*
 * SendDataDemoHostApplication.java
 *
 * Copyright (c) 2008-2009 Sun Microsystems, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.sunspotworld.demo;
import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.peripheral.ota.OTACommandServer;
import com.sun.spot.util.Utils;
import java.text.DateFormat;
import java.util.Date;
import javax.microedition.io.*;
import com.sun.spot.util.IEEEAddress;
import com.sun.spot.peripheral.basestation.*;
import com.sun.spot.peripheral.ota.IOTACommandServerListener;
import com.sun.spot.peripheral.radio.BasestationManager.DiscoverResult;
import com.sun.spot.peripheral.radio.*;
import com.sun.spot.peripheral.radio.I802_15_4_MAC;
import com.sun.spot.peripheral.radio.I802_15_4_PHY;
//package main;
import java.io.*;
import java.net.*;


/**
 * This application is the 'on Desktop' portion of the SendDataDemo. 
 * This host application collects sensor samples sent by the 'on SPOT'
 * portion running on neighboring SPOTs and just prints them out. 
 *   
 * @author: Vipul Gupta
 * modified: Ron Goldman
 */
public class SendDataDemoHostApplication extends Thread
{
  public ServerSocket serverSocket;
    
// Broadcast port on which we listen for sensor samples
    private static final int HOST_PORT = 88; // Data Port
  
    private static boolean[] SpotHandshake = new boolean[] {true,true,true,true}; 
    private static boolean handshake = true; //WZ COMMENTs: switch variable for handshaking process 
   public RadiogramConnection rCon;
   public Datagram dg;
   public DatagramConnection sendConn;
   public Datagram dg2;
   public int outT;
    String mymacAddress = IEEEAddress.toDottedHex(RadioFactory.getRadioPolicyManager().getIEEEAddress());
   
    public SendDataDemoHostApplication() throws IOException {
      // serverSocket = new ServerSocket(port);
    }



    public void run() {

      String incomingMsg;

        try {
            System.out.println("Starting socket thread...");
            serverSocket = new ServerSocket(5000);
            System.out.println("ServerSocket created, waiting for incomming connections...");

            Socket socket = serverSocket.accept();

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));

             // rCon = (RadiogramConnection) Connector.open("radiogram://:" + HOST_PORT);    
               // dg = rCon.newDatagram(rCon.getMaximumLength());                
            
                
              sendConn = (DatagramConnection) Connector.open("radiogram://broadcast:88");
              dg2 = sendConn.newDatagram(sendConn.getMaximumLength());

            while (true) {
                // System.out.println("Connection accepted, reading...");

                while ((incomingMsg = in.readLine()) != null && socket.isConnected()) {
                    System.out.println("Message recieved: " + incomingMsg
                            + ". Answering...");

                 
                    
                    
                    
                    if(incomingMsg.equals("STOP")){ //Stop                     
                        System.out.println("Received command: STOP");
                        //WZ COMMENTs: Set up outgoing message here
                        outT = 0;
                        }

                    else if(incomingMsg.equals( "FORWARD")){ //Start
                        System.out.println("Received command: START");
                        //WZ COMMENTs: Set up outgoing message here
                        outT=1;
                   }
                    else if(incomingMsg.equals( "SPEED1")){ //Set Speed 1
                        System.out.println("Received command: SET SPEED 1");
                        //WZ COMMENTs: Set up outgoing message here
                        outT=2;
                        }
                    else if(incomingMsg.equals( "SPEED2")){ //Set Speed 2
                        System.out.println("Received command: SET SPEED 2");
                        //WZ COMMENTs: Set up outgoing message here
                        outT =3;
                      }
                    else if(incomingMsg.equals( "SPEED3")){ //Set Speed 3
                        System.out.println("Received command: SET SPEED 3");
                        //WZ COMMENTs: Set up outgoing message here
                       outT=4;
                       }
                    else if(incomingMsg.equals( "BACKWARDS")){ //Backwards
                        System.out.println("Received command: BACKWARDs");
                        //WZ COMMENTs: Set up outgoing message here
                       outT=5;
                      }
                    else if(incomingMsg.equals( "SLIGHTL")){ //Turn slight left
                        System.out.println("Received command: TURN LEFT");
                        //WZ COMMENTs: Set up outgoing message here
                        outT=6;
                        }
                    else if(incomingMsg.equals( "SLIGHTR")){ //Turn slight right
                        System.out.println("Received command: TURN RIGHT");
                        //WZ COMMENTs: Set up outgoing message here
                        outT=7;
                       }
                    else if(incomingMsg.equals( "FOLLOWL")){ //Follow Left Wall
                        System.out.println("Received command: FOLLOW LEFT WALL");
                        //WZ COMMENTs: Set up outgoing message here
                        outT=8;
                        }
                    else if(incomingMsg.equals( "FOLLOWR")){ //Follow right wall
                        System.out.println("Received command: FOLLOW RIGHT WALL");
                        //WZ COMMENTs: Set up outgoing message here
                        outT=9;
                        }
                    else if(incomingMsg.equals( "SHARPL")){
                        //CommandLedOn(10);
                        outT =10;
                        }
                    else if(incomingMsg.equals( "SHARPR")){
                        //CommandLedOn(11);
                        outT=11;
                        }
                       else if(incomingMsg.equals( "STRAIGHT")){
                        //CommandLedOn(11);
                        outT=12;
                        
                    }
                    else{
                    System.out.println("Switch Unrecognized input");
                    }
                   
                

                    dg2.reset();
                    dg2.writeInt(outT);
                    System.out.println("Setting:"+incomingMsg);
                    System.out.println("Out: "+outT);
                    sendConn.send(dg2);

                    

                    // send a message
                    String outgoingMsg = "Message \"" + incomingMsg
                            + "\" recieved on server."
                            + System.getProperty("line.separator");
                    out.write(outgoingMsg);
                    out.flush();

                    System.out.println("Message sent: " + outgoingMsg);
                }

                if (socket.isConnected()) System.out.println("Socket still connected");
                else System.out.println("Socket not connected");
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }

    }


/*
   
    private void handshake() throws Exception {
    
        DateFormat fmt = DateFormat.getTimeInstance();
         
        try {
            
                 
              System.out.println("open2");
              //String macAddressBytes = ourAddress.substring(0, 19);
              // String macAddressBytes = IEEE802_15_4Environment.getIEEEAddress();
              
              //myaddress = getAddress();
                      
              //long ourAddr = Spot.getInstance().getRadioPolicyManager().getIEEEAddress();
              //System.out.println("Our radio address = " + IEEEAddress.toDottedHex(ourAddr));
             
              System.out.println("my mac is "+mymacAddress);
              
                } catch (Exception e) {
             System.err.println("setUp caught " + e.getMessage());
             throw e;
        }   
        


           while(true)  {
  
       
    
            for (int i = 0; i<3; i++ ) {
                
                  try{      
               
                dg2.reset();
                dg2.writeUTF(spot[i]);
                System.out.println("writing:"+spot[i]+" to Spot: " +i);
                sendConn.send(dg2);
                
                System.out.println("Handshake sent to SPOT: " + i);
                
                long now = System.currentTimeMillis();
                
                
                Utils.sleep(HANDSHAKE_SYNC);
                
                 } catch (Exception e) {
                System.err.println("Caught " + e +  " while reading sensor samples.");
                throw e;
                }
              } //end of for loop
               
                handshake = false;
             
        
            
            int count = 0;
            long timeout = System.currentTimeMillis();
            
           System.out.println("done sending");
            
            while (handshake==false)
            {
                  try{     
                
                long now = System.currentTimeMillis();
                System.out.println("Reading...");
                rCon.receive(dg);
                String addr = dg.getAddress();  // read sender's Id
                long time = dg.readLong();      // read time of the reading
                double val = dg.readDouble();         // read the sensor value
                //httpRequestor myRunnable = new httpRequestor(val, addr,1,time);
                //Thread t = new Thread(myRunnable);
                //t.start();
                System.out.println(fmt.format(new Date(time)) + "  from: " + addr + "   value = " + val);
                count ++;
                System.out.println("recieved count added to "+count);
                Utils.sleep(System.currentTimeMillis() - now);
                
                if (count==3)
                {
                    handshake = true;
                    Utils.sleep(HANDSHAKE_INTERVAL);     
                }
                
                else if (System.currentTimeMillis() - timeout > HANDSHAKE_INTERVAL)
                {
                    handshake = true;
                }
                
             } catch (Exception e) {
                System.err.println("Caught " + e +  " while reading sensor samples.");
                throw e;
            }
             

        } // end of while innerloop
           
               
       } // end of while loop
           
    }
    */
   
    /**
     * Start up the host application.
     *
     * @param args any command line arguments
     */
    public static void main(String[] args) throws Exception {
        // register the application's name with the OTA Command server & start OTA running
        OTACommandServer.start("SendDataDemo");

       

        try {
            Thread t = new SendDataDemoHostApplication();
            t.start();
        }catch(IOException e) {
            e.printStackTrace();
        }

        
       // SendDataDemoHostApplication app = new SendDataDemoHostApplication();
        
    }
}
