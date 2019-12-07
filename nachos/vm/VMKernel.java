package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * A kernel that can support multiple demand-paging user processes.
 */
public class VMKernel extends UserKernel {
	/**
	 * Allocate a new VM kernel.
	 */
	public VMKernel() {
		super();
		pageSize = Machine.processor().getNumPhysPages();
		int i = 0;
		while (i < pageSize) {
			clkCtr.add(new Integer[] { -1, -1 });
			processes.add(null);
			i++;
		}
		clkIdx = 0;
	}

	/**
	 * Initialize this kernel.
	 */
	public void initialize(String[] args) {
		super.initialize(args);
		currentFile = ThreadedKernel.fileSystem.open("swap", true);
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
		currentFile.close();
		ThreadedKernel.fileSystem.remove("swap");
		System.out.println("in TERMINATE");
		super.terminate();
	}

	/*
	 * Page replacement helper to free up a physical page frame to handle page
	 * faults: Helps to Evict pages once physical memory becomes full.
	 */
	public static int pageReplacement(VMProcess newProcess, int vpn) {
		// int[] clockNumbers = clkCtr.get(clkIdx);

		// Need to select a victim page to evict from memory, using clock algorithm
		System.out.println("in PAGE REPLACEMENT");
		int clock = clkCtr.get(clkIdx)[1];
		while (clock > 0) {
			clkCtr.get(clkIdx)[1] = 0;
			clkIdx++;
			if (clkIdx == pageSize)
				clkIdx = 0;
			clock = clkCtr.get(clkIdx)[1];
		}
		System.out.println("2 - Victim ppn - " + clkIdx);
		int ppnToReplace = clkIdx; // victim page

		VMProcess oldProcess = processes.get(ppnToReplace);

		// Then mark the TranslationEntry for that page as invalid - done in unload
		oldProcess.unloadSections(clkCtr.get(ppnToReplace)[0].intValue());
		clkCtr.set(ppnToReplace, new Integer[] { vpn, 1 });
		processes.set(ppnToReplace, newProcess);
		System.out.println('3');
		return ppnToReplace;
	}

	public static void printTable() {
		System.out.print("\n");
		int i = 0;
		while (i < 40) {
			System.out.print("-");
			i++;
		}
		System.out.print("\n");
		i = 0;
		while (i < clkCtr.size()) {
			System.out.print(i + "\t|\t" + clkCtr.get(i)[0] + "\t|\t" + clkCtr.get(i)[1] + "\n");
			i++;
		}
		i = 0;
		while (i < 40) {
			System.out.print("-");
			i++;
		}
		System.out.print("\n");
	}

	public static int swpOut(int ppn) {
		System.out.println("\nIn SWP OUT");
		int swapIndex = 0;
		if (freeToSwap.size() == 0) {
			// No pages to swap
			swapIndex = currPg;
			// Increment Current Page
			currPg++;
		} else {
			// Can swap
			swapIndex = freeToSwap.pop();
		}
		byte[] memory = Machine.processor().getMemory();
		int written = currentFile.write(swapIndex * Processor.pageSize, memory, ppn * Processor.pageSize,
				Processor.pageSize);
		System.out.println("\nExit SWP OUT");
		return swapIndex;
	}

	/* Helper Method that will create a swap file */
	public static byte[] swpIn(int swapIndex) {
		System.out.println("\nEnter SWP IN");
		byte[] content = new byte[Processor.pageSize];
		currentFile.read(swapIndex * Processor.pageSize, content, 0, Processor.pageSize);
		System.out.println("\nExit SWP IN");
		return content;
	}

	public static int physPageNumber(VMProcess process, int vpn) {
		System.out.println("\nEnter physPageNum");
		int ppn = physPagesAvailable.pop();
		clkCtr.set(ppn, new Integer[] { vpn, 1 });
		processes.set(ppn, process);
		System.out.println("\nExit physPageNum");
		return ppn;
	}

	// dummy variables to make javac smarter
	private static VMProcess dummy1 = null;

	private static final char dbgVM = 'v';

	private static int pageSize;

	private static ArrayList<Integer[]> clkCtr = new ArrayList<Integer[]>();

	private static ArrayList<VMProcess> processes = new ArrayList<VMProcess>();

	private static int clkIdx;

	private static OpenFile currentFile;

	public static LinkedList<Integer> freeToSwap = new LinkedList<Integer>();

	private static int currPg = 0;
}
