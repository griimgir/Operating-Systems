package nachos.threads;

import nachos.machine.*;

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
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	//this declaration is called in every function in condition, this what makes these functions atomic
	boolean status = Machine.interrupt().disable();
	//add this thread to wait queue so it can be wake up again after being put to sleep
	waitQueue.waitForAccess(KThread.currentThread());
	conditionLock.release();
	KThread.sleep();
	conditionLock.acquire();
	
	Machine.interrupt().restore(status);
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	boolean status = Machine.interrupt().disable();
	//KThread wakeUp = waitQueue.nextThread();
	KThread wakeUp;
	if((wakeUp = waitQueue.nextThread()) != null) {
		wakeUp.ready();
	}
	
	Machine.interrupt().restore(status);
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	boolean status = Machine.interrupt().disable();
	//KThread wakeUp = waitQueue.nextThread();
	KThread wakeUp = waitQueue.nextThread();
	while((wakeUp = waitQueue.nextThread()) != null) {
		wakeUp.ready();
	}
	
	Machine.interrupt().restore(status);
    }
    //wait queue for currentThreads in functions sleep() wake() and wakeAll()
    private ThreadQueue waitQueue = ThreadedKernel.scheduler.newThreadQueue(false);

    private Lock conditionLock;
}
