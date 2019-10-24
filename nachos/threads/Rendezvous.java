package nachos.threads;

import nachos.machine.*;
import java.util.*; //import util and priorityQueue

/**
 * A <i>Rendezvous</i> allows threads to synchronously exchange values.
 */
public class Rendezvous {
    /**
     * Allocate a new Rendezvous.
     */
    public Rendezvous () {
        lock = new Lock();
        cv = new Condition(lock);
    }
    private static PriorityQueue<RendezvousThread> waitQueue =new PriorityQueue<RendezvousThread>();; // waitQueue to hold values
    private Lock lock;
    private Condition cv;

    private class RendezvousThread implements Comparable<RendezvousThread> {
        private int value;
        private int tag;
        public RendezvousThread (int value, int tag){
            this.value = value;
            this.tag = tag;
        }

        // Since its a priority queue, we implement comparable so that the different alarms are
        // at different priority in the queue based on wakeTime
        public int compareTo(RendezvousThread RendezvousThread2){
            // This way the queue will have the alarm threads in ascending order of wakeTime
            if (RendezvousThread2.tag > this.tag) return -1;
            else{
                if (RendezvousThread2.tag < this.tag) return 1;
                else return 0;
            }
        }
    }

    /**
     * Synchronously exchange a value with another thread.  The first
     * thread A (with value X) to exhange will block waiting for
     * another thread B (with value Y).  When thread B arrives, it
     * will unblock A and the threads will exchange values: value Y
     * will be returned to thread A, and value X will be returned to
     * thread B.
     *
     * Different integer tags are used as different, parallel
     * synchronization points (i.e., threads synchronizing at
     * different tags do not interact with each other).  The same tag
     * can also be used repeatedly for multiple exchanges.
     *
     * @param tag the synchronization tag.
     * @param value the integer to exchange.
     */
    public int exchange2 (int tag, int value) {
        lock.acquire();
        RendezvousThread rt = new RendezvousThread(value, tag);
        if(waitQueue.isEmpty() ){
            System.out.println("1");
            waitQueue.add(rt); // Add to waitQueue and then sleep
            cv.sleep();
            System.out.println("5");

            System.out.println(waitQueue);
            RendezvousThread rtNew = waitQueue.poll();
            int val = rtNew.value;
            return val;
        }else{
            System.out.println(waitQueue);
            RendezvousThread rtNew = waitQueue.poll();
            int val = rtNew.value;
            System.out.println("2");
            waitQueue.add(rt);
            cv.wake();
            System.out.println("3");
            lock.release();
            System.out.println("4");
            return val;
        }
    }
    public int exchange (int tag, int value) {
        lock.acquire();
        RendezvousThread rt = new RendezvousThread(value, tag);
        if(waitQueue.isEmpty() || waitQueue.peek().tag != tag){
            System.out.println("1");
            waitQueue.add(rt); // Add to waitQueue and then sleep
            cv.sleep();
            System.out.println("5");

            System.out.println(waitQueue);
            RendezvousThread rtNew = waitQueue.poll();
            int val = rtNew.value;
            // if(!waitQueue.isEmpty()){
            //     System.out.println("HERE");
            //     RendezvousThread rtNext = waitQueue.poll();
            //     lock.release()
            //     this.exchange(rtNext.tag, rtNext.value);
            // }
            return val;
        }else{
            System.out.println(waitQueue);
            RendezvousThread rtNew = waitQueue.poll();
            int val = rtNew.value;
            System.out.println("2");
            waitQueue.add(rt);
            cv.wake();
            System.out.println("3");
            lock.release();
            System.out.println("4");
            return val;
        }
    }

    public static void rendezTest1() {
        final Rendezvous r = new Rendezvous();

        KThread t1 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = -1;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue (recv == 1, "Was expecting " + 1 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t1.setName("t1");
        KThread t2 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = 1;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue (recv == -1, "Was expecting " + -1 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t2.setName("t2");

        t1.fork(); t2.fork();
        // assumes join is implemented correctly
        t1.join(); t2.join();
    }

    public static void rendezTest2() {
        final Rendezvous r = new Rendezvous();

        KThread t1 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = -1;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue (recv == -2, "Was expecting " + -2 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t1.setName("t1");

        KThread t2 = new KThread( new Runnable () {
            public void run() {
                int tag = 1;
                int send = 1;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue (recv == 2, "Was expecting " + 2 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t2.setName("t2");  

        KThread t3 = new KThread( new Runnable () {
            public void run() {
                int tag = 1;
                int send = 2;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue (recv == 1, "Was expecting " + 1 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t3.setName("t3");

        KThread t4 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = -2;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue (recv == -1, "Was expecting " + -1 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t4.setName("t4");

        t1.fork(); t2.fork(); t3.fork(); t4.fork();
        // assumes join is implemented correctly
        t1.join(); t2.join(); t3.join(); t4.join();
    }

    // Invoke Rendezvous.selfTest() from ThreadedKernel.selfTest()

    public static void selfTest() {
        // place calls to your Rendezvous tests that you implement here
        rendezTest1();
        rendezTest2();
    }
}
