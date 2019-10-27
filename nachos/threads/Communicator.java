package nachos.threads;

import nachos.machine.*;

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
     */
	
	
	private Lock lk;
	private Condition speaker;
	private Condition listener;
	private int word;
	
    public Communicator() {
    	// CREATING NEW LOCK 'lk' TO MAKE SPEAK AND LISTEN 
    	this.lk = new Lock();
    	// different from pseudo-code: Condition2 vs Condition
    	
    	/* this.speaker = new Condition2(lk); */
    	this.speaker = new Condition(lk);
    	/* this.listener = new Condition2(lk); */
    	this.listener = new Condition(lk);
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
    	
    	// acquire lock
    	lk.acquire();
    	
    	// while another thread is speaking
    	while (listen() == 0)
    	{
    		// listener sleeps
    		/* speaker.sleep */
    		listener.sleep();
    	}
    	
    	// saving the word to current instance of communicator
    	this.word = word;
    	listener.wake(); 
    	speaker.sleep();
    	
    	// release lock
    	lk.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    
    public int listen() {
    	int returnWord = 0; 
    	lk.acquire();
    	while(listen() == 0)
    	{
    		listener.sleep();
    	} 
    	returnWord = word;
//    	word = null;   CANNOT SET A INT TO NULL UNLESS USING OPTIONAL VARIABLE LIBRARY
// 		SEE https://www.tutorialspoint.com/java8/java8_optional_class.htm
    	speaker.wakeAll();
    	lk.release();
	return returnWord;
    }
}
