package nachos.threads;

import nachos.machine.*;
import java.util.ArrayList; // import the ArrayList class
import java.util.LinkedList; // import the LinkedList class

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 * 
 * <p>
 * You must implement this.
 * 
 * @see nachos.threads.Condition
 */
public class Condition2 {

	private LinkedList<KThread> waitQueue; // waitQueue ArrayList not threadqueue
	// private ThreadQueue waitQueue = ThreadedKernel.scheduler.newThreadQueue(false);
	/**
	 * Allocate a new condition variable.
	 * 
	 * @param conditionLock the lock associated with this condition variable.
	 * The current thread must hold this lock whenever it uses <tt>sleep()</tt>,
	 * <tt>wake()</tt>, or <tt>wakeAll()</tt>.
	 */
	public Condition2(Lock conditionLock) {
		this.conditionLock = conditionLock;
		waitQueue = new LinkedList<KThread>();
		//ThreadQueue joinQueue = ThreadedKernel.scheduler.newThreadQueue(true);
	}

	/**
	 * Atomically release the associated lock and go to sleep on this condition
	 * variable until another thread wakes it using <tt>wake()</tt>. The current
	 * thread must hold the associated lock. The thread will automatically
	 * reacquire the lock before <tt>sleep()</tt> returns.
	 */
	public void sleep() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		boolean intStatus = Machine.interrupt().disable(); // Disable interrupts

		conditionLock.release();
		KThread currentThread = KThread.currentThread();
		
		// Add current thread to wait queue
		waitQueue.add(currentThread);
		
		currentThread.sleep(); // make current thread sleep now that its on the waitQueue, untill it is woken again

		Machine.interrupt().restore(intStatus); //Restore interrupts
		conditionLock.acquire();
	}

	/**
	 * Wake up at most one thread sleeping on this condition variable. The
	 * current thread must hold the associated lock.
	 */
	public void wake() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());


		// Wake up AT MOST one thread (First thread in queue)
		if (!waitQueue.isEmpty()) {
			boolean intStatus = Machine.interrupt().disable(); // Disable interrupts
			KThread threadToWake = waitQueue.peek(); // Get first thread in wait queue
			// and remove it from the queue
			if (threadToWake != null){
				boolean cancelStatus = ThreadedKernel.alarm.cancel(threadToWake);
				waitQueue.poll();
				if(!cancelStatus)
					threadToWake.ready();
			}
			Machine.interrupt().restore(intStatus); //Restore interrupts
		}
	}

	/**
	 * Wake up all threads sleeping on this condition variable. The current
	 * thread must hold the associated lock.
	 */
	public void wakeAll() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		//wake up ALL threads in waitQueue
		while(!waitQueue.isEmpty()){
			wake();
		}
	}

    /**
	 * Atomically release the associated lock and go to sleep on
	 * this condition variable until either (1) another thread
	 * wakes it using <tt>wake()</tt>, or (2) the specified
	 * <i>timeout</i> elapses.  The current thread must hold the
	 * associated lock.  The thread will automatically reacquire
	 * the lock before <tt>sleep()</tt> returns.
	 */
        public void sleepFor(long timeout) {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		boolean intStatus = Machine.interrupt().disable(); // Disable interrupts
		conditionLock.release(); //release the associated lock

		KThread currentThread = KThread.currentThread();

		// Add current thread to wait queue
		waitQueue.add(currentThread);
		ThreadedKernel.alarm.waitUntil(timeout);

		conditionLock.acquire();
		Machine.interrupt().restore(intStatus); //Restore interrupts
	}

    // Example of the "interlock" pattern where two threads strictly
    // alternate their execution with each other using a condition
    // variable.  (Also see the slide showing this pattern at the end
    // of Lecture 6.)

    private static class InterlockTest {
        private static Lock lock;
        private static Condition2 cv;

        private static class Interlocker implements Runnable {
            public void run () {
                lock.acquire();
                for (int i = 0; i < 10; i++) {
                    System.out.println(KThread.currentThread().getName());
                    cv.wake();   // signal
                    cv.sleep();  // wait
                }
                lock.release();
            }
        }

    public InterlockTest () {
 		System.out.println("\n\n\nInterlock Test\n\n\n");
        lock = new Lock();
        cv = new Condition2(lock);

        KThread ping = new KThread(new Interlocker());
        ping.setName("ping");
        KThread pong = new KThread(new Interlocker());
        pong.setName("pong");

        ping.fork();
        pong.fork();

        // We need to wait for ping to finish, and the proper way
        // to do so is to join on ping.  (Note that, when ping is
        // done, pong is sleeping on the condition variable; if we
        // were also to join on pong, we would block forever.)
        // For this to work, join must be implemented.  If you
        // have not implemented join yet, then comment out the
        // call to join and instead uncomment the loop with
        // yields; the loop has the same effect, but is a kludgy
        // way to do it.
        ping.join();
        // for (int i = 0; i < 50; i++) { KThread.currentThread().yield(); }
    }
}
    // Test programs should have exactly the same behavior with the
    // Condition and Condition2 classes.  You can first try a test with
    // Condition, which is already provided for you, and then try it
    // with Condition2, which you are implementing, and compare their
    // behavior.

    // Do not use this test program as your first Condition2 test.
    // First test it with more basic test programs to verify specific
    // functionality.

    public static void cvTest5() {
    	System.out.println("\n\n\nInterlock Test 2\n\n\n");
        final Lock lock = new Lock();
        // final Condition empty = new Condition(lock);
        final Condition2 empty = new Condition2(lock);
        final LinkedList<Integer> list = new LinkedList<>();

        KThread consumer = new KThread( new Runnable () {
                public void run() {
                    lock.acquire();
                    while(list.isEmpty()){
                        empty.sleep();
                    }
                    Lib.assertTrue(list.size() == 5, "List should have 5 values.");
                    while(!list.isEmpty()) {
                        // context swith for the fun of it
                        KThread.currentThread().yield();
                        System.out.println("Removed " + list.removeFirst());
                    }
                    lock.release();
                }
            });

        KThread producer = new KThread( new Runnable () {
                public void run() {
                    lock.acquire();
                    for (int i = 0; i < 5; i++) {
                        list.add(i);
                        System.out.println("Added " + i);
                        // context swith for the fun of it
                        KThread.currentThread().yield();
                    }
                    empty.wake();
                    lock.release();
                }
            });

        consumer.setName("Consumer");
        producer.setName("Producer");
        consumer.fork();
        producer.fork();

        // We need to wait for the consumer and producer to finish,
        // and the proper way to do so is to join on them.  For this
        // to work, join must be implemented.  If you have not
        // implemented join yet, then comment out the calls to join
        // and instead uncomment the loop with yield; the loop has the
        // same effect, but is a kludgy way to do it.
        consumer.join();
        producer.join();
        // for (int i = 0; i < 50; i++) { KThread.currentThread().yield(); }
    }
        // Place sleepFor test code inside of the Condition2 class.

    private static void sleepForTest1 () {
    	System.out.println ("\n\nsleepForTest1 \n\n");
		Lock lock = new Lock();
		Condition2 cv = new Condition2(lock);

		lock.acquire();
		long t0 = Machine.timer().getTime();
		long sleepfor = 40000;
		System.out.println (KThread.currentThread().getName() + " sleeping for "+sleepfor);
		// no other thread will wake us up, so we should time out
		cv.sleepFor(sleepfor);
		long t1 = Machine.timer().getTime();
		System.out.println (KThread.currentThread().getName() +
				    " woke up, slept for " + (t1 - t0) + " ticks");
		lock.release();
    }

    private static void sleepForTest2 () {
    	System.out.println ("\n\nsleepForTest2 \n\n");
		Lock lock = new Lock();
		Condition2 cv = new Condition2(lock);

		KThread t = new KThread( new Runnable () {
			public void run() {
				lock.acquire();
				cv.wakeAll();
				lock.release();
			}
		});
		t.setName("t").fork();

		lock.acquire();
		long t0 = Machine.timer().getTime();
		long sleepfor = 50000000;
		System.out.println (KThread.currentThread().getName() + " sleeping for "+sleepfor);
		// no other thread will wake us up, so we should time out
		cv.sleepFor(sleepfor);
		System.out.println ("\nCurrent thread - "+KThread.currentThread().getName() + " calling wakeAll() ");
		long t1 = Machine.timer().getTime();
		System.out.println (KThread.currentThread().getName() +
				    " woke up, slept for " + (t1 - t0) + " ticks");
		lock.release();
    }

    public static void selfTest() {
        new InterlockTest();
        cvTest5();
        sleepForTest1();
        sleepForTest2();
    }

    private Lock conditionLock;
}
