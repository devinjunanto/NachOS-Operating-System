package nachos.vm;

import java.util.ArrayList;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

/**
 * A kernel that can support multiple demand-paging user processes.
 */
public class VMKernel extends UserKernel {
	/**
	 * Allocate a new VM kernel.
	 */
	public VMKernel() {
		super();
	}

	/**
	 * Initialize this kernel.
	 */
	public void initialize(String[] args) {
		super.initialize(args);
	}

	/**
	 * Test this kernel.
	 */
	public void selfTest() {
		super.selfTest();
	}

	/**
	 * Start running user programs.
	 */
	public void run() {
		super.run();
	}

	/**
	 * Terminate this kernel. Never returns.
	 */
	public void terminate() {
		super.terminate();
	}

	// dummy variables to make javac smarter
	private static VMProcess dummy1 = null;

	public static Lock vmLock;

	private static final char dbgVM = 'v';

	// * Helper Methods !! *//

	/*
	 * Finds next available physical page, links it to calling process, and then
	 * returns new ppn
	 */
	public static int physPageNumber(VMProcess process, int vpn) {
		int ppn = physPagesAvailable.pop();
		processes.set(ppn, process);
		return  ppn;
	}

	public static int pageReplacement(VMProcess pricess, int vpn){
		return -1;	
	}

	// Kernel maintains list of processes
	private static ArrayListt<VMProcess> processes = new ArrayList<VMProcess>();
}
