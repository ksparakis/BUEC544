/*
 * SunSpotApplication.java
 *
 * Created on Nov 15, 2012 12:32:28 PM;
 */



//WZ COMMENTs: ToDo list:
//  1. Fix the speed problem
//  2. Further calibrate the IR sensor values
//  3. Need better gap detection
//  4. Improve wall following

package org.sunspotworld;

import com.sun.spot.peripheral.radio.RadioFactory;
import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.IAnalogInput;
import com.sun.spot.resources.transducers.ILightSensor;
import com.sun.spot.resources.transducers.ISwitch;
import com.sun.spot.resources.transducers.ITriColorLED;
import com.sun.spot.resources.transducers.ITriColorLEDArray;
import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.Servo;
import com.sun.spot.service.BootloaderListenerService;
import com.sun.spot.util.IEEEAddress;
import com.sun.spot.util.Utils;
import java.io.IOException;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

//WZ COMMENTs: Imported imports START *********************
import javax.microedition.io.Connector;  
import com.sun.spot.io.j2me.radiogram.*; 
import com.sun.spot.peripheral.Spot; 
import com.sun.spot.resources.transducers.LEDColor; 
import com.sun.spot.peripheral.radio.IRadioPolicyManager; 
import java.lang.String; 
//WZ COMMENTs: Imported imports END *********************

/**
 * The startApp method of this class is called by the VM to start the
 * application.
 *
 * The manifest specifies this class as MIDlet-1, which means it will be
 * selected for execution.
 */
public class CourseCar extends MIDlet {

    private static final int SERVO_CENTER_VALUE = 1500;
    private static final int FB_SERVO_HIGH = 50;
    private static final int LR_SERVO_HIGH = 500;
    private int turningPeriod = 3000;
    private int lightIndication = 0;         //ranges from 0 - 740
    private int environLight = 100;
    private double baselineRight = 0;
    private double baselineLeft = 0;
    private EDemoBoard eDemo = EDemoBoard.getInstance();
    private IAnalogInput rightSensor = eDemo.getAnalogInputs()[EDemoBoard.A0];
    private IAnalogInput leftSensor = eDemo.getAnalogInputs()[EDemoBoard.A1];
    private IAnalogInput frontSensor = eDemo.getAnalogInputs()[EDemoBoard.A2];
    private ISwitch sw1 = (ISwitch) Resources.lookup(ISwitch.class, "SW1");
    private ISwitch sw2 = (ISwitch) Resources.lookup(ISwitch.class, "SW2");
    private Servo frontBackServo = new Servo(eDemo.getOutputPins()[EDemoBoard.H0]);
    private Servo leftRightServo = new Servo(eDemo.getOutputPins()[EDemoBoard.H1]);
    private static final ITriColorLEDArray leds = (ITriColorLEDArray) Resources.lookup(ITriColorLEDArray.class); //WZ COMMENTs: changed to static final
    private ILightSensor light = eDemo.getLightSensor();
    
    
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
    
    private boolean sw1Pressed = false;
    
    private int leftGapcount = 0; 
    private int rightGapcount = 0; 
    private boolean LeftGap = false;
    private boolean RightGap = false;

    //WZ COMMENTs: New variables END *********************

    //WZ COMMENTs: Imported Function from TripwireRSSIReceiver START ***************
    /* Helper method for blinking LEDs the specified color. */
    private static void blinkLED(int ledA, LEDColor color) { //WZ COMMENTs: Modified to blink 1 LED only
        leds.getLED(ledA).setColor(color);
        leds.getLED(ledA).setOn();
        Utils.sleep(LED_ON);
        leds.setOff();
    }
    private static void blinkLEDall(LEDColor color) {
        leds.setColor(color);
        leds.setOn();
        Utils.sleep(LED_ON);
        leds.setOff();
    }
    
       /* Establish RadiogramConnections on the specified ports. */
    private static void setupConnection() {
        IRadioPolicyManager rpm = Spot.getInstance().getRadioPolicyManager();
        rpm.setOutputPower(-16);
        rpm.setChannelNumber(BROADCAST_CHANNEL);
        try {
            //long ourAddr = RadioFactory.getRadioPolicyManager().getIEEEAddress();
            //System.out.println("Our radio address = " + IEEEAddress.toDottedHex(ourAddr));
            
            // Connection used for receiving beacon transmissions
            rxConnection = (RadiogramConnection) Connector.open("radiogram://:" + RECEIVE_PORT);
            rxg = (Radiogram) rxConnection.newDatagram(10);
            
            // blink white to confirm successful connection setup!
            blinkLEDall(LEDColor.WHITE);
            
        } catch (IOException ex) {
            //blink red upon failure. :(
            blinkLEDall( LEDColor.RED);
            
            System.err.println("Could not open radiogram broadcast connection!");
            System.err.println(ex);
        }
    }
    /* Wait for beacon transmissions, then forward them to the basestation. */
    
    
    
    private void receiveLoop() {    
        while (true) {
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
                    System.out.println("Received RSSI packet from: " + spotAddress  + ", RSSI: " + rssiValue);
                    
                    //blink color code to confirm successful send!
                    if (spotAddress.endsWith("7F48")){
                        blinkLED(2,LEDColor.GREEN);
                        beaconNum = 1;
                    } else if (spotAddress.endsWith("7E5D")){
                        blinkLED(3,LEDColor.MAGENTA);
                        beaconNum = 2;
                    } else if (spotAddress.endsWith("80F5")){
                        blinkLED(4,LEDColor.ORANGE);
                        beaconNum = 3;
                    } else {
                        blinkLED(5,LEDColor.TURQUOISE);
                        beaconNum = 4;
                    }
                }
                else if (checkByte == TRIP_CONFIRM_BYTE) {
                    //grab the RSSI and address info embedded in the radiogram
                    rssiValue = rxg.getRssi();
                    spotAddress = rxg.getAddress();
                    System.out.println("Received Tripwire packet from: " + spotAddress  + ", RSSI: " + rssiValue);
                    
                    //blink color code to confirm successful send!
                    if (spotAddress.endsWith("7F48")){
                        blinkLED(2,LEDColor.GREEN);
                        beaconNum = 1; //WZ COMMENTs: Tell fcuntion turncorner() which beacon/corner it is 
                    } else if (spotAddress.endsWith("7E5D")){
                        blinkLED(3,LEDColor.MAGENTA);
                        beaconNum = 2;
                    } else if (spotAddress.endsWith("80F5")){
                        blinkLED(4,LEDColor.ORANGE);
                        beaconNum = 3;
                    } else {
                        blinkLED(5,LEDColor.TURQUOISE);
                        beaconNum = 4;
                    }
                
                } else {
                    //blink red upon failure. :(
 //                   blinkLEDall(LEDColor.RED);
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
    
    
    protected void startApp() throws MIDletStateChangeException {
        
        
        BootloaderListenerService.getInstance().start();   // monitor the USB (if connected) and recognize commands from host
        
        setupConnection(); //WZ COMMENTs: 
        
        (new Thread() {
            public void run() {
                receiveLoop();//WZ COMMENTs: Continously listening to beacon signals 
            }
        }).start();
        
        long ourAddr = RadioFactory.getRadioPolicyManager().getIEEEAddress();
        System.out.println("Our radio address = " + IEEEAddress.toDottedHex(ourAddr));

        ITriColorLED led = leds.getLED(0);
        while (true) {
            
            led.setRGB(100, 0, 0);                    // set color to moderate red
            //System.out.println("Start listen to the switch");
            while (sw1.isClosed()) { //WZ COMMENTs: While sw1 is pressed
                blinkLed(led);
                sw1Pressed = !sw1Pressed;
                if (sw1Pressed) {
                    System.out.println("Car Start");
                    leds.getLED(1).setColor(LEDColor.GREEN);
                    leds.getLED(1).setOn();
                    stop();// wait 1 second
                    goStraight(); //WZ COMMENTs: Set up wheels straight
                }

            }
            recordBaseline();

                if (sw1Pressed) {

                    //System.out.println("Ready"); 

                    if (checkCorner()) { //WZ COMMENTs: If a beacon is detected
                        turnCorner(); //WZ COMMENTs: turn left
                    } else if (getFrontValue() < 17) {
                        backUp();
                    } else {
                        System.out.println(baselineRight + " " + getRightValue());
                        //WZ COMMENTs: Wall avoiding strategy is adopted for simplicity reason, may need calibration or change to wall following !!!!!!!!!!!!!!!!
                        if (getLeftValue() < baselineLeft - 10) {
                            turnWheelRight(); //WZ COMMENTs: No sleep, so bend right
                        } else if (getRightValue() < baselineRight - 10) {
                            turnWheelLeft(); //WZ COMMENTs: No sleep, so bend left
                        } else {
                            goStraight(); //WZ COMMENTs: Set the leftright servo to center value
                        }
                    }
                    
                    goForward();
                    blinkLed(led);
                }
                else {
                    leds.getLED(1).setColor(LEDColor.RED);
                    leds.getLED(1).setOn();
                    stop();// wait 1 second
                    goStraight(); //WZ COMMENTs: Set up wheels straight
                    System.out.println("Stop"); 
                    
                }
                //receiveLoop(); //WZ COMMENTs: Continously listening to beacon signals 


            
        }
    }

    protected void pauseApp() {
        // This is not currently called by the Squawk VM
    }

    //WZ COMMENTs: backup()
    //  check left and right, turn the wheels if necessary
    //  backup for half of the turning period
    //  stop the car then go straight
    private void backUp() {
        System.out.println("REVERSE");
        stop();
        Utils.sleep(250);
        System.out.println("Turn values" + getLeftValue() + " " + getRightValue());
        if(getLeftValue() < getRightValue() - 15) {
            turnWheelLeft();
        } else if (getRightValue() < getLeftValue() -15) {
            turnWheelRight();
        } else {
            goStraight();
        }
        Utils.sleep(500);
        long time = System.currentTimeMillis() + turningPeriod/2;

        while (System.currentTimeMillis() < time) {
            goBackward();
            Utils.sleep(250);
        }
        stop();
        goStraight();
        Utils.sleep(250);
    }

    /*WZ COMMENTs: checkCorner
    //  Light Sensor is used ()
    private boolean checkCorner() {
        try {
            lightIndication = light.getValue();         //ranges from 0 - 740
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (lightIndication > environLight + LIGHT_STEP) {

            leds.getLED(5).setRGB(100, 0, 0);
            leds.getLED(5).setOn();
            return true;
        } else {
            leds.getLED(5).setOff();
            return false;
        }
    }
    */
    
    //WZ COMMENTs: New function checkCorner
    //  check RSSI from beacon
    //  check reading from left or right IR to determine whether it is a corner
    //  give correct command to function turnCorner
    
    //WZ COMMENTs: TODO:  NEED TO INTEGRATE from TripwireRSSIReceiver & MODIFY to check the corners *************************
    private boolean checkCorner() {
        if (beaconNum == 1) {
            if (corner1Passed) { //WZ COMMENTs: case when coming back and meet beacon 1 again
                return false;
            }
            else {
                if (!gapDetection()) { //WZ COMMENTs: If we have not yet reached the gap/corner
                    System.out.println("Entering corner " + beaconNum);
                    return false;
                }
                else {
                    System.out.println("Ready to turn RIGHT at corner " + beaconNum);
                    corner1Passed = true;
                    return true;
                }
            }
        }
        else if (beaconNum == 2) {
                if (!gapDetection()) { //WZ COMMENTs: If we have not yet reached the gap/corner
                    System.out.println("Entering corner " + beaconNum);
                    return false;
                }
                else {
                    System.out.println("Ready to turn LEFT at corner " + beaconNum);
                    return true;
                }
        } 
        else if (beaconNum == 3) {
                if (!gapDetection()) { //WZ COMMENTs: If we have not yet reached the gap/corner
                    System.out.println("Entering corner " + beaconNum);
                    return false;
                }
                else {
                    System.out.println("Ready to turn LEFT at corner " + beaconNum);
                    return true;
                }
        }
        else if (beaconNum == 4) {
                if (!gapDetection()) { //WZ COMMENTs: If we have not yet reached the gap/corner
                    System.out.println("Entering corner " + beaconNum);
                    return false;
                }
                else {
                    System.out.println("Ready to turn LEFT at corner " + beaconNum);
                    return true;
                }
        }
        else {
            if (beaconNum == 0) {
                //System.out.println("ERROR: beaconNumber = 0 in function checkCorner() " );
                return false;
            }
            else {
                System.out.println("ERROR: Wrong beaconNumber in function checkCorner()" + beaconNum);
                return false;
            }
        }
    }    
    
    //WZ COMMENTs:
    //  NEW function: TODO: Integrate the gap Detection we had from challange 4 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private  boolean gapDetection() {
        
        if (getLeftValue() > 70) {
            leftGapcount++;
            if (leftGapcount > 5) {
                LeftGap = true;
                return true;
            }
            else {
                LeftGap = false;
            } 
        }
        
        if (getRightValue() > 70) {
            rightGapcount++;
            if (rightGapcount > 5) {
                RightGap = true;
                return true;
            }
            else {
                RightGap = false;
            } 
        }
        return false;
    }
    


    /*/WZ COMMENTs: 
    //  stop the car, then turn left for a turning period
    //  set the wheels straight
    private void turnCorner() {
        System.out.println("Ermahgerd Light! Turnin nowh");
        stop();
        Utils.sleep(500);
        turnLeft();
        Utils.sleep(1000);

        long time = System.currentTimeMillis() + turningPeriod;

        while (System.currentTimeMillis() < time) {
            goForward();
            Utils.sleep(250);
        }
        goStraight();
    }
    */
    
    //WZ COMMENTs: new function turnCorner
    //  Turn left or right depends on the beacon number
    //
    private void turnCorner() {
        if (beaconNum == 1) {
            stop();
            Utils.sleep(500);          
            turnWheelRight();
            System.out.println("Turning RIGHT at corner " + beaconNum);
            Utils.sleep(1000);
        }
        else if (beaconNum == 2) {
            stop();
            Utils.sleep(500);
            turnWheelLeft();
            System.out.println("Turning LEFT at corner " + beaconNum);
            Utils.sleep(1000);
        } 
        else if (beaconNum == 3) {
            stop();
            Utils.sleep(500);
            turnWheelLeft();
            System.out.println("Turning LEFT at corner " + beaconNum);
            Utils.sleep(1000);
        }
        else if (beaconNum == 4) {
            stop();
            Utils.sleep(500);
            turnWheelLeft();
            System.out.println("Turning LEFT at corner " + beaconNum);
            Utils.sleep(1000);
        }
        else {
            if (beaconNum == 0) {
                System.out.println("ERROR: beaconNumber = 0 in function turnCorner() " );
            }
            else {
                System.out.println("ERROR: Wrong beaconNumber in function turnCorner()" + beaconNum);
            }
        }
        long time = System.currentTimeMillis() + turningPeriod;
        while (System.currentTimeMillis() < time) {
            goForward();
            Utils.sleep(250);
        }
        beaconNum = 0; //WZ COMMENTs: set beaconNum = 0 after the turning is completed
        goStraight();
    }

    private void turnWheelLeft() {
        leftRightServo.setValue(SERVO_CENTER_VALUE + LR_SERVO_HIGH);
    }

    private void turnWheelRight() {
        leftRightServo.setValue(SERVO_CENTER_VALUE - LR_SERVO_HIGH);
    }

    private void goStraight() {
        leftRightServo.setValue(SERVO_CENTER_VALUE);
    }

    private void goForward() {
        //System.out.println("Forward");
        frontBackServo.setValue(SERVO_CENTER_VALUE - FB_SERVO_HIGH);
    }

    private void goBackward() {
        //System.out.println("Backward");
        frontBackServo.setValue(SERVO_CENTER_VALUE + FB_SERVO_HIGH);
    }

    private void stop() {
        frontBackServo.setValue(SERVO_CENTER_VALUE);
        try {
            Thread.sleep(300);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    //WZ COMMENTs: getFrontValue()
    //  get value from the sensor up front
    //  ATTENTION: Calibration may required !!!!
    private int getFrontValue() {
        double value = 0;
        for (int ii = 0; ii < 5; ii++) {
            try {
                double volts = frontSensor.getVoltage();
                value += (18.67 / (volts + 0.167));
            } catch (IOException ex) {
                ex.printStackTrace();
                return 10000;
            }
        }
        return (int) (value/5);
        
    }
    
    //WZ COMMENTs: getLeftValue()
    //  get value from the sensor on the left
    //  ATTENTION: Calibration may required !!!!
    private double getLeftValue() {
        double value = 0;
        
        /*
        for (int ii = 0; ii < 5; ii++) {
            try {
                value += (leftSensor.getVoltage() / .009766 * 2.4);
                
            } catch (IOException ex) {
                ex.printStackTrace();
                return -1;
            }
        }
        return (int) (value / 5);
        */
        try {
            value = 18.67/(leftSensor.getVoltage()+0.167);
                
        } catch (IOException ex) {
            ex.printStackTrace();
            return -1;
        }
        
        return value;
    }

    //WZ COMMENTs: getRightValue()
    //  get value from the sensor on the right
    //  ATTENTION: Calibration may required !!!!
    private double getRightValue() {
        double value = 0;
        
        /*
        for (int ii = 0; ii < 5; ii++) {
            try {
                value += (leftSensor.getVoltage() / .009766 * 2.4);
                
            } catch (IOException ex) {
                ex.printStackTrace();
                return -1;
            }
        }
        return (int) (value / 5);
        */
        try {
            value = 18.67/(leftSensor.getVoltage()+0.167);
                
        } catch (IOException ex) {
            ex.printStackTrace();
            return -1;
        }
        
        return value;
    }

    
    //WZ COMMENTs: recordBaseline()
    //  Do truncation on the values read from left and right
    private void recordBaseline() {
        baselineLeft = getLeftValue();
        baselineRight = getRightValue();

        if (baselineLeft < 0) {
            baselineLeft = 0;
        }
        if (baselineRight < 0) {
            baselineRight = 0;
        }
    }


    /**
     * Called if the MIDlet is terminated by the system. It is not called if
     * MIDlet.notifyDestroyed() was called.
     *
     * @param unconditional If true the MIDlet must cleanup and release all
     * resources.
     */
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
    }

    private void blinkLed(ITriColorLED led) {
        led.setOn();
        Utils.sleep(250);
        led.setOff();
        Utils.sleep(250);
    }
    
    
    

    
}


