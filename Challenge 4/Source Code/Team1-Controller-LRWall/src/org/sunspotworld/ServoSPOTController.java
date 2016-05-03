/*
 * ServoSPOTController.java
 *
 * Created on Jul 19, 2012 9:46:39 PM;
 */

package org.sunspotworld;

import com.sun.spot.peripheral.radio.RadioFactory;
import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.resources.transducers.IAccelerometer3D;

import com.sun.spot.resources.transducers.LEDColor;
import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.ISwitch;
import com.sun.spot.resources.transducers.ISwitchListener; //WZ COMMENTs: 
import com.sun.spot.resources.transducers.ITriColorLED;
import com.sun.spot.resources.transducers.ITriColorLEDArray;
import com.sun.spot.resources.transducers.LEDColor;
import com.sun.spot.resources.transducers.SwitchEvent; //WZ COMMENTs:
import com.sun.spot.service.BootloaderListenerService;
import com.sun.spot.util.IEEEAddress;
import com.sun.spot.util.Utils;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import org.sunspotworld.lib.BlinkenLights;
import org.sunspotworld.lib.LedUtils;
import org.sunspotworld.common.Globals; //
import org.sunspotworld.common.TwoSidedArray; 

/**
 * This class is used to control a servo car remotely. This sends values
 * measured by demoboard accelerometer to the servo car.
 * 
 * LRou must specify buddyAddress, that is the SPOT address on the car to
 * communicate each other.
 * 
 * @author Tsuyoshi Miyake <Tsuyoshi.Miyake@Sun.COM>
 * @author LRuting Zhang<ytzhang@bu.edu>
 */
public class ServoSPOTController extends MIDlet  implements ISwitchListener{ //WZ COMMENTs: implements ISwitchListener

    private EDemoBoard eDemo = EDemoBoard.getInstance();
    private IAccelerometer3D accel = (IAccelerometer3D)Resources.lookup(IAccelerometer3D.class);
    private ITriColorLEDArray myLEDs = (ITriColorLEDArray) Resources.lookup(ITriColorLEDArray.class);
    private ISwitch sw1 = eDemo.getSwitches()[EDemoBoard.SW1]; //WZ COMMENTs: utilize switch 1 on the SPOT
    private ISwitch sw2 = eDemo.getSwitches()[EDemoBoard.SW2]; //WZ COMMENTs: utilize switch 2 on the SPOT
    private int FB = 0; //WZ COMMENTs: variable for forward and backward
    private int LR = 0; //WZ COMMENTs: Variable for left and right control signal
    
    private BlinkenLights blinker = new BlinkenLights();
    
    protected void startApp() throws MIDletStateChangeException {
        System.out.println("Hello, world");
        BootloaderListenerService.getInstance().start();  
        
        //WZ COMMENTs: Flash the LEDs (Green) to indicate start
        for (int i = 0; i < myLEDs.size(); i++) {
                        myLEDs.getLED(i).setColor(LEDColor.GREEN);
                        myLEDs.getLED(i).setOn();
                    }
        Utils.sleep(500);
        for (int i = 0; i < myLEDs.size(); i++) {
                        myLEDs.getLED(i).setOff(); 
                    }
        
        //BlinkenLights blinker = new BlinkenLights();
        blinker.startPsilon();
        
        String buddyAddress = getAppProperty("buddyAddress");
        if (buddyAddress == null) {
            throw new RuntimeException("the property buddyAddress must be set in the manifest");
        }
        TwoSidedArray controller = new TwoSidedArray(buddyAddress); 


        try {
            controller.startOutput();
         
        //    accel.setRestOffsets();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        blinker.setColor(LEDColor.BLUE);
        sw1.addISwitchListener(this);   //WZ COMMENTs:
        sw2.addISwitchListener(this);   //WZ COMMENTs:
        
        while (true) {
            try {
                controller.setVal(0, FB); //WZ COMMENTs:
                controller.setVal(1, LR); //WZ COMMENTs:   
                System.out.println("FB =: " + FB); //WZ COMMENTs: DEBUG DELETE
                System.out.println("LR =: " + LR); //WZ COMMENTs: DEBUG DELETE
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Utils.sleep(20);
        }
    }
    
    // WZ COMMENTs: press sw1 sw2 for follwing the left and right wall respectively
     public void switchPressed(SwitchEvent sw) {
         //WZ COMMENTs: press sw1 to follow the left wall
         if (sw.getSwitch() == sw1) {
             System.out.println("Left Wall Mode: ENABLED");
             blinker.setColor(LEDColor.GREEN);
             FB = 1;
             LR = 9999;
             //Utils.sleep(20);
         }
         
         //WZ COMMENTs: Use switch 2 to follow the right wall
         if (sw.getSwitch() == sw2) {
                //tilting = true;
                System.out.println("Right Wall Mode: ENABLED");
                blinker.setColor(LEDColor.YELLOW);
                FB = 1;
                LR = -9999;

             }              
     }
     
     //WZ COMMENTs: Release sw1 and sw2 to stop
    public void switchReleased(SwitchEvent se) {
        if (se.getSwitch ()== sw1) {
            FB = 0;
            LR = 0;
            System.out.println("Left Wall mode: DISABLED");
            blinker.setColor(LEDColor.BLUE);  
        }
        if (se.getSwitch() == sw2) {
            //tilting = false;
            FB = 0;
            LR = 0;
            System.out.println("Right Wall mode: DISABLED");
            blinker.setColor(LEDColor.BLUE);            
        }
    } 




    protected void pauseApp() {
        // This is not currently called by the Squawk VM
    }

    /**
     * Called if the MIDlet is terminated by the system.
     * It is not called if MIDlet.notifyDestroyed() was called.
     *
     * @param unconditional If true the MIDlet must cleanup and release all resources.
     */
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        LedUtils.setOffAll();
    }


}
