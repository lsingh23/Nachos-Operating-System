
package nachos.threads;

import nachos.machine.*;
import java.lang.Math;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     * test
     */
    public Communicator() {

        //initialize instace variables
        lock = new Lock();
        statListener = new Condition2(lock);
        statSpeaker = new Condition2(lock);
        listener = 0;
        messageWaiting = false;

    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
        //Acquire the lock
        lock.acquire();
        
        // Sleep while there is no active listner or previous message has not been picked up
        while(listener == 0 || messageWaiting == true)
            statSpeaker.sleep();

        // pass the message
        message = word;

        //Indicates that a message is waiting to be listened
        messageWaiting = true;

        //Wake up a listener
        statListener.wake();

        //Release the lock
        lock.release();
    }


    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
        //Acquire the lock
        lock.acquire();

        // Indicates there is an active listener
        listener++;

        // If there is no current message then sleep and wake up a speaker
        while(messageWaiting == false){
            statSpeaker.wake();
            statListener.sleep();
        }
        statSpeaker.wake();

        // Pass the message
        int passedMessage = message;

        //Message is heard
        messageWaiting = false;

        //Listener has heard so it has done its work
        listener--;

        //Release the lock
        lock.release();

	return passedMessage;
    }


/****************************TESTING********************************************* */   

//Class used for Communicator
    private static class Speaker implements Runnable{
        Speaker(Communicator link, double id){
            this.link = link;
            this.id = id;
        }

        //Speak a random number
        public void run(){
            int num = (int)(Math.random() * 60 + 10);
            link.speak(num);
            System.out.println("SpeakerThread id #" + id + " spoke value " + num );
          
        }

        private Communicator link;
        private double id;
    }

    private static class Listener implements Runnable{
        Listener(Communicator link, double id){
            this.link = link;
            this.id = id;
        }

        //Listen a message
        public void run(){
            int num = link.listen();
            System.out.println("ListenerThread id #" + id + " heard " + num );
          
        }

        private Communicator link;
        private double id;
    }

     // selfTest Method will be called when the debug flag is C,
    //that is, nachos -d C
    public static void selfTest(){
        Communicator communicator = new Communicator();
        ThreadedKernel.alarm.waitUntil(2000);

        System.out.println("--------------------------------------Communicator Testing----------------------------------------------");

        System.out.println("-------------- Test 1 in Communicator  Testing : 2 Speakers and 2 Listeners----------------------------- ");
        KThread s1_test1 = new KThread(new Speaker(communicator, 1.0));
        KThread s2_test1 = new KThread(new Speaker(communicator, 1.1));
        KThread l1_test1 = new KThread(new Listener(communicator, 1.0));
        KThread l2_test1 = new KThread(new Listener(communicator, 1.1));
        s1_test1.fork();
        s2_test1.fork();
        l1_test1.fork();
        l2_test1.fork();

        ThreadedKernel.alarm.waitUntil(1000);

        System.out.println("\n\n----------- Test 2 in Communicator  Testing : excessive speaker case ( 2 speakers , 1 listener )-----");
        KThread s1_test2 = new KThread(new Speaker(communicator, 2.0));
        KThread s2_test2 = new KThread(new Speaker(communicator, 2.1));
        KThread l1_test2 = new KThread(new Listener(communicator, 2.0));
        s1_test2.fork();
        s2_test2.fork();
        l1_test2.fork();

        ThreadedKernel.alarm.waitUntil(1000);
        
        System.out.println("\n\n---------Test 3 in Communicator  Testing : excessive speaker case ( 2 speakers , 1 listener )---------");
        KThread s1_test3 = new KThread(new Speaker(communicator, 3.0));
        KThread l1_test3 = new KThread(new Listener(communicator, 3.0));
        KThread l2_test3 = new KThread(new Listener(communicator, 3.1));
        l1_test3.fork();
        l2_test3.fork();
        s1_test3.fork();

        ThreadedKernel.alarm.waitUntil(1000);
      
    }


    private int message;
    private int listener;
    private Lock lock;
    private Condition2 statListener, statSpeaker;
    private boolean messageWaiting;

}

// ------------------------------------------------






// --------------------------------------------






// package nachos.threads;

// import nachos.machine.*;

// /**
//  * A <i>communicator</i> allows threads to synchronously exchange 32-bit
//  * messages. Multiple threads can be waiting to <i>speak</i>,
//  * and multiple threads can be waiting to <i>listen</i>. But there should never
//  * be a time when both a speaker and a listener are waiting, because the two
//  * threads can be paired off at this point.
//  */
// public class Communicator {
//     /**
//      * Allocate a new communicator.
//      */
//     public Communicator() {
//         isMessage = false;
//         speakerCount = 0;
//         listenerCount = 0;
//         lock = new Lock();
//         speaker = new Condition2(lock);
//         listener = new Condition2(lock);
//     }

//     /**
//      * Wait for a thread to listen through this communicator, and then transfer
//      * <i>word</i> to the listener.
//      *
//      * <p>
//      * Does not return until this thread is paired up with a listening thread.
//      * Exactly one listener should receive <i>word</i>.
//      *
//      * @param word the integer to transfer.
//      */
//     public void speak(int word) {
//         if (!lock.isHeldByCurrentThread()) {
//             lock.acquire();
//         }
//         speakerCount++;
//         while (isMessage || speakerCount > 1) {
//             speaker.sleep();
//         }
//         speakerCount--;
//         message = word;
//         isMessage = true;
//         listener.wake();
//         lock.release();
//     }

//     /**
//      * Wait for a thread to speak through this communicator, and then return
//      * the <i>word</i> that thread passed to <tt>speak()</tt>.
//      *
//      * @return the integer transferred.
//      */
//     public int listen() {
//         if (!lock.isHeldByCurrentThread()) {
//             lock.acquire();
//         }
//         listenerCount++;
//         while (!isMessage) {
//             if (speakerCount > 0) {
//                 speaker.wake();
//             }
//             listener.sleep();
//         }
//         listenerCount--;
//         int finalMessage = message;
//         message = 0;
//         isMessage = false;
//         lock.release();
//         return finalMessage;
//     }

//     /**
//      * Used for testing purposes
//      */
//     private static class Speaker implements Runnable {

//         Speaker(Communicator communicator, double communicator_id) {
//             this.communicator = communicator;
//             this.communicator_id = communicator_id;
//         }

//         public void run() {
//             int val = (int) (Math.random() * 90 + 10);
//             communicator.speak(val);
//             System.out.println("SpeakerThread : " + communicator_id + " , Corresponding spoken value : " + val);

//         }

//         private Communicator communicator;
//         private double communicator_id;
//     }

//     /**
//      * Used for testing purposes
//      */
//     private static class Listener implements Runnable {

//         Listener(Communicator communicator, double communicator_id) {
//             this.communicator = communicator;
//             this.communicator_id = communicator_id;
//         }

//         public void run() {
//             int num = communicator.listen();
//             System.out.println("ListenerThread : " + communicator_id + " , Corresponding heard value : " + num);
//         }

//         private Communicator communicator;
//         private double communicator_id;
//     }


//     /**
//      * Testing for Communicator functionality
//      */
//     public static void selfTest() {

//         Communicator communicator = new Communicator();
//         ThreadedKernel.alarm.waitUntil(2000);

//         System.out.println("--------------------------------------Communicator Testing----------------------------------------------");

//         System.out.println("-------------- Test 1 in Communicator  Testing : 2 Speakers and 2 Listeners----------------------------- ");
//         KThread s1_test1 = new KThread(new Speaker(communicator, 1.0));
//         KThread s2_test1 = new KThread(new Speaker(communicator, 1.1));
//         KThread l1_test1 = new KThread(new Listener(communicator, 1.0));
//         KThread l2_test1 = new KThread(new Listener(communicator, 1.1));
//         s1_test1.fork();
//         s2_test1.fork();
//         l1_test1.fork();
//         l2_test1.fork();

//         ThreadedKernel.alarm.waitUntil(1000);

//         System.out.println("\n\n----------- Test 2 in Communicator  Testing : excessive speaker case ( 2 speakers , 1 listener )-----");
//         KThread s1_test2 = new KThread(new Speaker(communicator, 2.0));
//         KThread s2_test2 = new KThread(new Speaker(communicator, 2.1));
//         KThread l1_test2 = new KThread(new Listener(communicator, 2.0));
//         s1_test2.fork();
//         s2_test2.fork();
//         l1_test2.fork();

//         ThreadedKernel.alarm.waitUntil(1000);
        
//         System.out.println("\n\n---------Test 3 in Communicator  Testing : excessive speaker case ( 2 speakers , 1 listener )---------");
//         KThread s1_test3 = new KThread(new Speaker(communicator, 3.0));
//         KThread l1_test3 = new KThread(new Listener(communicator, 3.0));
//         KThread l2_test3 = new KThread(new Listener(communicator, 3.1));
//         l1_test3.fork();
//         l2_test3.fork();
//         s1_test3.fork();

//         ThreadedKernel.alarm.waitUntil(1000);

//     }

//     private Lock lock;
//     private int speakerCount;
//     private int listenerCount;
//     private int message;
//     private boolean isMessage;
//     private Condition2 speaker;
//     private Condition2 listener;
// }
