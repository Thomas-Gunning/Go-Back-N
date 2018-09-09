package transport;

import java.nio.BufferOverflowException;


/**<h1>Sender</h1>
 * 135124
 * This is the Sender class, this generates a packet from the message using 
 * an sequence number, acknowledgement number and the char int of the message 
 * It then sends the packet to the receiver, it then receives an acknowledgement
 * from the receiver.
 * 
 * If the acknowledgement is correct it will send the next packet, however, if it
 * is incorrect it will cause a timer interrupt which will in turn then send the
 * old packet and all the packets in the window.
 * 
 * If the buffer is full it will send a BufferOverFlowException 
 */

public class Sender extends NetworkHost {

    /*
     * Predefined Constant (static member variables):
     *
     *   int MAXDATASIZE : the maximum size of the Message data and Packet payload
     *
     *
     * Predefined Member Methods:
     *
     *  void startTimer(double increment):
     *       Starts a timer, which will expire in "increment" time units, causing the interrupt handler to be called.  You should only call this in the Sender class.
     *  void stopTimer():
     *       Stops the timer. You should only call this in the Sender class.
     *  void udtSend(Packet p)
     *       Sends the packet "p" into the network to arrive at other host
     *  void deliverData(String dataSent)
     *       Passes "dataSent" up to app layer. You should only call this in the Receiver class.
     *
     *  Predefined Classes:
     *
     *  NetworkSimulator: Implements the core functionality of the simulator
     *
     *  double getTime()
     *       Returns the current time in the simulator. Might be useful for debugging. Call it as follows: NetworkSimulator.getInstance().getTime()
     *  void printEventList()
     *       Prints the current event list to stdout.  Might be useful for debugging. Call it as follows: NetworkSimulator.getInstance().printEventList()
     *
     *  Message: Used to encapsulate a message coming from the application layer
     *    Constructor:
     *      Message(String inputData): 
     *          creates a new Message containing "inputData"
     *    Methods:
     *      void setData(String inputData):
     *          sets an existing Message's data to "inputData"
     *      String getData():
     *          returns the data contained in the message
     *
     *  Packet: Used to encapsulate a packet
     *    Constructors:
     *      Packet (Packet p):
     *          creates a new Packet, which is a copy of "p"
     *      Packet (int seq, int ack, int check, String newPayload):
     *          creates a new Packet with a sequence field of "seq", an ack field of "ack", a checksum field of "check", and a payload of "newPayload"
     *      Packet (int seq, int ack, int check)
     *          chreate a new Packet with a sequence field of "seq", an ack field of "ack", a checksum field of "check", and an empty payload
     *    Methods:
     *      void setSeqnum(int seqnum)
     *          sets the Packet's sequence field to seqnum
     *      void setAcknum(int acknum)
     *          sets the Packet's ack field to acknum
     *      void setChecksum(int checksum)
     *          sets the Packet's checksum to checksum
     *      void setPayload(String payload) 
     *          sets the Packet's payload to payload
     *      int getSeqnum()
     *          returns the contents of the Packet's sequence field
     *      int getAcknum()
     *          returns the contents of the Packet's ack field
     *      int getChecksum()
     *          returns the checksum of the Packet
     *      String getPayload()
     *          returns the Packet's payload
     *
     */
    // Add any necessary class variables here. They can hold state information for the sender. 
    // Also add any necessary methods (e.g. checksum of a String)
    
    // This is the constructor.  Don't touch!
    /**
     *
     * @param entityName
     */
    public Sender(int entityName) {
        super(entityName);
    }

    /**
     * This is the sequence number which is set is initialised to the nextSeqNum 
     * in the output method
     */
    private int seq;

    /**
     *This is the Ack number which is initialised to the nextAckNum in the output
     * method 
     */
    private int ackNo;

    /**
     *This is the delay between the sender and the receiver which is used in RTT
     * and the increment 
     */
    private double delay;

    /**
     *This is the checkSum which makes a checksum for the messages being made into packets
     */
    private int checkSum;

    /**
     *This is a packet which is initialised when the timer interrupt is called 
     */
    private Packet mes;
    
    /**
     * This is a packet which is initialised when the output method is called 
     */
    private Packet pack = null;

    /**
     *The is the Round trip time of the packet, this is initialised to double of 
     * the delay
     */
    private double RTT;

    /**
     *This is the incr of the timer, this is initialised to double of the RTT
     * It is used for the parameter of startTimer()
     */
    private double incr;

    /**
     *This is the buffer
     */
    private Packet[] window;

    /**
     *This is the sendBase so the start of the window 
     */
    private int sendBase;

    /**
     *This is the bufferSize which is intialised to 50 
     */
    private int bufferSize;

    /**
     *This is the nextSeqNum which is initialised to 0 and then incremented 
     */
    private int nextSeqNum;

    /**
     *This is the index pointer in the array for window 
     */
    private int addPacket;

    /**
     *This is the windowSize which is initialised to 8
     */
    private int windowSize;

    /**
     *This is the nextAckNo which is initialised to 0 and is incremented 
     */
    private int nextAckNo;

    // This method will be called once, before any of your other sender-side methods are called. 
    // It can be used to do any required initialisation (e.g. of member variables you add to control the state of the sender).

    /**
     *<h1>init</h1> 
     * This initialises all of the variables when an instance is made
     */
    @Override

    public void init() {
        //This inisialises the sequence number to 0
        seq = 0;
        //This initialises the Ack No to 0
        ackNo = 0;
        //This is the delay between the packets
        delay = 10;
        //This is the check which is initialised to 0
        checkSum = 0;
        //This is the Round trip time which is two times the delay
        RTT = delay * 2;
        //The increment value is 2 times the RTT 
        incr = RTT * 2;
        //The send base is the start of the window
        sendBase = 0;
        //The buffersize is set to 50, if the buffer fills up the program will 
        //wrap the array around 
        bufferSize = 50;
        //The nextSeqNum is initialised to 0
        nextSeqNum = 0;
        //The nextAckNo is initialised to 0
        nextAckNo = 0;
        //this is the index for the array of the window 
        addPacket = 0;
        //This is the end of the window which is set to 8
        windowSize = 8;
        //This is the whole buffer which is initialise to size of the buffer 
        window = new Packet[bufferSize];
    }

    /**
     *<h1>output</h1> 
     * 
     *  This method will be called whenever the app layer at the sender has a message to send.  
     *  The job of your protocol is to ensure that the data in such a message is delivered in-order, and correctly, to the receiving application layer.
     *
     * @param message this is the message that get sent from the application layer 
     */
    @Override
    public void output(Message message) {
        //If the addPacket has a remainder of 0 then set addpacket to 0 
        if (addPacket % bufferSize == 0) {
            addPacket = 0;
        }
        //If the sendBase has a remainder of 0 then set the sendBase to 0 
        if (sendBase % bufferSize == 0) {
            sendBase = 0;
        }
        seq = nextSeqNum;
        ackNo = nextAckNo;
        //This is to identify if the addPacket is less than the window, if it is then create
        //and send the packet
        if (addPacket < (sendBase + windowSize)) {
            checkSum = createCheckSum(message);
            pack = new Packet(nextSeqNum, nextAckNo, checkSum, message.getData());
            //If the window is null then add the packet else throw exception
            if (window[addPacket] == null) {
                window[addPacket] = pack;
            } else {
                throw new BufferOverflowException();
            }
            //Send the packet 
            udtSend(pack);
            //start the timer 
            if (sendBase == addPacket) {
                startTimer(incr);
            }
            //increment values 
            nextSeqNum++;
            nextAckNo++;
            addPacket++;
        } else {
            //If the window size is less than where the addPacket index is then check if the window isnt 
            //else throw exception 
            if (window[addPacket] != null) {
                throw new BufferOverflowException();
            } else {
                int checkSum = createCheckSum(message.getData(), nextSeqNum, nextAckNo);
                window[addPacket] = new Packet(nextSeqNum, nextAckNo, checkSum, message.getData());
                addPacket++;
                nextSeqNum++;
                nextAckNo++;

            }
        }
    }

     

    /**
     *<h1>input</h1> 
     * 
     * This method will be called whenever a packet sent from the receiver (ie as a result of a udtSend() being done by a receiver procedure) arrives at the sender
     * "packet" is the (possibly corrupted) packet sent from the receiver
     * @param packet this is the packet that has been sent from the receiver 
     */
    @Override
    public void input(Packet packet) {
        checkAck(packet);
    }



    /**
     * <h1> timerInteerrup</h1>
     * 
     * This method will be called when the senders's timer expires (thus generating a timer interrupt)
     * You'll probably want to use this method to control the retransmission of packets. 
     * See startTimer() and stopTimer(), above, for how the timer is started and stopped. 
     * 
     */
    @Override
    public void timerInterrupt() {
        //Starts the timer instantly 
        startTimer(incr);
        //if the index is less than the sendBase then we know we have wrapped around
        
        if (addPacket < sendBase) {
            //send the packets before the send base 
            for (int i = sendBase; i < bufferSize; i++) {
                mes = window[i];
                udtSend(mes);
            }
            //send the packets after the buffersize to the index 
            for (int i = 0; i < addPacket; i++) {
                mes = window[i];
                udtSend(mes);
            }
        } else {
            //if it fine then send all them from the send base to the packet 
            for (int i = sendBase; i < addPacket; i++) {
                mes = window[i];
                udtSend(mes);
            }
        }

    }

     

    /**
     *
     * <h1>checkAck</h1> 
     * This method will be called in the output method and input method
     * to help check the checksum receiving of packets from the receiver 
     * 
     * @param packet this is the packet sent from the receiver 
     */
    public void checkAck(Packet packet) {
        //this checks if the checksumm is correct compared to what has come in 
        if (createCheckSum(packet.getPayload(), packet.getAcknum(), packet.getSeqnum()) == packet.getChecksum()) {
            //this checks if the the AckNum remainder is greater than the send base if it is, we accept the packet
            if (packet.getAcknum() % bufferSize >= sendBase) {
                window[sendBase] = null;
                sendBase = (packet.getAcknum() + 1) % bufferSize;
                if (sendBase == nextSeqNum % bufferSize) {
                    stopTimer();
                } else {
                    startTimer(incr);
                }
                checkSum = 0;
            }
        }
    }


    /**
     * <h1>createCheckSum</h1>
     * This method helps create a checksum for each message 
     * @param message which is the only parameter 
     * @return int this returns the checksum from the message  
     */
    public int createCheckSum(Message message) {
        int sum = 0;
        //checkSum = 65535;
        for (int i = 0; i < message.getData().length(); i++) {
            sum += message.getData().charAt(i);
        }
        sum += seq + ackNo;
        //checkSum = checkSum -
        return sum;

    }

    

    /**
     *<h1>createCheckSum</h1> 
     * This method helps create a checksum for each message 
     * @param message first parameter used to get the char values of the message
     * @param seq second parameter used to get int value of the sequence number 
     * @param ackNo third parameter used to get the int value of the ackNo
     * @return int this returns the checkSum from the string message, int seq and int ackNO
     */
    public int createCheckSum(String message, int seq, int ackNo) {
        int sum = 0;
        for (int i = 0; i < message.length(); i++) {
            sum += message.charAt(i);
        }
        sum += seq + ackNo;
        return sum;

    }
}
