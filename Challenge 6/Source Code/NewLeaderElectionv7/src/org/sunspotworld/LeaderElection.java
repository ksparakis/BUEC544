

/**
 * Routines to turn multiple SPOTs broadcasting their information and discover
 * peers in the same PAN
 *
 * The SPOT uses the LEDs to display its status as follows:
 * LED 0:
 * Red = missed an expected packet
 * Green = received a packet
 *
 * LED 1-5:
 * Leader displayed count of connected spots in green
 * 
 * LED 6:
 * display TiltChange flag when sw1 is pressed
 * Blue = false
 * Green = true
 * 
 * LED 7:
 * Red = right tilt 
 * Blue = left tilt
 *
 * Press right witch to change neighbors' tilt state:
 * by sending out tilt change flag in the datagram
 * SW1 = change neighbors' tilt
 * 
 * Switch 2 right now is used to adjust transmitting power
 *
 * Note: Each group need to use their own channels to avoid interference from others
 * channel 26 is default
 * Assignment of channels to groups 1- 10:  Valid range is 11 to 26, 
 * Group 1: 11 �C 12
 * Group 2: 13 �C 14
 * Group 3: 15 �C 16 
 * Group 4: 17 �C 18 
 * Group 6: 19 �C 20
 * Group 7: 21 �C 22
 * Group 8: 23 �C 24
 * Group 9: 25
 * Group 10: 26

 * Assignment of port numbers: (0-31 are reserved for system use) 32 �C 255 are valid. 
 * Group 1: Ports 110-119
 * Group 2: Ports 120-129
 * Group 3: Ports 130-139
 * Group 4: Ports 140-149
 * Group 6: Ports 150-159
 * Group 7: Ports 160-169
 * Group 8: Ports 170-179
 * Group 9: Ports 180-189
 * Group 10: Ports 190-199
 * 
 */
package org.sunspotworld;

import com.sun.spot.io.j2me.radiogram.Radiogram;
import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.peripheral.Spot;
import com.sun.spot.peripheral.TimeoutException;
import com.sun.spot.peripheral.radio.IRadioPolicyManager;
import com.sun.spot.peripheral.radio.RadioFactory;
import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.IAccelerometer3D;
import com.sun.spot.resources.transducers.ISwitch;
import com.sun.spot.resources.transducers.ISwitchListener;
import com.sun.spot.resources.transducers.ITriColorLED;
import com.sun.spot.resources.transducers.ITriColorLEDArray;
import com.sun.spot.resources.transducers.LEDColor;
import com.sun.spot.resources.transducers.SwitchEvent;
import com.sun.spot.service.BootloaderListenerService;
import com.sun.spot.util.IEEEAddress;
import com.sun.spot.util.Utils;
import java.io.IOException;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * The startApp method of this class is called by the VM to start the
 * application.
 *
 * The manifest specifies this class as MIDlet-1, which means it will be
 * selected for execution.
 */
public class LeaderElection extends MIDlet implements ISwitchListener{
    
    private class MessageType {
        static final int DISCOVERY = 1;
        static final int I_AM_LEADER = 2;
        static final int RESPONSE_TO_LEADER = 3;
        static final int DATA = 4;
    }
    
    private static final int NUMBER_OF_SPOTS_ALLOWED = 10;
    private static final int DISCOVERY_PERIOD = 10000;
    private static final int ACKNOWLEDGE_PERIOD = 3000;
    private static final int PACKET_INTERVAL = 3000;
    private static final int CHANNEL_NUMBER = 11;
    private static final short PAN_ID = IRadioPolicyManager.DEFAULT_PAN_ID;
    private static final String DISCOVERY_PORT = "42";
    private static final String ACKNOWLEDGE_PORT = "41";
    private static final String DATA_PORT = "52";
    private int power = 32;                             // Start with max transmit power
    private long myAddr; // own MAC addr (ID)
    private ITriColorLEDArray leds = (ITriColorLEDArray) Resources.lookup(ITriColorLEDArray.class);
    private ITriColorLED statusLED = leds.getLED(0);
    private IAccelerometer3D accel = (IAccelerometer3D) Resources.lookup(IAccelerometer3D.class);
    
    private ISwitch sw1 = (ISwitch) Resources.lookup(ISwitch.class, "SW1");
    private ISwitch sw2 = (ISwitch)Resources.lookup(ISwitch.class, "SW2");
        
        
    private LEDColor red = new LEDColor(50, 0, 0);
    private LEDColor green = new LEDColor(0, 50, 0);
    private LEDColor blue = new LEDColor(0, 0, 50);
    private LEDColor white = new LEDColor(50, 50, 50);
    private LEDColor orange = new LEDColor(255, 102, 0);
    private LEDColor lime = new LEDColor (0,255,0);
    
    private Vector spots;
    private Vector isThere;
    private boolean doDiscover = false;
    private long leader = 0;
    private boolean doLeaderAcknowledge = false;
    private boolean doNonLeaderAcknowledge = false;
    private boolean doLeaderDataTransfer = false;
    private boolean doDataTransfer = false;
    private boolean respond = false;
    private long master = 0;
    private long slave = 0;
    private boolean needDiscovery;
    private long timeElected;
    private Node myself;
    private boolean doReset = false;
    private boolean infected = false;
    private int tilt = 0;
    private RadiogramConnection acknowledgeRecConn;
    private RadiogramConnection acknowledgeTransConn;
    private RadiogramConnection discoverRecConn;
    private RadiogramConnection discoverTransConn;
    private RadiogramConnection dataRecConn;
    private RadiogramConnection dataTransConn;
    
    private double Xtilt;
    private boolean tiltChangeToggle  = false; // AZIZ: to toggle lock LED7
    private boolean ledsInUse = false;
    
    private int infectionTimeCounter =0;// counter for infection period
    private int clearinfectionTimeCounter = 0; // counter for infection period
    private boolean LeaderInfectToggle = false; //WZ COMMENTs: Switch toggle for infection of leader
   
    protected void startApp() throws MIDletStateChangeException {
        initialize();
        sw1.addISwitchListener(this);
        sw2.addISwitchListener(this);
        timeElected = Long.MAX_VALUE;
        needDiscovery = true;
        while (true) {
            needDiscovery = false;
            System.out.println("we are starting InitialDiscovery");
            doInitialDiscovery();
            
            if (leader <= 0) {
                selectLeader();
            }
            if (myAddr == leader) {
                timeElected = System.currentTimeMillis();
                System.out.println("Startup: I am the leader !!");  
                myself = new Leader();
                myself.start();
            } else {
                myself = new Follower();
                myself.start();
                System.out.println("Startup: I am a follower !!"); 
            }
            while (!needDiscovery) {
                
               // if (myAddr != leader) { //Commented by Aziz v5 ***
               //     if (tilt > 0) {
               //         leds.getLED(7).setColor(red);
               //     } else {
               //         leds.getLED(7).setColor(blue);
               //     }
               //     leds.getLED(7).setOn();
                //}
                
                 if (!tiltChangeToggle) {   // AZIZ: modified v5 for leader
                       
                        //tilt = (accel.getTiltX() > 0) ? 1 : -1;  /// AZIZ: TODO: put the tilttogggle code
                        
                      try { //
                        Xtilt = accel.getTiltX();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                      if (Xtilt > 0){ //WZ COMMENTs: Right
                            System.out.println("right");
                            tilt =1;
                            leds.getLED(7).setColor(red);
                            leds.getLED(7).setOn();
                        }else if(Xtilt < 0){ //WZ COMMENTs: Left
                             System.out.println("left");
                            tilt = -1;
                            leds.getLED(7).setColor(blue);
                            leds.getLED(7).setOn();
                        } 
                        }
                 
                    else  leds.getLED(7).setOn();   // AZIZ: to hang the LED7 in case of sw1 is hold
                 
                    pause(500);
                    
                System.out.println("no need for discovery for 3000 msec");
                pause(3000); // Aziz wait for discovery changed from 1500
            }
        }
    }
    
    private class Leader implements Node {

        public void start() {
            print("Starting as Leader");
            leds.getLED(1).setColor(white);
            leds.getLED(1).setOn();
            spots.removeElementAt(0);
            isThere = new Vector();
            for (int ii = 0; ii < spots.size(); ii++) {
                isThere.addElement(new Integer(2));
            }
            doLeaderAcknowledge = true;
            doNonLeaderAcknowledge = false;
            doDataTransfer = false;
            doLeaderDataTransfer = true;

            startAcknowledgeConnections();
            startDataConnections();
            startThreads();
        }

        public void stop() {
            doLeaderAcknowledge = false;
            doDataTransfer = false;
        }

        public void doAction() {
            //doReset = true;
            //LeaderInfectToggle = !LeaderInfectToggle; //WZ COMMENTs: Toggle infection
        }

        /* leaderAcknowledgeTransmit
            open another transmission connection
            tell others I am a leader, my address and the time elected
        */
        private void leaderAcknowledgeTransmit() {
            try {
                Radiogram xdg = (Radiogram) acknowledgeTransConn.newDatagram(acknowledgeTransConn.getMaximumLength());
                while (doLeaderAcknowledge) {
                    print("Sending acknowledge");
                    xdg.reset();
                    xdg.writeInt(MessageType.I_AM_LEADER);
                    xdg.writeLong(myAddr);
                    xdg.writeLong(timeElected);
                    acknowledgeTransConn.send(xdg);
                    for (int ii = 0; ii < isThere.size(); ii++) {
                        int val = ((Integer) isThere.elementAt(ii)).intValue();
                        if (val <= 0) {
                            spots.removeElementAt(ii);
                            isThere.removeElementAt(ii);
                        } else {
                            isThere.setElementAt(new Integer(val - 1), ii);
                        }
                    }
                    adjustLights();
                    pause(ACKNOWLEDGE_PERIOD);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        /* leaderResponseReciever
            open another receiving connection
            listen to the responds, react to conflict leader election
        */
        private void leaderResponseReciever() {
            try {
                Radiogram xdg = (Radiogram) acknowledgeRecConn.newDatagram(acknowledgeRecConn.getMaximumLength());
                while (doLeaderAcknowledge) {
                    xdg.reset();
                    try {
                        acknowledgeRecConn.receive(xdg);
                        int message = xdg.readInt();
                        if (message == MessageType.RESPONSE_TO_LEADER) {
                            long srcLeader = xdg.readLong();
                            Long addr = new Long(xdg.readLong());
                            if (srcLeader == myAddr) {
                                print("Received Response From: " + IEEEAddress.toDottedHex(addr.longValue()));
                                if (!spots.contains(addr)) {
                                    spots.addElement(addr);
                                    isThere.addElement(new Integer(2));
                                } else {
                                    int index = spots.indexOf(addr);
                                    isThere.setElementAt(new Integer(2), index);
                                }
                            }
                        } else if (message == MessageType.I_AM_LEADER) {
                            long srcLeader = xdg.readLong();
                            long electionTime = xdg.readLong();

                            System.out.println("CONFLICT - Leader Message");
                            if (electionTime < timeElected) {
                                print("Bullied out by: " + IEEEAddress.toDottedHex(srcLeader));
                                leader = srcLeader;
                                myself = new Follower();
                                myself.start();
                            }
                        }
                    } catch (TimeoutException t) {
                    }
                    pause(100);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        /* resetTransmitter
            open another trasmission connection
            
        */       


        private void startThreads() {
            new Thread() {
                public void run() {
                    leaderAcknowledgeTransmit();
                }
            }.start();
            new Thread() {
                public void run() {
                    leaderResponseReciever();
                }
            }.start();
            
            /* //WZ COMMENTs: Disabled becasue doesn't fit with the requirements
            new Thread() {
                public void run() {
                    resetTransmitter();
                }
            }.start();
            */
            
            // AZIZ: thread for respond to switches
            new Thread() {     
                public void run(){
                    respondToSwitches();
                }     
                
            }.start();  
        }
    }
    private void leaderInfection() {
            try {
                Radiogram xdg = (Radiogram) dataTransConn.newDatagram(dataTransConn.getMaximumLength());

                while (doLeaderDataTransfer) {
                    xdg.reset();
                
                    if (!LeaderInfectToggle) {    //// WZ COMMENTs: NO infection
                        //WZ COMMENTs: 
                        clearinfectionTimeCounter++;
                        if (clearinfectionTimeCounter <=6) {
                            System.out.println("Leader: Clear infection set");
                            xdg.writeInt(MessageType.DATA);
                            xdg.writeLong(myAddr);
                            xdg.writeBoolean(false); //WZ COMMENTs: **** Send false to disable infection
                            xdg.writeInt(tilt);
                        //LeaderInfectToggle = false;
                            dataTransConn.send(xdg);
                        }
                    
                    }
                    else {//WZ COMMENTs: LeaderInfectToggle is true so do leader set

                        
                        infectionTimeCounter ++; //WZ COMMENTs: add a counter to create the one-shot activity
                        if (infectionTimeCounter <= 6) { //6*500 which equals 3 secs
                            System.out.println("Leader: Infection set");
                            xdg.writeInt(MessageType.DATA);
                            xdg.writeLong(myAddr);
                            xdg.writeBoolean(true); //WZ COMMENTs: **** Send true to enable infection
                            xdg.writeInt(tilt);
                            //LeaderInfectToggle = false; //WZ COMMENTs: one-shot activity per button press
                            dataTransConn.send(xdg);
                        }
                        
                        
                    }
                    pause(500);
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
    }    

    /* adjustLights
        set up the LEDs according to Leader Follower status
        use the LEDs to indicate numebr of connected spots for leader    
    */        
    private void adjustLights() {
        leds.getLED(1).setOff();
        pause(250);
        leds.getLED(1).setOn();
        if (myAddr == leader) {
            System.out.println("I'm leader and number of followers: " + spots.size());
            
           
            for (int ii = 2; ii <= 5; ii++) {
                leds.getLED(ii).setOff();
            }
            
             if (spots.size()<5){
            for (int ii = 2; ii < spots.size()+2; ii++) {
                leds.getLED(ii).setColor(lime); 
                leds.getLED(ii).setOn();
            }
            }
        }
    }

    private class Follower implements Node {

        public void start() {
            print("Starting as Follower");
            leds.setOff();
            leds.getLED(0).setColor(green);
            leds.getLED(0).setOn();
            leds.getLED(1).setColor(orange);
            leds.getLED(1).setOn();
            spots.removeAllElements();
            doLeaderAcknowledge = false;
            doNonLeaderAcknowledge = true;
            doDataTransfer = true;
            doLeaderDataTransfer = false;
            startAcknowledgeConnections();
            startDataConnections();
            startThreads();
        }

        public void stop() {
            doNonLeaderAcknowledge = false;
            doDataTransfer = false;
        }

        public void doAction() {
            infected = true;
        }

        /** Follower
         * Receiver for a non-leader to receive the acknowledge from the leader.
         */
        private void AcknowledgeReciever() {
            try {
                Radiogram xdg = (Radiogram) acknowledgeRecConn.newDatagram(acknowledgeRecConn.getMaximumLength());
                long time = System.currentTimeMillis() + 2 * ACKNOWLEDGE_PERIOD;
                while (doNonLeaderAcknowledge) {
                    xdg.reset();
                    try {
                        acknowledgeRecConn.receive(xdg);
                        int message = xdg.readInt();
                        if (message == MessageType.I_AM_LEADER) {
                            long addr = xdg.readLong();
                            long electionTime = xdg.readLong();
                            print("Acknowledge from: " + IEEEAddress.toDottedHex(addr));
                            if (electionTime < timeElected) {
                                leader = addr;
                            }
                            if (addr == leader || leader == 0) {
                                time = System.currentTimeMillis() + 2 * ACKNOWLEDGE_PERIOD;
                                respond = true;
                            }
                        }
                    } catch (TimeoutException t) {
                    }
                    if (System.currentTimeMillis() > time) {
                        //leader is gone rerun election   
                        System.out.println("Leader is gone!");
                        leader = 0;
                        doNonLeaderAcknowledge = false;
                        needDiscovery = true;
                        myself.stop();
                        break;
                    }
                    pause(ACKNOWLEDGE_PERIOD / 2);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        /* Follower responseTransmitter
             
        */   
        private void responseTransmitter() {
            try {

                Radiogram xdg = (Radiogram) acknowledgeTransConn.newDatagram(acknowledgeTransConn.getMaximumLength());
                while (doNonLeaderAcknowledge) {
                    print(respond + "");
                    if (respond) {
                        print("Sending response to: " + IEEEAddress.toDottedHex(leader));
                        respond = false;
                        xdg.reset();
                        xdg.writeInt(MessageType.RESPONSE_TO_LEADER);
                        xdg.writeLong(leader);
                        xdg.writeLong(myAddr);
                        acknowledgeTransConn.send(xdg);
                        adjustLights();
                    }
                    pause(2000); // keep alive changed from 1500
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }

        private void startThreads() {
            new Thread() {
                public void run() {
                    AcknowledgeReciever();
                }
            }.start();
            new Thread() {
                public void run() {
                    responseTransmitter();
                }
            }.start();
            new Thread() {
                public void run() {
                    infectTransmitter();
                }
            }.start();
            new Thread() {
                public void run() {
                    dataReceiver();
                }
            }.start();
        }

        /* Follower infectTransmitter
            if infected,
            infect others
            else tilt LED
        */   
        private void infectTransmitter() {
            try {
                Radiogram xdg = (Radiogram) dataTransConn.newDatagram(dataTransConn.getMaximumLength());
                while (doDataTransfer) {
                    xdg.reset();
                    if (infected) {
                        print("I am infected");
                        pause(3000);
                        if (infected) {
                            print("No reset -- then infecting others");
                            xdg.writeInt(MessageType.DATA);
                            xdg.writeLong(myAddr);
                            xdg.writeBoolean(infected);
                            xdg.writeInt(tilt);
                            dataTransConn.send(xdg);
                            leds.getLED(5).setColor(red);//WZ COMMENTs: SET LED 5 to red for infection
                            leds.getLED(5).setOn(); //WZ COMMENTs: SET LED 5 on
                        }
                    //} else  {  // AZIZ: changed to display LED toggeling
                    }
                    
                      else if (!tiltChangeToggle) {   // AZIZ: REVIEW
                        //tilt = (accel.getTiltX() > 0) ? 1 : -1;  /// AZIZ: TODO: put the tilttogggle code
                        Xtilt = accel.getTiltX();
 
                       leds.getLED(5).setOff(); //WZ COMMENTs: SET LED 5 off for non infection 
                      if (Xtilt > 0){ //WZ COMMENTs: Right
                            System.out.println("right");
                            tilt =1;
                            leds.getLED(7).setColor(red);
                            leds.getLED(7).setOn();
                        }else if(Xtilt < 0){ //WZ COMMENTs: Left
                             System.out.println("left");
                            tilt = -1;
                            leds.getLED(7).setColor(blue);
                            leds.getLED(7).setOn();
                        } 
                      }
                    pause(500);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        /* Follower dataReceiver
            receive data, determine whether it is from Leader or Follower
            get infected or uninfected according to the message
        */   
        private void dataReceiver() {
            try {
                Radiogram xdg = (Radiogram) dataRecConn.newDatagram(dataRecConn.getMaximumLength());

                while (doDataTransfer) { 
                    xdg.reset();

                    dataRecConn.receive(xdg);
                    int message = xdg.readInt();
                    System.out.println("recieved message for the follower is " + message);
                    if (message == MessageType.DATA) {
                        long addr = xdg.readLong();
                        boolean action = xdg.readBoolean();
                        System.out.println("action recieved is " + action);
                        int infectedTilt = xdg.readInt();

                        if (addr == leader && !action) {
                            print("The leader has cleared my infection!");
                            infected = false;
                            leds.getLED(5).setOff();
                            tilt = (accel.getTiltX() > 0) ? 1 : -1;
                          
                        } 
                        else if (addr == leader && action) {
                                print("I've been infected by: " + IEEEAddress.toDottedHex(addr));
                                infected = true;
                                tilt = infectedTilt;
                                leds.getLED(5).setColor(red);
                                leds.getLED(5).setOn();
                                pause(3000);
                            
                        }
                        
                        //pause(500);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /* Follower selectLeader
        select leader based on mac address
    */   
    private void selectLeader() {
        long highest = 0;
        for (int ii = 0; ii < spots.size(); ii++) {
            long addr = ((Long) spots.elementAt(ii)).longValue();
            if (addr > highest) {
                highest = addr;
            }
        }
        leader = highest;
        System.out.println("The leader is: " + IEEEAddress.toDottedHex(leader));
    }

    private void initialize() {
        myAddr = RadioFactory.getRadioPolicyManager().getIEEEAddress();
        IRadioPolicyManager rpm = Spot.getInstance().getRadioPolicyManager();
        rpm.setChannelNumber(CHANNEL_NUMBER);
        rpm.setPanId(PAN_ID);
        rpm.setOutputPower(power - 32);
    }

    /**
     * Pause for a specified time.
     *
     * @param time the number of milliseconds to pause
     */
    private void pause(long time) {
        try {
            Thread.currentThread().sleep(time);
        } catch (InterruptedException ex) { /* ignore */ }
    }

    protected void pauseApp() {
        // This is not currently called by the Squawk VM
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

    private void doInitialDiscovery() {
        System.out.println("Discovery Initialized");
        startDiscoveryConnections();
        long stopTime = System.currentTimeMillis() + DISCOVERY_PERIOD;
        spots = new Vector();
        spots.addElement(new Long(myAddr));
        doDiscover = true;
        startDiscovery();
        statusLED.setColor(red);     // Red = not active
        statusLED.setOn();
        while (System.currentTimeMillis() < stopTime) {
            System.out.println("InitialDiscovery: Number of spots: " + spots.size());
            
           
            for (int ii = 2; ii <= 5; ii++) {
                leds.getLED(ii).setOff();
            }
            //for (int ii = 0; ii < spots.size(); ii++) {
            //    leds.getLED(7 - ii).setColor(blue);
            //    leds.getLED(7 - ii).setOn();
            //}
            
            if (spots.size()<5){
            for (int iii = 2; iii <  spots.size()+2; iii++) {
                  leds.getLED(iii).setColor(lime);
                  leds.getLED(iii).setOn();
                  
                }
            }
            leds.getLED(1).setOff();
            pause(250);
            leds.getLED(1).setOn();
            pause(250);
        }
        doDiscover = false;
        leds.getLED(0).setColor(green);
    }

    private void startDiscovery() {
        new Thread() {
            public void run() {
                runDiscoveryTransmitter();
            }
        }.start();
        new Thread() {
            public void run() {
                runDiscoveryReciever();
            }
        }.start();
    }

    private void runDiscoveryReciever() {
        try {
            Radiogram xdg = (Radiogram) discoverRecConn.newDatagram(discoverRecConn.getMaximumLength());
            while (doDiscover) {
                xdg.reset();
                discoverRecConn.receive(xdg);
                int messageType = xdg.readInt();
                if (messageType == MessageType.DISCOVERY) {
                    Long addr = new Long(xdg.readLong());
                    if (!spots.contains(addr)) {
                        spots.addElement(addr);
                    }
                }
                pause(250);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void runDiscoveryTransmitter() {
        try {
            // transConn.setTimeout(PACKET_INTERVAL - 5);

            Radiogram xdg = (Radiogram) discoverTransConn.newDatagram(discoverTransConn.getMaximumLength());

            while (doDiscover) {
                xdg.reset();
                xdg.writeInt(MessageType.DISCOVERY);
                xdg.writeLong(myAddr);
                discoverTransConn.send(xdg);

                pause(250);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    
    
    // AZIZ: added for abstraction

    /*  AZIZ: COMMENTED
    
    
    public void switchPressed(SwitchEvent sw) {
        print("Switch Pressed");
     
        
        //else if (sw2.equals(sw.getSwitch()) && myself != null) {
        //    print("Do action");
        //    myself.doAction();
        //}
        
         pause(100);         // check every 0.1 seconds
            if (sw1.isClosed()) {  //WZ COMMENTs: isClosed return true is button is pressed
                if (tiltChangeToggle) {
                    tiltChangeToggle = false; //WZ COMMENTs: Toggle tiltchange whenever sw1 is pressed
                    leds.getLED(6).setColor(green);
                    leds.getLED(6).setOn(); 
                    pause(50);
                }else{
                    tiltChangeToggle = true; //WZ COMMENTs: tiltchange is true for lock state
                    leds.getLED(6).setColor(blue);
                    leds.getLED(6).setOn(); 
                    pause(50);                
                }
                System.out.println("tiltchange = " + tiltChangeToggle);
                
                pause(1000);    // wait 1.0 second
                if (sw1.isClosed()) {
                   
                }
                pause(1000);    // wait 1.0 second
                displayNumber(0, blue);
            }
            if (sw2.isClosed()) {
                int cnt = 0;
                ledsInUse = true;
                displayNumber(power, red);
                pause(1000);    // wait 1.0 second
                if (sw2.isClosed()) {
                    while (sw2.isClosed()) {
                        power++;
                        if (power > 30) { cnt = 0; }
                        if (power > 32) { power = 0; }
                        displayNumber(power, red);
                        cnt++;
                        pause(cnt < 5 ? 500 : 300);    // wait 0.5 second
                    }
                    Spot.getInstance().getRadioPolicyManager().setOutputPower(power - 32);
                }
                pause(1000);    // wait 1.0 second
                displayNumber(0, blue);
            }
            ledsInUse = false;
            
      
    }
    
     */
    
    
    // AZIZ: added for Switchtogelling.
     private void respondToSwitches() {
        while (true) {
            pause(100);         // check every 0.1 seconds
            //WZ COMMENTs: DEBUG: sw1 working for follower
            if (sw1.isClosed()) {  //WZ COMMENTs: isClosed return true is button is pressed
                System.out.println("Sw1 Pressed");
                
                if (tiltChangeToggle) {
                    tiltChangeToggle = false; //WZ COMMENTs: Toggle tiltchange whenever sw1 is pressed
                    leds.getLED(6).setColor(green);
                    leds.getLED(6).setOn(); 
                    pause(100);
                    
                    //tiltLED = false; ///////////////////////////////// AZIZ: to stop the LED7 update for tilting ***
      
                           try { //
                       Xtilt = accel.getTiltX();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    if (Xtilt > 0){ //WZ COMMENTs: Right
                            System.out.println("leader is right");
                            tilt =1;
                            leds.getLED(7).setColor(red);
                            leds.getLED(7).setOn();
                    }else if(Xtilt < 0){ //WZ COMMENTs: Left
                             System.out.println("leader is left");
                            tilt = -1;
                            leds.getLED(7).setColor(blue);
                            leds.getLED(7).setOn();
                   
                    }  
              
                }else{
                    tiltChangeToggle = true; //WZ COMMENTs: tiltchange is true for lock state
                    leds.getLED(6).setColor(blue);
                    leds.getLED(6).setOn(); 
                    pause(100);                
                }
                
                System.out.println("Entering LOCKED State - tiltchange = " + tiltChangeToggle);
                
                pause(1000);    // wait 1.0 second
                if (sw1.isClosed()) {
                   
                }
                //pause(1000);    // wait 1.0 second
                //displayNumber(0, blue);
            }

                
                
            /*
            if (sw2.isClosed()) {
                int cnt = 0;
                ledsInUse = true;
                displayNumber(power, red);
                pause(1000);    // wait 1.0 second
                if (sw2.isClosed()) {
                    while (sw2.isClosed()) {
                        power++;
                        if (power > 30) { cnt = 0; }
                        if (power > 32) { power = 0; }
                        displayNumber(power, red);
                        cnt++;
                        pause(cnt < 5 ? 500 : 300);    // wait 0.5 second
                    }
                    Spot.getInstance().getRadioPolicyManager().setOutputPower(power - 32);
                }
                pause(1000);    // wait 1.0 second
                displayNumber(0, blue);
            }
            ledsInUse = false;
            */
            
        }
    }

    public void switchPressed(SwitchEvent sw) { //WZ COMMENTs: monitor sw2 for infection or disinfection
        if (sw2.equals(sw.getSwitch()) && myself != null) {
            System.out.println("Switch2 is Pressed");
            if (myAddr == leader){
                    
                    LeaderInfectToggle = !LeaderInfectToggle; // toggle sw2
                    if (LeaderInfectToggle) {
                        clearinfectionTimeCounter = 0;
                        System.out.println("clearinfectionTimeCounter = " + clearinfectionTimeCounter);
                    }
                    else { //LeaderInfectToggle is false, No infection
                        infectionTimeCounter = 0;
                        System.out.println("infectionTimeCounter = " + infectionTimeCounter);
                    }
                    //LeaderInfectToggle = true;
                    leaderInfection(); //Do infection
                    System.out.println("button B pressed - do leader set");
            }
            else{ //I am a follower
                    myself.doAction();
                    System.out.println("button B pressed - do follower infection set");
            }        
        }
        
    }

    public void switchReleased(SwitchEvent sw) {
        // do nothing
    }

    private void print(String message) {
        System.out.println(message);
    }

    /* startAcknowledgeConnections
        start listening, if no message within 2 periods, start broadcasting
    */
    private void startAcknowledgeConnections() {
        if (acknowledgeRecConn == null) {
            try {
                acknowledgeRecConn = (RadiogramConnection) Connector.open("radiogram://:" + ACKNOWLEDGE_PORT);
                acknowledgeRecConn.setTimeout(ACKNOWLEDGE_PERIOD * 2);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (acknowledgeTransConn == null) {
            try {
                acknowledgeTransConn = (RadiogramConnection) Connector.open("radiogram://broadcast:" + ACKNOWLEDGE_PORT);
                acknowledgeTransConn.setMaxBroadcastHops(1);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /* startDiscoveryConnections
        start listening, if no message, start broadcasting
    */
    private void startDiscoveryConnections() {
        if (discoverRecConn == null) {
            try {
                discoverRecConn = (RadiogramConnection) Connector.open("radiogram://:" + DISCOVERY_PORT);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (discoverTransConn == null) {
            try {
                discoverTransConn = (RadiogramConnection) Connector.open("radiogram://broadcast:" + DISCOVERY_PORT);
                discoverTransConn.setMaxBroadcastHops(1);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /* startDataConnections
        start listening, if no message, start broadcasting
    */
    private void startDataConnections() {
        if (dataRecConn == null) {
            try {
                dataRecConn = (RadiogramConnection) Connector.open("radiogram://:" + DATA_PORT);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (dataTransConn == null) {
            try {
                dataTransConn = (RadiogramConnection) Connector.open("radiogram://broadcast:" + DATA_PORT);
                dataTransConn.setMaxBroadcastHops(1);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
       private void displayNumber(int val, LEDColor col) {
        for (int i = 0, mask = 1; i < 7; i++, mask <<= 1) {
            leds.getLED(7-i).setColor(col);
            leds.getLED(7-i).setOn((val & mask) != 0);
        }
    }

       
}
