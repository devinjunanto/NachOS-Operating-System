package nachos.threads;

import nachos.machine.*;
import java.util.PriorityQueue;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {

	private class AlarmTimeThread  {
		private KThread thread;
    	private long waketime;
    	public AlarmTimeThread (KThread thread, long waketime){
    		this.thread = thread;
    		this.waketime = waketime;
    	}

    	// Since its a priority queue, we implement comparable so that the different alarms are
    	// at different priority in the queue based on wakeTime
    	public int compareTo(AlarmTimeThread alarmThread2){
    		// This way the queue will have the alarm threads in ascending order of wakeTime
    		if (alarmThread2.waketime > this.waketime) return -1;
    		else{
    			if (alarmThread2.waketime < this.waketime) return 1;
    			else return 0;
    		}
    	}
 
    }

	// private ThreadQueue waitQueue = ThreadedKernel.scheduler.newThreadQueue(false);
	private PriorityQueue<AlarmTimeThread> alarmQueue = new PriorityQueue<AlarmTimeThread>(); 

	private static long alarmTime = 0;
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 * 
	 * <p>
	 * <b>Note</b>: Nachos will not function correctly with more than one alarm.
	 */
	public Alarm() {
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() {
				timerInterrupt();
			}
		});
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */
	public void timerInterrupt() {

		long currentTime = Machine.timer().getTime();

		boolean intStatus = Machine.interrupt().disable();

		//Set off first alarm in queue as soon as its wakeTime is less than current time
		while(!alarmQueue.isEmpty() && alarmQueue.peek().waketime <= currentTime ){
			AlarmTimeThread alarmReady = alarmQueue.poll();
			KThread alarmThread = alarmReady.thread;

			if (alarmThread != null) {
				alarmThread.ready();
			}
		}

		// if (currentTime >= alarmTime) {
		// 	System.out.println("\n\n\n\n\n HERE WE MADE IT \n\n\n\n\n");
		// 	KThread thread = waitQueue.nextThread();
		// 	if (thread != null) {
		// 		thread.ready();
		// 	}
		// }
		// else{
		// 	System.out.println(currentTime+"/"+alarmTime);
		// }
		// KThread thread = waitQueue.nextThread();
		// if (thread != null) {
		// 	thread.ready();
		// }

		// make current thread yield
		KThread.currentThread().yield();
		Machine.interrupt().restore(intStatus);
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks, waking it up
	 * in the timer interrupt handler. The thread must be woken up (placed in
	 * the scheduler ready set) during the first timer interrupt where
	 * 
	 * <p>
	 * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
	 * 
	 * @param x the minimum number of clock ticks to wait.
	 * 
	 * @see nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {

		//Handle negative and small times
		if (x <= 0 ) {
			return;			
		}
		long wakeTime = Machine.timer().getTime() + x;
		KThread currentThread = KThread.currentThread();
		AlarmTimeThread alarmTimeThread = new AlarmTimeThread(currentThread, wakeTime);
		
		boolean intStatus = Machine.interrupt().disable();

		alarmQueue.add(alarmTimeThread);
		currentThread.sleep();
		Machine.interrupt().restore(intStatus);
	}

        /**
	 * Cancel any timer set by <i>thread</i>, effectively waking
	 * up the thread immediately (placing it in the scheduler
	 * ready set) and returning true.  If <i>thread</i> has no
	 * timer set, return false.
	 * 
	 * <p>
	 * @param thread the thread whose timer should be cancelled.
	 */
        public boolean cancel(KThread thread) {
		return false;
	}

	// Add Alarm testing code to the Alarm class
    
    public static void alarmTest1() {
		int durations[] = {100, 0, 100*1000};
		long t0, t1;
		for (int d : durations) {
			t0 = Machine.timer().getTime();
		    ThreadedKernel.alarm.waitUntil (d);
		    t1 = Machine.timer().getTime();
		    System.out.println ("alarmTest1: waited for " + (t1 - t0) + " ticks");
		}
    }

     public static void alarmTest2() {
		int durations[] = {4000*1000, -10, 1000000};
		long t0, t1;
		
		for (int d : durations) {
			t0 = Machine.timer().getTime();
		    ThreadedKernel.alarm.waitUntil (d);
		    t1 = Machine.timer().getTime();
		    System.out.println ("alarmTest2: waited for " + (t1 - t0) + " ticks");
		}
    }

    // Implement more test methods here ...

    // Invoke Alarm.selfTest() from ThreadedKernel.selfTest()
    public static void selfTest() {
	alarmTest1();
	alarmTest2();

	// Invoke your other test methods here ...
    }
}
