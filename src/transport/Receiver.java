package transport;

/**
 *<h1>Receiver</h1>
 * 135124
 * This is the receiver class it checks the packet which has been received and then send an 
 * acknowledgement, of either the old packet or the new packet
 * 
 * 
 */
public class Receiver extends NetworkHost {

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

    // Add any necessary class variables here. They can hold state information for the receiver.
    // Also add any necessary methods (e.g. checksum of a String)

    /**
     *This is the packet that has just come in 
     */
    private Packet pack;

    /**
     *This is the old packet that has already been received 
     */
    private Packet oldPack;

    /**
     *This is the next expectedSeqno 
     */
    private int expectedSeqNo;

    /**
     *This is the next expected Ack Number 
     */
    private int expectedAckNo;

    /**
     *This is the pack Acknowledgement number which has come through 
     */
    private int packAckNo;

    /**
     * This is the pack Sequence number which has come through 
     */
    private int packSeq;

    /**
     *This is the checkSum of the new packet from the messages, PackAckNo and PackSeq
     */
    private int checkSum;

    /**
     *This is the actual check sum from the packet 
     */
    private int oldCheckSum;

    // This is the constructor.  Don't touch!

    /**
     *
     * @param entityName
     */
    public Receiver(int entityName) {
        super(entityName);
    }

    // This method will be called once, before any of your other receiver-side methods are called. 
    // It can be used to do any required initialisation (e.g. of member variables you add to control the state of the receiver).

    /**
     *<h1>init</h1> 
     * This initialises variables when the instance is made 
     */
    @Override
    public void init() {
        expectedSeqNo = 0;
        packAckNo = 0;
        packSeq = 0;
        checkSum = 0;
        oldCheckSum = 0;
    }

   

    /**
     *<h1>input</h1> 
     *  This method will be called whenever a packet sent from the sender(i.e. as a result of a udtSend() being called by the Sender ) arrives at the receiver. 
     *   The argument "packet" is the (possibly corrupted) packet sent from the sender.
     * 
     * @param packet this is the packet from the sender 
     */
    @Override
    public void input(Packet packet) {
        //Sets the packet to pack
        pack = packet;
        packAckNo = pack.getAcknum();
        packSeq = pack.getSeqnum();
        oldCheckSum = pack.getChecksum();
        //Generate the packet check sum 
        checkSum = createCheckSum(pack.getPayload(), packSeq, packAckNo);
        //if its not expected then send the old packet 
        if (packSeq != expectedAckNo || oldCheckSum != checkSum) {
            udtSend(oldPack);
        } else {
            //deliver the data and set the old packet and sent it increase the expected numbers 
            deliverData(pack.getPayload());
            oldPack = new Packet(packSeq, packAckNo, createCheckSum("", packAckNo, packSeq));
            udtSend(oldPack);
            expectedSeqNo++;
            expectedAckNo++;
        }
    }
    
    /**
     *<h1>createCheckSum</h1> 
     * This creates a checksum using the message sequence number and Ack number
     * 
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

    // This method will be called in the output method and input method
    // to help check the checksum of sending of packets and receiving of packets 

    /**
     * This check if the packet is corrupt or not 
     * @return boolean
     */
    public boolean checkAck() {
        if (packAckNo < expectedAckNo && packSeq < expectedSeqNo && oldCheckSum == checkSum) {
            expectedAckNo = packAckNo;
            expectedSeqNo = packSeq;
            return true;
        }
        if (oldCheckSum != checkSum) {
            return false;
        } else if (packAckNo != expectedAckNo) {
            return false;
        } else if (packSeq != expectedSeqNo) {
            return false;
        } else {
            checkSum = 0;
            return true;
        }

    }

}
