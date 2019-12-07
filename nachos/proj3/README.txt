Shardul Saiya A13964199
Devin Junanto A15754079

pageReplacement
Set the clock counter
Replace the ppn

swpOut
Increment the current page until it can be swap
Swap the page

swpIn
Return the swap content

physPageNumber
Get the physical page number

readVirtualMemory
Read from virtual memory

writeVirtualMemory
Write to virtual memory

loadSections
Initialize all of the TranslationEntries as invalid to trigger a page fault exception
Initialize all entries
return true

unloadSections
Write on swap

isDirty
Check if the pageTable is dirty or not

handleException
Find a page fault exception

faultHandler
Handle the page fault

The code works well for everything and passed the test

We use the test that the write up provided.

Devin wrote the code in VMKernel.java
Shardul wrote the code in VMProcess.java
We combine everything and debug it together