package nachos.threads;

import nachos.machine.*;
import java.util.PriorityQueue;
import javax.crypto.Mac;
import java.util.Comparator;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
	// KThread.currentThread().yield();
    long curTime = Machine.timer().getTime();
    boolean checkStatus = Machine.interrupt().disable();

    while(!queueWait.isEmpty() && queueWait.peek().wakeTime <= curTime) {
        ThreadTime curThreadTime = queueWait.poll();
        KThread curThread = curThreadTime.thread;

        if(curThread != null) {
            curThread.ready();
        }
    }
    KThread.currentThread().yield();
    Machine.interrupt().restore(checkStatus);
    }


    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {	
        if (x <= 0) return;
        long wakeTime = Machine.timer().getTime() + x;
        boolean checkStatus = Machine.interrupt().disable();
        KThread thread = KThread.currentThread();
        ThreadTime curThreadTime = new ThreadTime(thread, wakeTime);
        queueWait.add(curThreadTime);
        thread.sleep();
        Machine.interrupt().restore(checkStatus);
    }

    private class ThreadTime implements Comparable<ThreadTime> {  
        private KThread thread;
        private long wakeTime;

        public ThreadTime(KThread thread, long wakeTime){
            this.thread = thread;
            this.wakeTime = wakeTime;
        }

        public int compareTo(ThreadTime compTime){
            if(this.wakeTime > compTime.wakeTime){
                return 1;
            } else if(this.wakeTime < compTime.wakeTime) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    public static void selfTest(){
		Alarm alarm = new Alarm();
        System.out.println("\n------------------Alarm Testing------------------------\n");
    	long wakeTime = Machine.timer().getTime();
    	alarm.waitUntil(-4);
    	alarm.waitUntil(0);
    	if(wakeTime == Machine.timer().getTime()) {
    		System.out.println("waitUntil() is able to handle both negative and 0 values.");
    	} else {
    		System.out.println("waitUntil() is not able to handle negative and 0 values.");
    	}
    	wakeTime = Machine.timer().getTime() + 10000;
    	alarm.waitUntil(10000);
    	if(wakeTime <= Machine.timer().getTime()) {
    		System.out.println("waitUntil() successfully waits for a minimum amount of time.");
    	} else {
    		System.out.println("waitUntil() is not able to wait for a minimum amount of time.");
    	}
    }

    private PriorityQueue<ThreadTime> queueWait = new PriorityQueue<ThreadTime>();
}