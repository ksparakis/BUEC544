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
    private static final int SERVO2_CENTER_VALUE = 1500;
    private static final int SERVO1_MAX_VALUE = 2000;
    private static final int SERVO1_MIN_VALUE = 1000;
    private static final int SERVO2_MAX_VALUE = 2000;
    private static final int SERVO2_MIN_VALUE = 100;
    private static final int SERVO1_HIGH = 200;
    private static final int SERVO1_LOW = 90; // AZIZ
    
    private static final int SERVO1_VLOW = 20; // AZIZ
    
    private static final int SERVO2_HIGH = 100;
    private static final int SERVO2_LOW = 25;
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
    private ITriColorLEDArray myLEDs = (ITriColorLEDArray) Resources.lookup(ITriColorLEDArray.class);
    
    // 1st servo for left & right direction 
    private Servo servo1 = new Servo(eDemo.getOutputPins()[EDemoBoard.H0]); // fliped Aziz
    // 2nd servo for forward & backward direction
    private Servo servo2 = new Servo(eDemo.getOutputPins()[EDemoBoard.H1]); // fliped Aziz
    private BlinkenLights progBlinker = new BlinkenLights(1, 3);
    private BlinkenLights velocityBlinker = new BlinkenLights(4, 6);
    private int current1 = SERVO_CENTER_VALUE;
    private int current2 = SERVO2_CENTER_VALUE;
    private int step1 = SERVO1_LOW;
    private int step11 = SERVO1_VLOW; // AZIZ
    private int step2 = SERVO2_LOW;
    
    //WZ COMMENTs: Devices for tilting
        // steering servo for left & right direction 
    private Servo servo = new Servo(eDemo.getOutputPins()[EDemoBoard.H1]);
    // esc for forward & backward direction
    private Servo esc = new Servo(eDemo.getOutputPins()[EDemoBoard.H0]);
    
//    private int servo1ForwardValue;
//    private int servo2ForwardValue;
    private int servo1Left = SERVO_CENTER_VALUE +SERVO1_LOW;
    private int servo1Right = SERVO_CENTER_VALUE -SERVO1_LOW;
    private int servo2Forward = SERVO2_CENTER_VALUE +SERVO2_LOW;
    private int servo2Back = SERVO2_CENTER_VALUE - SERVO2_LOW;
    
                 
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
             
    public ServoSPOTonCar() 
    {

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
        straight();
        rFHistory = new double[10];
        rBHistory = new double[10];
        lFHistory = new double[10];
        lBHistory = new double[10];
        for (int i = 0; i < myLEDs.size(); i++) {
                        myLEDs.getLED(i).setColor(LEDColor.GREEN);
                        myLEDs.getLED(i).setOn();
                    }

        Utils.sleep(500);
        setServoForwardValue();
        progBlinker.startPsilon();
        velocityBlinker.startPsilon();
        // timeout 1000
        

        TwoSidedArray robot = new TwoSidedArray(getAppProperty("buddyAddress"), Globals.READ_TIMEOUT);
        try {
            robot.startInput();
        } catch (Exception e) {
            e.printStackTrace();
        }

        velocityBlinker.setColor(LEDColor.BLUE);
        progBlinker.setColor(LEDColor.BLUE);

        boolean error = false;
        int cSignal = 0; //WZ COMMENTs: Enabled for tilting
        int go = 0;
        int firstTime = 0;
        while (true) {

            if(firstTime == 0)
            {
                //Sets initial values for distance from walls
                try {
                        
                    initRF = getDistance(irRightF);
                    System.out.println (" init distance RF: "+ initRF); 
                    initRB = getDistance(irRightB); 
                    System.out.println (" init distance RB: "+initRB); 
                    
                    initLF = getDistance(irLeftF);  
                      System.out.println (" init distance LF: "+initLF);  
                    initLB = getDistance(irLeftB); 
                      System.out.println (" init distance LB: "+initLB); 
                   
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                  
                     firstTime = 1;
            }

            boolean timeoutError = robot.isTimeoutError();

            if (!timeoutError) 
            {
            
          

              cSignal = robot.getVal(1);
              
                if (error) {
                   
                    cSignal = 0; //WZ COMMENTs: signal in
                    step1 = SERVO1_LOW;
                    step11 = SERVO1_VLOW; //AZIZ: for 4way IR
                    step2 = SERVO2_LOW;
                    velocityBlinker.setColor(LEDColor.BLUE);
                    progBlinker.setColor(LEDColor.BLUE);
                    error = false;
                }
                

                //Code for figuring out what movements to do
     /////////////////////////////////////////////////////////////////
                if (cSignal == 9999){ // FOLLOW LEFT WALL
                    forward();
                    System.out.println("Following Left Wall");
                    //System.out.println("Auto Mode");
                    velocityBlinker.setColor(LEDColor.WHITE);
                    progBlinker.setColor(LEDColor.WHITE);

                    try {
                        checkDirectionLeft();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                  

                } 
                else if (cSignal == -9999) // RIGHT FOLLOW MODE
                { 
                    forward();
                  System.out.println("Following Right Wall");
                    velocityBlinker.setColor(LEDColor.BLUE);
                    progBlinker.setColor(LEDColor.BLUE);

                    try {
                        checkDirectionRight();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } 
                }
                else
                {
                    //Nothing is happeningin here
                    stop();
                }
            
        } // END OF THE IF ERROR TIMEOUT 
        else 
        {
                velocityBlinker.setColor(LEDColor.RED); 
                progBlinker.setColor(LEDColor.RED);
                error = true;
                stop();
        }

            Utils.sleep(50);
    } //End of while       
        


    } // end of start app
        





        /*
        *
        *
        *           /////FUNCTIONS 
        *
        *
        * 
        */
        
    private void setServoForwardValue(){
        servo1Left = current1 + step1;
        servo1Right = current1 - step1;
        servo2Forward = current2 + step2;
        servo2Back = current2 - step2;
        if (step2 == SERVO2_HIGH) {
            velocityBlinker.setColor(LEDColor.GREEN);
        } else if (step2 == SERVO2_LOW) {
            velocityBlinker.setColor(LEDColor.BLUE);
        }
    }
    
    private void checkDirectionRight() throws IOException {
        initRB = 15;
        initRF = 15;
        double rF = getDistance(irRightF); 
        System.out.println (" distance RF: "+ rF); 
        double rB = getDistance(irRightB); 
        System.out.println (" distance RB: "+ rB);
        double lF = getDistance(irLeftF); 
        System.out.println (" distance lF: "+ lF); 
        double lB = getDistance(irLeftB);  
        System.out.println (" distance lB: "+ lB); 
        
        rF = calibrateRF(rF);
        rB = calibrateRB(rB);
     

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
       

   int oldbCount =0;
    private void checkDirectionLeft() throws IOException {
        initLF = 16;
        initLB = 16;
        double rF = getDistance(irRightF); 
        System.out.println (" distance RF: "+ rF); 
        double rB = getDistance(irRightB); 
        System.out.println (" distance RB: "+ rB);
        double lF = getDistance(irLeftF); 
        System.out.println (" distance lF: "+ lF); 
        double lB = getDistance(irLeftB);  
        System.out.println (" distance lB: "+ lB); 
        

        double oldF = lF;
        double oldB = lB;
        lB = calibrateRF(lB);
        System.out.println (" Calibrated lB: "+ lB); 
        lF = calibrateLF(lF);
        System.out.println (" Calibrated lF: "+ lF); 
        

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

    public double calibrateRF(double in)
    {
        if(in > initRF +10)
        {
             

            return initRF;
          
        }

        return in;
    }


    public double calibrateLF(double in)
    {
        if(in > initLF +10)
        {
             gapcount++;
              if(gapcount %2 ==0)
              {
                    right();
                    Utils.sleep(500);

              }
              System.out.println("\n\n\n\n\n\n\n\n GAP DETECTED count"+gapcount+"\n\n\n\n\n\n");
        return initLF;
         }
         if(gapcount > 1)
         {
            //we are on second wall
            System.out.println("\n ON second wall \n");
         }
        return in;

    }
 

    public double calibrateRB(double in)
    {
           if(in > initRB +10)
           {
            return initRB;
           }

        return in;

    }
 

    public double calibrateLB(double in)
    {
        if(in > initLB +10)
        {
        return initLB;
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
        
        System.out.println("STRAIGHT");
        servo1.setValue(SERVO_CENTER_VALUE);
        Utils.sleep(sleeptime);
    }

    private void left() {
        System.out.println("left");
        current1 = servo1.getValue();
        if (current1 + step1 < SERVO1_MAX_VALUE){
        servo1.setValue(current1+step1);
        Utils.sleep(sleeptime);
       // servo1.setValue(current1);
       // Utils.sleep(sleeptime);
        } else{
        servo1.setValue(SERVO1_MAX_VALUE);
        Utils.sleep(sleeptime);
        }
        
 //       servo2.setValue(0);
    }

    private void right() {
        System.out.println("right");
        current1 = servo1.getValue();
        if (current1-step1 > SERVO1_MIN_VALUE){
        servo1.setValue(current1-step1);
        Utils.sleep(sleeptime);
       // servo1.setValue(current1);
        // Utils.sleep(sleeptime);
        } else{
        servo1.setValue(SERVO1_MIN_VALUE);
        Utils.sleep(sleeptime);
        }   
 //       servo2.setValue(0);
    }
    

    // AZIZ 
    // CODE to slightly turn right and left for case of passing open areas
    
     private void leftS() {
        System.out.println("left slight");
        current1 = servo1.getValue();
        if (current1 + step11 < SERVO1_MAX_VALUE){
        servo1.setValue(current1+step11);
        Utils.sleep(sleeptime);
        } else{
        servo1.setValue(SERVO1_MAX_VALUE);
        Utils.sleep(sleeptime);
        }
        
 //       servo2.setValue(0);
    }

    private void rightS() {
        System.out.println("right slight");
        current1 = servo1.getValue();
        if (current1-step11 > SERVO1_MIN_VALUE){
        servo1.setValue(current1-step11);
        Utils.sleep(sleeptime);
        } else{
        servo1.setValue(SERVO1_MIN_VALUE);
        Utils.sleep(sleeptime);
        }   
 //       servo2.setValue(0);
    }
    
    private void keep() {
        System.out.println("keeping position");
        current1 = servo1.getValue();
        //if (current1-step11 > SERVO1_MIN_VALUE){
        servo1.setValue(current1);
        Utils.sleep(10);
        //} else{
        //servo1.setValue(SERVO1_MIN_VALUE);
        //Utils.sleep(50);
        //} 
 //       servo2.setValue(0);
    }
    
    private void stop() {
//        System.out.println("stop");
   //     servo1.setValue(0);
        servo2.setValue(SERVO2_CENTER_VALUE);
    }

    private void backward() {
        
        
        servo2.setValue(SERVO2_CENTER_VALUE + 300);
            
  /*     while(current2 + step2 <SERVO2_MAX_VALUE){
        
         servo2.setValue(current2+step2);
         current2= servo2.getValue();
        Utils.sleep(50);
         
}*/
    }

    private void forward() {
        System.out.println("forward");              
        servo2.setValue(SERVO2_CENTER_VALUE - 200);        
        
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
    
    
    //WZ COMMENTs: Initializing function for tilting
    private void initialize() {
        System.out.println("Our radio address = " + IEEEAddress.toDottedHex(myAddr));
        for (int i = 0; i < myLEDs.size(); i++) {
            myLEDs.getLED(i).setColor(LEDColor.GREEN);
            myLEDs.getLED(i).setOn();
        }
        Utils.sleep(500);
        for (int i = 0; i < myLEDs.size(); i++) {
            myLEDs.getLED(i).setOff();
        }
        servo.setValue(SERVO_CENTER_VALUE);
        esc.setValue(ESC_CENTER_VALUE);
    }
    
}

