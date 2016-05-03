/*
 * Onspot-TCPLEDControler
 * This code has been edited to recieve a predefined signal and turn on the representive LED
 *
 * 
 * Orignaly:SensorSampler.java
 *
 * Copyright (c) 2008-2010 Sun Microsystems, Inc.
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
import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.ILightSensor;
import com.sun.spot.resources.transducers.ISwitch;
import com.sun.spot.resources.transducers.ITriColorLED;
import com.sun.spot.util.Utils;
import javax.microedition.io.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import com.sun.spot.resources.transducers.ITemperatureInput;
import com.sun.spot.service.BootloaderListenerService;
import com.sun.spot.util.IEEEAddress;
import java.io.IOException;


public class SensorSampler extends MIDlet // implements Runnable{  // Aziz multithreading
{
    
    private static final int HOST_PORT = 88;
    private static final int HOST_PORT_HS = 89; //WZ COMMENTs: Port for handshaking
    private static final int LISTENING_PERIOD = 100;

    boolean handshake = true; // WZ COMMENTs: Add a handshake signal to control the transmitting, this signal should be received from the basestation
    private long[] addresses = new long[8];  //WZ COMMENTS: address is 8 bit long

    private int loop = 4;
    boolean listening = true;      
         
    RadiogramConnection rCon = null;
    Datagram dg = null;
    
    //private Thread t;
    //private String threadName;
    
        
    //RadiogramConnection rCon2 = null; //WZ COMMENTS: another connection for handshaking
    //Datagram dg2 = null; //WZ COMMENTS: another connection for handshaking
    //String ourAddress = System.getProperty("IEEE_ADDRESS");  //WZ COMMENTS: ourAddress is the MAC address
    ITriColorLED led1 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED1");
    ITriColorLED led2 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED2");
    ITriColorLED led3 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED3");
    ITriColorLED led4 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED4");
    ITriColorLED led5 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED5");
    ITriColorLED led6 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED6");
    ITriColorLED led7 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED7");
    ITriColorLED led8 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED8");
    
    

    
    
       public void initialize() { 
         
     
         
        //String macAddressBytes = ourAddress.substring(15, 19); 
        int AddressforCompare = 0;
        String moteAddress=null;
        
   try{
        
        DatagramConnection recvConn = (DatagramConnection) Connector.open("radiogram://:89");    
        Datagram dg2 = recvConn.newDatagram(recvConn.getMaximumLength()); 
        
        rCon = (RadiogramConnection) Connector.open("radiogram://broadcast:" + HOST_PORT);
        //dg = rCon.newDatagram(50);  // only sending 12 bytes of data
        led8.setRGB(255,255,255);
         led8.setOn();
         led7.setOff();
        while (true) {
             

        try {
            // Open up a unicast listening connection to the host port
            // where the 'on Desktop' portion of this demo is listening
            //rCon2 = (RadiogramConnection) Connector.open("radiogram://:" + HOST_PORT_HS); //WZ COMMENTs: open another port for listening for handshake
            //led#.setOff();
            //led#.setOn();
            //led#.setRGB(255,255,255); 
            //Utils.sleep(500);  
                 
            
            dg2.reset();    
            recvConn.receive(dg2);  
            //MoteAddress = dg2.readInt();       // read the last 4 bytes mote mac address
            moteAddress = dg2.readUTF();
            dg2.reset();
            System.out.println(moteAddress);
            if(moteAddress.equals("1OFF"))
            {
                led1.setOff();
            }else if(moteAddress.equals("2OFF"))
            {
                led2.setOff();
            }
            else if(moteAddress.equals("3OFF"))
            {
                led3.setOff();
            }
            else if(moteAddress.equals("1ON"))
            {
                 led1.setRGB(255,0,0);
                led1.setOn();
            }
            else if(moteAddress.equals("2ON"))
            {
                 led2.setRGB(0,255,0);
                led2.setOn();
            }
            else if(moteAddress.equals("3ON"))
            {
                 led3.setRGB(0,0,255);
                led3.setOn();
            }
            else
            {
                //FAIL
                led7.setRGB(255,0,0);
               led7.setOn();
            }

            /*
            switch(moteAddress)
            {
            case moteAddress=="1OFF":  
                         led1.setOff();
                     break;
            case moteAddress=="2OFF": 
                         led2.setOff();
                     break;
            case moteAddress=="3OFF":  
                        led3.setOff();
                     break;
             case moteAddress=="1ON": 
                led1.setRGB(255,0,0);
                led1.setOn(); 
                        
                     break;
            case moteAddress=="2ON": 
                led2.setRGB(255,0,0);
                led2.setOn();
                        
                     break;
            case moteAddress=="3ON":  
                led3.setRGB(255,0,0);
                led3.setOn();
                       
                     break;
                     default:
                     break;

            }
            */
              
          
            } catch (Exception e) {
                System.err.println("Caught " + e +  " while reading sensor samples.");
                led2.setRGB(255, 0, 0);
                led2.setOn();
                //throw e;
            }       
     
        
      
     } // end of while
        
    } catch (Exception e) {
        System.err.println("Caught " + e +  " while reading sensor samples.");
        led1.setRGB(255, 0, 0);
        
    }    

 }  
 protected void startApp() throws MIDletStateChangeException {
       // RadiogramConnection rCon = null;

    //System.out.println("Starting sensor sampler application on " + ourAddress + " ...");
	// Listen for downloads/commands over USB connection
	//new com.sun.spot.service.BootloaderListenerService().getInstance().start();

       // handshake = false; //WZ COMMENTs: DEBUG
        
        //String ourAddress = System.getProperty("IEEE_ADDRESS");
        ILightSensor lightSensor = (ILightSensor)Resources.lookup(ILightSensor.class);
        //ITriColorLED led = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED7");
        
        System.out.println("Starting sensor sampler application on ...");

	// Listen for downloads/commands over USB connection
	new com.sun.spot.service.BootloaderListenerService().getInstance().start();
       
        initialize();
       // run();
    }
   
    protected void pauseApp() {
        // This will never be called by the Squawk VM
    }
    
    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
        // Only called if startApp throws any exception other than MIDletStateChangeException
    }
}