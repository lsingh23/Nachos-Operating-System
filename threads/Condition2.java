package nachos.threads;

import nachos.machine.*;
import java.util.LinkedList;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
	this.conditionLock = conditionLock;

    waitingQueue = new LinkedList<KThread>();
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {

	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
    boolean checkStatus = Machine.interrupt().disable();
    conditionLock.release();
    waitingQueue.add(KThread.currentThread());

    KThread.sleep();

	conditionLock.acquire();
    Machine.interrupt().restore(checkStatus);

    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
    boolean checkStatus = Machine.interrupt().disable();

    if(!waitingQueue.isEmpty()){
        
        KThread thread = waitingQueue.removeFirst();

        if(thread != null) {
            thread.ready();
        } 
    }

    Machine.interrupt().restore(checkStatus);
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
    boolean checkStatus = Machine.interrupt().disable();
    while(!waitingQueue.isEmpty()) {
        wake();
    }
    Machine.interrupt().restore(checkStatus);
    }

    /**
     * Testing for Condition2 functionality
     */
    public static void selfTest() {

        final Lock lock = new Lock();
        final Condition2 emptyCondition2 = new Condition2(lock);
        final LinkedList<Integer> queueWait = new LinkedList<>();
    
        KThread consumer = new KThread( new Runnable() {
            public void run() {
                lock.acquire();
                while (queueWait.isEmpty()) {
                    emptyCondition2.sleep();
                }
                Lib.assertTrue(queueWait.size() == 6, "6 values in the list");
                while (!queueWait.isEmpty()) {
                    System.out.println("Removed " + queueWait.removeFirst());
                }
                lock.release();
            }
        });
    
        KThread producer = new KThread( new Runnable() 
        {
            public void run() {
                lock.acquire();
                for (int i = 0; i < 6; i++) {
                    queueWait.add(i);
                    System.out.println("Added " + i);
                }
                emptyCondition2.wake();
                lock.release();
            }        
        });
    
        consumer.setName("Consumer");
        producer.setName("Producer");
        consumer.fork();
        producer.fork();
        consumer.join();
        producer.join();
    }

    private LinkedList<KThread> waitingQueue;
    private Lock conditionLock;
}
