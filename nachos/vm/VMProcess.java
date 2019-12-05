package nachos.vm;

import java.util.ArrayList;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

/**
 * A <tt>UserProcess</tt> that supports demand-paging.
 */
public class VMProcess extends UserProcess {
	/**
	 * Allocate a new process.
	 */
	public VMProcess() {
		super();
	}

	/**
	 * Save the state of this process in preparation for a context switch. Called by
	 * <tt>UThread.saveState()</tt>.
	 */
	public void saveState() {
		super.saveState();
	}

	/**
	 * Restore the state of this process after a context switch. Called by
	 * <tt>UThread.restoreState()</tt>.
	 */
	public void restoreState() {
		super.restoreState();
	}

	/**
	 * Initializes page tables for this process so that the executable can be
	 * demand-paged.
	 * 
	 * @return <tt>true</tt> if successful.
	 */
	protected boolean loadSections() {
		// initialize all of the TranslationEntries as invalid. This will cause the
		// machine to trigger a page fault exception when the process accesses a page.
		pageTable = new TranslationEntry[numPages];

		for (int i = 0; i < numPages; i++) {
			int unusedIndex = -1;
			pageTable[i] = new TranslationEntry(i, unusedIndex, false, false, false, false); // Initialize all entries
																								// as invalid
			swapMap.add(unusedIndex); // Add -1 numPage Times
		}

		for (int i = 0; i < coff.getNumSections(); i++) {
			CoffSection coffSec = coff.getSection(i);

			for (int y = 0; y < section.getLength(); y++) {
				coffMap.add(i);
				int vpn = section.getFirstVPN() + y;
				pageTable[vpn].readOnly = section.readOnly;
				pageTable[vpn].dirty = false;
				pageTable[vpn].used = false;
				pageTable[vpn].valid = false;
				pageTable[vpn].vpn = y;
				// Also do not initialize the page by, e.g., loading from the COFF file.
			}
		}
		return true;
	}

	/**
	 * Release any resources allocated by <tt>loadSections()</tt>.
	 */
	protected void unloadSections() {
		super.unloadSections();
	}

	/**
	 * Handle a user exception. Called by <tt>UserKernel.exceptionHandler()</tt> .
	 * The <i>cause</i> argument identifies which exception occurred; see the
	 * <tt>Processor.exceptionZZZ</tt> constants.
	 * 
	 * @param cause the user exception that occurred.
	 */
	public void handleException(int cause) {
		Processor processor = Machine.processor();

		switch (cause) {
		case Processor.exceptionPageFault:
			int vAddr = processor.readRegister(processor.readregBadVAddr);
			faultHandler(vAddr);
		default:
			super.handleException(cause);
			break;
		}
	}

	// When the process references an invalid page, the machine will raise a page
	// fault exception. Modify your exception handler to catch this exception and
	// handle it by preparing the requested page on demand.
	private void faultHandler(int vAddr) {
		VMKernel.vmLock.acquire();

		int vpn = Processor.pageFromAddress(vAddr);
		System.out.println("\nPAGE FAULT on page: " + vpn + "(" + vAddr + ")");
		int coffNum, ppn;

		if (vpn < coffMap.size()) {
			// VPN is in CoffMap !
			coffNum = coffMap.get(vpn);
		} else {
			coffNum = -1;
		}
		TranslationEntry entry = pageTable[vpn];
		if (UserKernel.physPagesAvailable.size() > 0) {
			// There are physics pages available
			ppn = VMKernel.physPageNumber(this, vpn);
		} else {
			// No physical pages available
			// ppn = VMKernel.pageReplacement(this, vpn);
			ppn = -1;
			System.out.println("\n HERE no phys pages !");
		}

		// Now we have a ppn, so we link it to vpn and mark it valid
		entry.ppn = ppn;
		entry.valid = true;

		if (!entry.dirty) {
			// Entry is valid and not dirty
			if (coffNum >= 0) {
				CoffSection section = coff.getSection(coffNum);
				int pageOffset = vpn - section.getFirstVPN();
				section.loadPage(pageOffset, ppn);
				entry.readOnly = section.isReadOnly();
			} else {
				byte[] copyFrom = new byte[pageSize]; // Zero filled by default, will be copied into physical
				byte[] memory = Machine.processor.getMemory();
				int physAddress = ppn * pageSize;
				System.arraycopy(copyFrom, 0, memory, physAddress, pageSize);
			}
		} else {
			// Handle Dirty entry swaping
			System.out.println("\n Here in handle swap \n");
		}
		VMKernel.vmLock.release();
	}

	private ArrayList<Integer> coffMap = new ArrayList<Integer>();
	private static final int pageSize = Processor.pageSize;
	private ArrayList<Integer> swapMap = new ArrayList<Integer>();

	private static final char dbgProcess = 'a';

	private static final char dbgVM = 'v';
}
