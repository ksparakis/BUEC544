/*
 * Copyright (c) 2006-2010 Sun Microsystems, Inc.
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
 **/

package org.sunspotworld;

import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.IDemoBoard;
import com.sun.spot.sensorboard.peripheral.Servo;
import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.ISwitch;
import com.sun.spot.resources.transducers.ISwitchListener;
import com.sun.spot.resources.transducers.SwitchEvent;
import com.sun.spot.resources.transducers.ITriColorLED;
import com.sun.spot.resources.transducers.ITriColorLEDArray;
import com.sun.spot.resources.transducers.LEDColor;
import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.peripheral.radio.RadioFactory;
import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.IAnalogInput;
import com.sun.spot.util.IEEEAddress;
import com.sun.spot.service.BootloaderListenerService;
import com.sun.spot.util.Utils;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import org.sunspotworld.common.Globals; //
import org.sunspotworld.common.TwoSidedArray; //
import org.sunspotworld.lib.BlinkenLights;
import org.sunspotworld.lib.LedUtils;
import org.sunspotworld.lib.RadioDataIOStream;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;


//WZ COMMENTs: Imported imports START *********************
import javax.microedition.io.Connector;  
import com.sun.spot.io.j2me.radiogram.*; 
import com.sun.spot.peripheral.Spot; 
import com.sun.spot.resources.transducers.LEDColor; 
import com.sun.spot.peripheral.radio.IRadioPolicyManager; 
import java.lang.String; 
//WZ COMMENTs: Imported imports END *********************

/**
 * This class is used to move a servo car consisting of two servos - one for
 * left wheel and the other for right wheel. To combine these servos properly,
 * this servo car moves forward/backward, turn right/left and rorate
 * clockwise/counterclockwise.
 * 
 * The current implementation has 3 modes and you can change these "moving mode" 
 * by pressing sw1. Mode 1 is "Normal" mode moving the car according to the tilt 
 * of the remote controller. Mode 2 is "Reverse" mode moving the car in 
 * a direction opposite to Mode 1. Mode 3 is "Rotation" mode only rotating the
 * car clockwise or counterclockwise according to the tilt.
 * 
 * @author Tsuyoshi Miyake <Tsuyoshi.Miyake@Sun.COM>
 * @author Yuting Zhang<ytzhang@bu.edu>
 */


public class ServoSPOTonCar extends MIDlet implements ISwitchListener {

    
    private static final int SERVO_CENTER_VALUE = 1450;
    private static final int FBServo_CENTER_VALUE = 1500;
    private static final int LRServo_MAX_VALUE = 2000;
    private static final int LRServo_MIN_VALUE = 1000;
    private static final int FBServo_MAX_VALUE = 2000;
    private static final int FBServo_MIN_VALUE = 100;
    private static final int LRServo_HIGH = 200;
    private static final int LRServo_LOW = 90; // AZIZ
    
    private static final int LRServo_VLOW = 20; // AZIZ
    
    private static final int FBServo_HIGH = 100;
    private static final int FBServo_LOW = 25;
    private static final int SERVO_CENTER_VALUE_TILT = 1300;
    private static final int SERVO_MAX_VALUE = 2000;
    private static final int SERVO_MIN_VALUE = 1000;
    private static final int SERVO_STEP_HIGH = 20;
    private static final int SERVO_STEP_LOW = 5;
    //ESC for forward/backward speed control
    private static final int ESC_CENTER_VALUE = 1500;
    private static final int ESC_MAX_VALUE = 2000;
    private static final int ESC_MIN_VALUE = 1000;
    private long myAddr;
    
    // devices
    private EDemoBoard eDemo = EDemoBoard.getInstance();
    private IAnalogInput irRightF = eDemo.getAnalogInputs()[EDemoBoard.A0];
    private IAnalogInput irLeftF = eDemo.getAnalogInputs()[EDemoBoard.A1];
    
    //Aziz
    private IAnalogInput irRightB = eDemo.getAnalogInputs()[EDemoBoard.A2];
    private IAnalogInput irLeftB = eDemo.getAnalogInputs()[EDemoBoard.A3];
    
    private ITriColorLED[] leds = eDemo.getLEDs();
    private static final ITriColorLEDArray myLEDs = (ITriColorLEDArray) Resources.lookup(ITriColorLEDArray.class); //WZ COMMENTs: changed to static final
    
    // 1st servo for left & right direction 
    private Servo LRServo = new Servo(eDemo.getOutputPins()[EDemoBoard.H0]); // fliped Aziz
    // 2nd servo for forward & backward direction
    private Servo FBServo = new Servo(eDemo.getOutputPins()[EDemoBoard.H1]); // fliped Aziz
    private BlinkenLights progBlinker = new BlinkenLights(1, 3);
    private BlinkenLights velocityBlinker = new BlinkenLights(4, 6);
    private int currentLR = SERVO_CENTER_VALUE;
    private int currentFB = FBServo_CENTER_VALUE;
    private int stepLR = LRServo_LOW;
    private int stepLR1 = LRServo_VLOW; // AZIZ
    private int stepFB = FBServo_LOW;
    private int locationSTATE =0;
    
    //WZ COMMENTs: Devices for tilting
        // steering servo for left & right direction 
    private Servo servo = new Servo(eDemo.getOutputPins()[EDemoBoard.H1]);
    // esc for forward & backward direction
    private Servo esc = new Servo(eDemo.getOutputPins()[EDemoBoard.H0]);
    
//    private int LRServoForwardValue;
//    private int FBServoForwardValue;
    private int LRServoLeft = SERVO_CENTER_VALUE +LRServo_LOW;
    private int LRServoRight = SERVO_CENTER_VALUE -LRServo_LOW;
    private int FBServoForward = FBServo_CENTER_VALUE +FBServo_LOW;
    private int FBServoBack = FBServo_CENTER_VALUE - FBServo_LOW;
    
                 
    //Aziz for calibration
    private double SrightF=0;
    private double SleftF=0;
    private double SrightB=0;
    private double SleftB=0;

    private double initRF =0;
    private double initLF =0;
    private double initRB =0;
    private double initLB =0;

    private double[] rFHistory;
    private double[] lFHistory;
    private double[] rBHistory;
    private double[] lBHistory;
    private int historyCounter = 0;
    
    int gapcount =0;
    private int sleeptime = 35;
            
    private int keepvalue = 10;//for caliibration
         
    public int stopper =0;
    
    
    //WZ COMMENTs: Imported variables START *********************
    private static final int LED_ON = 10; 
    private static final String RECEIVE_PORT = "55";
    private static final int BROADCAST_CHANNEL = 15;
    
    //MUST be the same as the CONFIRM_BYTE from RSSIBeacon.java
    private static final byte RSSI_CONFIRM_BYTE = 23;
    
    //MUST be the same as the CONFIRM_BYTE from RSSIReceiver.java
    //Change this to keep people from using your port and screwing up your data!
    private static final byte TRIP_CONFIRM_BYTE = 100;
    
    //static radio connection objects
    private static RadiogramConnection rxConnection = null;
    private static Radiogram rxg = null;
    
    
    //WZ COMMENTs: Imported variables END *********************
    
    //WZ COMMENTs: New variables START *********************
    private static final int BEACON_LED = 1; //WZ COMMENTs: Use LED 1 as indicator for beacon
    private int beaconNum = 0; //WZ COMMENTs: indicator used to indentify which corner/beacon we are at
    private boolean corner1Passed = false; //WZ COMMENTs: boolean to check whether we passed corner 1 or not
    private boolean corner2Passed = false;
    private boolean corner3Passed = false;
    private boolean corner4Passed = false;
    ITriColorLED led1 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED1");
    ITriColorLED led2 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED2");
    ITriColorLED led3 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED3");
    ITriColorLED led4 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED4");
    ITriColorLED led5 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED5");
    ITriColorLED led6 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED6");
    ITriColorLED led7 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED7");
    ITriColorLED led8 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED8");
    
    //WZ COMMENTs: Constants for speed
    private int speed1 = 50;
    private int speed2 = 100;
    private int speed3 = 200;


        //WZ COMMENTs: Variables basestation connection
    private static final String BS_RECEIVE_PORT = "88";
    private int command = 15;
    
    
    private static RadiogramConnection recvConnBS = null;
    private static Radiogram dgBS = null;
    //WZ COMMENTs: New variables END *********************
    
    
    public ServoSPOTonCar() 
    {

    }
    
    
        //WZ COMMENTs: Imported Function from TripwireRSSIReceiver START ***************
    /* Helper method for blinking LEDs the specified color. */
    private static void blinkLED(int ledA, LEDColor color) { //WZ COMMENTs: Modified to blink 1 LED only
        myLEDs.getLED(ledA).setColor(color);
        myLEDs.getLED(ledA).setOn();
        Utils.sleep(LED_ON);
        myLEDs.setOff();
    }
    private static void blinkLEDall(LEDColor color) {
        myLEDs.setColor(color);
        myLEDs.setOn();
        Utils.sleep(LED_ON);
        myLEDs.setOff();
    }
    
       /* Establish RadiogramConnections on the specified ports. */
    private static void setupConnection() {
        IRadioPolicyManager rpm = Spot.getInstance().getRadioPolicyManager();
        rpm.setOutputPower(-16);
        //rpm.setChannelNumber(BROADCAST_CHANNEL);
        try {
            //long ourAddr = RadioFactory.getRadioPolicyManager().getIEEEAddress();
            //System.out.println("Our radio address = " + IEEEAddress.toDottedHex(ourAddr));
            
            // Connection used for receiving beacon transmissions
            rxConnection = (RadiogramConnection) Connector.open("radiogram://:" + RECEIVE_PORT);
            rxg = (Radiogram) rxConnection.newDatagram(10);
            
            // blink white to confirm successful connection setup!
            //blinkLEDall(LEDColor.WHITE);
            
        } catch (IOException ex) {
            //blink red upon failure. :(
            //blinkLEDall( LEDColor.RED);
            
            System.err.println("Could not open radiogram broadcast connection!");
            System.err.println(ex);
        }
    }
    /* Wait for beacon transmissions, then forward them to the basestation. */
        //WZ COMMENTs: setupBaseStationConnection(): setup connection with base station
    //  Quesiton: Do I really need datagram or radiogram is fine? 
    private void setupBaseStationConnection() {
        try {
            recvConnBS = (RadiogramConnection) Connector.open("radiogram://:" + BS_RECEIVE_PORT);  
            //DatagramConnection recvConn = (DatagramConnection) Connector.open("radiogram://:" + BS_RECEIVE_PORT); 
           dgBS = (Radiogram) recvConnBS.newDatagram(recvConnBS.getMaximumLength()); 
            //Datagram dgBS = recvConnBS.newDatagram(recvConnBS.getMaximumLength()); 

        
        }
        catch (Exception ex) {
            System.err.println("Could not open radiogram broadcast connection!");
            System.err.println(ex);
        }
    }
    
    
    private void receiveLoop() { 
 
      while(true)
      {
        System.out.println("receiveLoop() running");
            try {
                //reset radiograms to clear transmission data
                rxg.reset();
                
                //variables for holding radiogram data
                int rssiValue;
                String spotAddress;
                
                //waits for a new transmission on RECEIVE_PORT
                rxConnection.receive(rxg);
                
                //read confirmation byte data from the radiogram
                byte checkByte = rxg.readByte();
                
                //check to see if radiogram is the right type
                if (checkByte == RSSI_CONFIRM_BYTE) {
                    //grab the RSSI and address info embedded in the radiogram
                    rssiValue = rxg.getRssi();
                    spotAddress = rxg.getAddress();
                    System.out.println("Received RSSI packet from: " + spotAddress  + ", RSSI: " + rssiValue+"\n\n\n");
                    
                    //blink color code to confirm successful send!
                    if (spotAddress.endsWith("7F48")){
                        blinkLED(1,LEDColor.BLUE);
                        beaconNum = 1;
                        locationSTATE=1;
                    } else if (spotAddress.endsWith("7E5D")){
                        blinkLED(1,LEDColor.MAGENTA);
                        beaconNum = 2;
                        locationSTATE=2;
                    } else if (spotAddress.endsWith("80F5")){
                        blinkLED(1,LEDColor.ORANGE);
                        beaconNum = 3;
                        locationSTATE = 3;
                    } else {
                        blinkLED(1,LEDColor.TURQUOISE);
                        beaconNum = 4;
                        locationSTATE=4;
                    }
                }
                else if (checkByte == TRIP_CONFIRM_BYTE) {
                    //grab the RSSI and address info embedded in the radiogram
                    rssiValue = rxg.getRssi();
                    spotAddress = rxg.getAddress();
                    System.out.println("Received Tripwire packet from: " + spotAddress  + ", RSSI: " + rssiValue);
                    
                    //blink color code to confirm successful send!
                    if (spotAddress.endsWith("7F48")){
                        blinkLED(1,LEDColor.BLUE);
                        beaconNum = 1; //WZ COMMENTs: Tell fcuntion turncorner() which beacon/corner it is 
                        
                    } else if (spotAddress.endsWith("7E5D")){
                        blinkLED(1,LEDColor.MAGENTA);
                        beaconNum = 2;
                        
                    } else if (spotAddress.endsWith("80F5")){
                        blinkLED(1,LEDColor.ORANGE);
                        beaconNum = 3;
                
                    } else {
                        blinkLED(1,LEDColor.TURQUOISE);
                        beaconNum = 4;
                      
                    }
                
                } else {
                    //blink red upon failure. :(
                    //blinkLEDall(LEDColor.RED);
                    beaconNum = 0;
                    System.out.println("Unrecognized radiogram type! Expected: " + RSSI_CONFIRM_BYTE + " or " + TRIP_CONFIRM_BYTE + ", Saw: " + checkByte);
                }
                
            } catch (Exception e) {
                //blinkr ed upon failure. :(
                //blinkLEDall(LEDColor.RED);
                System.err.println("Caught " + e + " while collecting/sending sensor sample.");
                System.err.println(e);
            }
        }
        
    }
    //WZ COMMENTs: Imported Function from TripwireRSSIReceiver END ***************
    
    
    private void BSreceiveLoop() {
        while(true) { //WZ COMMENTs: receiving loop for the basestation
            try {
                System.out.println("TRYING");
                dgBS.reset();
                recvConnBS.receive(dgBS);
                command = dgBS.readInt();
                switch(command)
                {
                   case 0:
                    stopper =0;
                    stop();
                    break;
                    case 2:
                      setSpeed(speed1);
                      break;
                      case 3:
                      setSpeed(speed2);
                      break;
                      case 4:
                      setSpeed(speed3);
                      break;

                }
                System.out.println("The received command is: " + command);
            }
            catch (IOException e) {
                System.err.println("Caught " + e +  " while reading sensor samples.");
                System.out.println("ERRROROROROR");
            }
        }
    }
    

    private void commandControl () {
            CommandLedOn(command);
            switch(command) {
                    
                case 0: //Stop                     
                    stop();
                    straight();  
                    System.out.println("Received command: STOP");
                    break;
                case 1: //Start
                    forward();
                    stopper =1;
                    System.out.println("Received command: START");

                    break;
                case 2: //Set Speed 1
                    System.out.println("Received command: SET SPEED 1");
                    setSpeed(speed1);
   
                    break;
                case 3: //Set Speed 2
                    System.out.println("Received command: SET SPEED 2");
                    setSpeed(speed2);

                    break;
                case 4: //Set Speed 3
                    System.out.println("Received command: SET SPEED 3");
                    setSpeed(speed3);
                       
                    break;
                case 5: //Backwards
                    System.out.println("Received command: BACKWARDs");
                    stopper =1;
                    backward();
                       
                    break;
                case 6: //Slight left
                    System.out.println("Received command: TURN LEFT");
                    leftS();
                    
                    break;
                case 7: //Slight right
                    System.out.println("Received command: TURN RIGHT");
                    rightS();

                    break;
                case 8: //Follow Left Wall
                    System.out.println("Received command: FOLLOW LEFT WALL");
                   
                    followLeftWall();
                    break;
                case 9: //Follow right wall
                    System.out.println("Received command: FOLLOW RIGHT WALL");
                    
                    followRightWall();
                    break;
                case 10: //Sharp Left
                    System.out.println("Received command: LEFT");
                    sharpLeft();
                    break;
                case 11: //Sharp Right
                    System.out.println("Received command: SHARP RIGHT");
                    sharpRight();
  
                    break;
                case 12: //Straight
       
                    System.out.println("Received command: STRAIGHT");
                    straight();
                    break;
                case 13:
    
                    break;
                case 14:
                   
                    break;    
                case 15:
                    //System.out.println("Received command: Initial");
                    break; 
                default:
                    //System.out.println("Received command: Initial or Error");
                    break;
            }
        
    }
    
    //WZ COMMENTs: CommandLedOff(): short function to set command leds (2-5) off
    private void CommandLedOff () {
        for (int i = 2; i < 6; i++) {
            myLEDs.getLED(i).setOff();
        }
    }
    
    //WZ COMMENTs: CommandLedOff(): short function to set command leds (2-5) off
    private void CommandLedOn (int commandNum) {
        CommandLedOff();
        for (int i = 2; i < 6; i++) {
            myLEDs.getLED(i).setColor(LEDColor.GREEN);
        }
        switch (commandNum) {
            case 0: //0000
                break; //All led should be off;
            case 1: //0001
                led5.setOn();
                break;
            case 2: //0010
                led4.setOn();
                break;
            case 3: //0011
                led4.setOn();
                led5.setOn();
                break;
            case 4: //0100
                led3.setOn();
                break;
            case 5: //0101
                led3.setOn();
                led5.setOn();
                break;
            case 6: //0110
                led3.setOn();
                led4.setOn();
                break;
            case 7: //0111
                led3.setOn();
                led4.setOn();
                led5.setOn();
                break;
            case 8: //1000
                led2.setOn();
                break;
            case 9: //1001
                led2.setOn();
                led5.setOn();
                break;
            case 10: //1010
                led2.setOn();
                led4.setOn();
                break;
            case 11: //1011
                led2.setOn();
                led4.setOn();
                led5.setOn();
                break;
            case 12: //1100
                led2.setOn();
                led3.setOn();
                break;
            case 13: //1101
                led2.setOn();
                led3.setOn();
                led5.setOn();
                break;
            case 14: //1110
                led2.setOn();
                led3.setOn();
                led4.setOn();
                break;
            case 15: //1111
                led2.setOn();
                led3.setOn();
                led4.setOn();
                led5.setOn();
                break;
        }
    }

    
    

    /*
    *
    *   BASIC STARTUP CODE  ---- This is where the code starts
    */
    
    protected void startApp() throws MIDletStateChangeException {
        BootloaderListenerService.getInstance().start();  

        System.out.println("Started");

        //WZ COMMENTs: Enable initializing for tilting
        initialize();  //initializes led
        //
        setupBaseStationConnection();//WZ COMMENTs: Continously listening to beacon signals 
  
        (new Thread() {
            public void run() {    
            System.out.println("AYYY ");  
               BSreceiveLoop();
            }
        }).start();
        
        
        
        straight();
        rFHistory = new double[10];
        rBHistory = new double[10];
        lFHistory = new double[10];
        lBHistory = new double[10];

        Utils.sleep(500);
        setServoForwardValue();

        boolean error = false;
        int cSignal = 0; //WZ COMMENTs: Enabled for tilting
        int go = 0;
        int firstTime = 0;
       
        while (true) {

            if(stopper ==0)
            {

                commandControl(); //WZ COMMENTs: Where should I put commandControl() for human control mode ??????????????????????????????????
            }
            else{

            }

            Utils.sleep(50);
         } //End of while       


 } // end of start app
        





        /*
        *
        *
        *    /////FUNCTIONS 
        *
        *
        * 
        */
        
    private void setServoForwardValue(){
        LRServoLeft = currentLR + stepLR;
        LRServoRight = currentLR - stepLR;
        FBServoForward = currentFB + stepFB;
        FBServoBack = currentFB - stepFB;
        if (stepFB == FBServo_HIGH) 
        {
            //velocityBlinker.setColor(LEDColor.GREEN);
        } 
        else if (stepFB == FBServo_LOW)
        {
            //velocityBlinker.setColor(LEDColor.BLUE);
        }
    }
    
    private void checkDirectionRight() throws IOException {
        initRB = 15;
        initRF = 15;
        double rF = getDistance(irRightF); 
        //System.out.println (" distance RF: "+ rF); 
        double rB = getDistance(irRightB); 
        //System.out.println (" distance RB: "+ rB);
        double lF = getDistance(irLeftF); 
        //System.out.println (" distance lF: "+ lF); 
        double lB = getDistance(irLeftB);  
        //System.out.println (" distance lB: "+ lB); 
        

        if(gapcount > 0)
        {
            if(locationSTATE ==0)
            {
                locationSTATE =1;
            }
            System.out.println("Reacting... GAP COUNT"+gapcount+"\n");
            stop();
             gapcount = 0;
            
           
        }
        else
        {
        rF = calibrateRF(rF);
        //rB = calibrateRB(rB);
     

        //int currently = (historyCounter % 10 )-1;
        //rFHistory[currently] = rF;
        //lFHistory[currently] = lF;
        //rBHistory[currently] = rB;
        //lBHistory[currently] = lB;

        if(rF < initRF) //needs to move to the left
        {
            left();
            Utils.sleep(300);
            straight();
            
        }
        else if(rF > initRF) //needs to move to the rihgt
        {
            right();
            Utils.sleep(300);
            straight();
       
        }
        else{ // keep moving forward
            straight();
        }
      }
       
    }    
       

   int oldbCount =0;
    private void checkDirectionLeft() throws IOException {
        initLF = 16;
        initLB = 16;
        double rF = getDistance(irRightF); 
        //System.out.println (" distance RF: "+ rF); 
        double rB = getDistance(irRightB); 
        //System.out.println (" distance RB: "+ rB);
        double lF = getDistance(irLeftF); 
        //System.out.println (" distance lF: "+ lF); 
        double lB = getDistance(irLeftB);  
        //System.out.println (" distance lB: "+ lB); 
        

        double oldF = lF;
        double oldB = lB;
        //lB = calibrateRF(lB);
        //System.out.println (" Calibrated lB: "+ lB); 
     
        

      /*  if (historyCounter == 0)
        {

        }
        else
        {
            int currently = (historyCounter % 10 )-1;
        }
        rFHistory[currently] = rF;
        lFHistory[currently] = lF;
        */
       
     if(gapcount >0 )
        {
            stop();
             gapcount = 0;
            //stateReact();
           

        }
        else
        {

               lF = calibrateLF(lF);
        System.out.println (" Calibrated lF: "+ lF); 
        if(lF < initLF) //needs to move to the right
        {

            right();
            Utils.sleep(300);
            straight();
           

        }
        else if(lF > initLF) //needs to move to the left
        {
            left();
            Utils.sleep(300);
            straight();
            
        }
        else{ // keep moving forward
            straight();
        }
       oldbCount++;
    }    
}

    public double calibrateRF(double in)
    {
        if(in > initRF +10)
        {
             
             gapcount++;
            return initRF;
          
        }

        return in;
    }


    public double calibrateLF(double in)
    {
        if(in > initLF +10)
        {
             gapcount++;
              System.out.println("\n\n\n\n\n\n\n\n GAP DETECTED count"+gapcount+"\n\n\n\n\n\n");
              return initLF;
         }


        return in;

    }
 
 
    private double getDistance(IAnalogInput analog) throws IOException {
        double volts = analog.getVoltage();
        double value = 18.67/(volts+0.167);
        //System.out.println (" distance is: "+value);
        return value;
    }

    private void straight() {
        
        //System.out.println("STRAIGHT");
        LRServo.setValue(SERVO_CENTER_VALUE);
        Utils.sleep(sleeptime);
    }

    private void left() {
        System.out.println("left");
        currentLR = LRServo.getValue();
        if (currentLR + stepLR < LRServo_MAX_VALUE){
        LRServo.setValue(currentLR+stepLR);
        Utils.sleep(sleeptime);
       // LRServo.setValue(currentLR);
       // Utils.sleep(sleeptime);
        } else{
        LRServo.setValue(LRServo_MAX_VALUE);
        Utils.sleep(sleeptime);
        }
        
 //       FBServo.setValue(0);
    }

    private void right() {
        //System.out.println("right");
        currentLR = LRServo.getValue();
        if (currentLR-stepLR > LRServo_MIN_VALUE){
        LRServo.setValue(currentLR-stepLR);
        Utils.sleep(sleeptime);
       // LRServo.setValue(currentLR);
        // Utils.sleep(sleeptime);
        } else{
        LRServo.setValue(LRServo_MIN_VALUE);
        Utils.sleep(sleeptime);
        }   

 //       FBServo.setValue(0);
    }

    private void sharpRight() {
        System.out.println("Sharp right");
        currentLR = LRServo.getValue();
        if (currentLR-stepLR > LRServo_MIN_VALUE){
        LRServo.setValue(LRServo_MIN_VALUE);
        Utils.sleep(sleeptime);
       // LRServo.setValue(currentLR);
        // Utils.sleep(sleeptime);
        } else{
        LRServo.setValue(LRServo_MIN_VALUE);
        Utils.sleep(sleeptime);
        }   

 //       FBServo.setValue(0);
    }
    
    private void sharpLeft() {
        System.out.println("Sharp right");
        currentLR = LRServo.getValue();
        if (currentLR-stepLR > LRServo_MAX_VALUE){
        LRServo.setValue(LRServo_MAX_VALUE);
        Utils.sleep(sleeptime);
       // LRServo.setValue(currentLR);
        // Utils.sleep(sleeptime);
        } else{
        LRServo.setValue(LRServo_MAX_VALUE);
        Utils.sleep(sleeptime);
        }   

 //       FBServo.setValue(0);
    }

    // AZIZ 
    // CODE to slightly turn right and left for case of passing open areas
    
     private void leftS() {
        System.out.println("left slight");
        currentLR = LRServo.getValue();
        if (currentLR + stepLR1 < LRServo_MAX_VALUE){
        LRServo.setValue(currentLR+stepLR1);
        Utils.sleep(sleeptime);
        } else{
        LRServo.setValue(LRServo_MAX_VALUE);
        Utils.sleep(sleeptime);
        }
        
        //FBServo.setValue(0);
    }

    private void rightS() {
        System.out.println("right slight");
        currentLR = LRServo.getValue();
        if (currentLR-stepLR1 > LRServo_MIN_VALUE){
        LRServo.setValue(currentLR-stepLR1);
        Utils.sleep(sleeptime);
        } else{
        LRServo.setValue(LRServo_MIN_VALUE);
        Utils.sleep(sleeptime);
        }   
 //       FBServo.setValue(0);
    }
    
    private void keep() {
        System.out.println("keeping position");
        currentLR = LRServo.getValue();
        //if (currentLR-stepLR1 > LRServo_MIN_VALUE){
        LRServo.setValue(currentLR);
        Utils.sleep(10);
        //} else{
        //LRServo.setValue(LRServo_MIN_VALUE);
        //Utils.sleep(50);
        //} 
 //       FBServo.setValue(0);
    }
    
    private void stop() {
//        System.out.println("stop");
   //     LRServo.setValue(0);
        if (FBServo.getValue() != FBServo_CENTER_VALUE) { //WZ COMMENTs: Stop only when the car is not stopped
            FBServo.setValue(FBServo_CENTER_VALUE);
        }
    }

    private void backward() {
        
        
        FBServo.setValue(FBServo_CENTER_VALUE + 100);
            
  /*     while(currentFB + stepFB <FBServo_MAX_VALUE){
        
         FBServo.setValue(currentFB+stepFB);
         currentFB= FBServo.getValue();
        Utils.sleep(50);
         
}*/
    }

    private void forward() {
        //System.out.println("forward");              
        FBServo.setValue(FBServo_CENTER_VALUE - 150);        
        
    }

      private void setSpeed(int in) {
        System.out.println("forward");              
        FBServo.setValue(FBServo_CENTER_VALUE - in);        
        
    }

    public void switchPressed(SwitchEvent sw) {
    }

    public void switchReleased(SwitchEvent sw) {
    // do nothing
    }
    
    protected void pauseApp() {
        // This will never be called by the Squawk VM
    }
    
    /**
     * Called if the MIDlet is terminated by the system.
     * I.e. if startApp throws any exception other than MIDletStateChangeException,
     * if the isolate running the MIDlet is killed with Isolate.exit(), or
     * if VM.stopVM() is called.
     * 
     * It is not called if MIDlet.notifyDestroyed() was called.
     *
     * @param unconditional If true when this method is called, the MIDlet must
     *    cleanup and release all resources. If false the MIDlet may throw
     *    MIDletStateChangeException  to indicate it does not want to be destroyed
     *    at this time.
     */
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        for (int i = 0; i < myLEDs.size(); i++) {
            myLEDs.getLED(i).setOff();
        }
    }
    
    private void followRightWall()
    {
 
                    forward();
                  //System.out.println("Following Right Wall");
                    //velocityBlinker.setColor(LEDColor.BLUE);
                    //progBlinker.setColor(LEDColor.BLUE);

                    try {
                        checkDirectionRight();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } 
    }
      private void followLeftWall()
    {

                    forward();
                    //System.out.println("Following Left Wall");
                    //System.out.println("Auto Mode");
                    //velocityBlinker.setColor(LEDColor.WHITE);
                    //progBlinker.setColor(LEDColor.WHITE);

                    try {
                        checkDirectionLeft();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                
    }

    private void stateReact()
    {

        switch (locationSTATE)
        {
            case 0:

            break;
            case 1:
            System.out.println("TURNING SHARP RIGHT");
                sharpRight();
                Utils.sleep(800);
                setSpeed(100);
                Utils.sleep(8500);
                locationSTATE = 3;
                followLeftWall();

            break;
            case 2:  //Ignore gap on left
             System.out.println("STATE 2 into 3");
                straight();
                Utils.sleep(800);
                forward();
                Utils.sleep(500);
                locationSTATE =3;
            break;
            case 3:
            System.out.println("STATE 3\n\n\n");
            sharpLeft();
            Utils.sleep(800);
            setSpeed(100);
             Utils.sleep(10000);
             straight();
             locationSTATE =4;
            break;
            case 4:
             straight();
                Utils.sleep(800);
                setSpeed(300);
                Utils.sleep(1000);
                setSpeed(200);
             locationSTATE =5;
            break;
            case 5:
         
            break;
        }
    }
    
    //WZ COMMENTs: Initializing function for tilting
    private void initialize() {
        System.out.println("Our radio address = " + IEEEAddress.toDottedHex(myAddr));
        /*
       
        for (int i = 0; i < myLEDs.size(); i++) {
            myLEDs.getLED(i).setColor(LEDColor.GREEN);
            myLEDs.getLED(i).setOn();
        }
        Utils.sleep(500);
        for (int i = 0; i < myLEDs.size(); i++) {
            myLEDs.getLED(i).setOff();
        }
                */
        servo.setValue(SERVO_CENTER_VALUE);
        esc.setValue(ESC_CENTER_VALUE);
    }
    
    
    
    
    
    
    
    
    

    
    
    
    
}