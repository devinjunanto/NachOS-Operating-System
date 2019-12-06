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
		super.terminate();
	}

	public static int pageReplacement(VMProcess newProcess, int vpn) {
		// int[] clockNumbers = clkCtr.get(clkIdx);
		int clock = clkCtr.get(clkIdx)[1];
		while (clock > 0) {
			clkCtr.get(clkIdx)[1] = 0;
			clkIdx++;
			if (clkIdx == pageSize)
				clkIdx = 0;
			clock = clkCtr.get(clkIdx)[1];
		}
		int ppnToReplace = clkIdx;
		VMProcess oldProcess = processes.get(ppnToReplace);
		oldProcess.unloadSections(clkCtr.get(ppnToReplace)[0].intValue());
		clkCtr.set(ppnToReplace, new Integer[] { vpn, 1 });
		processes.set(ppnToReplace, newProcess);
		return ppnToReplace;
	}

	// public static void printTable() {
	// System.out.print("\n");
	// int i = 0;
	// while (i < 40) {
	// System.out.print("-");
	// i++;
	// }
	// System.out.print("\n");
	// i = 0;
	// while (i < clkCtr.size()) {
	// System.out.print(i + "\t|\t" + clkCtr.get(i_[0] + "\t|\t" + clkCtr.get(i)[1]
	// + "\n"));
	// i++;
	// }
	// i = 0;
	// while (i < 40) {
	// System.out.print("-");
	// i++;
	// }
	// System.out.print("\n");
	// }

	public static int swpOut(int ppn) {
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
		return swapIndex;
	}

	public static byte[] swpIn(int swapIndex) {
		byte[] content = new byte[Processor.pageSize];
		currentFile.read(swapIndex * Processor.pageSize, content, 0, Processor.pageSize);
		return content;
	}

	public static int physPageNumber(VMProcess process, int vpn) {
		int ppn = physPagesAvailable.pop();
		clkCtr.set(ppn, new Integer[] { vpn, 1 });
		processes.set(ppn, process);
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
