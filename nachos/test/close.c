/*
 * close.c
 * Test basic Close Functionality 
 */

#include "syscall.h"
#include "stdlib.h"

int main(int argc, char *argv[])
{
    int val = 0;
    val = close1();
    val = close2();
    val = close3();
    // val = open4();
    return val;
}

// Close a file that doesnt exists on disk / not opened
int close1()
{
    printf("\nRunning Close1\n\n");
    printf("Closing non existent openfile num - 7 \n");

    int val = close(7);
    assert(val == -1);
    return 0;
}

// test if it frees up the file descriptor so that it can be used again;
int close2()
{
    printf("\nRunning Close2\n\n");
    printf("Opening/Creating File test.txt\n");
    char *name = "test.txt";
    char *name2 = "test2.txt";

    int val = creat(name);
    printf("Closing File test.txt - %d\n", val);
    int closeVal = close(val);
    assert(closeVal == 0);

    int newVal = open(name);
    printf("Opening/Creating File test2.txt \n");
    printf("The file descriptor is - %d\n", newVal);
    assert(val == newVal);
    return 0;
}

// reading or writing to a file descriptor that was closed returns an error.
int close3()
{
    printf("\nRunning Close3\n\n");
    printf("ReCreating File test.txt\n");
    char *name = "test.txt";
    int fileDesc = creat(name);
    char *str = "Roses are Red\nI am Ded\n";
    int numWritten = write(fileDesc, str, 22);
    assert(numWritten == 22);

    printf("Closing File test.txt\n");
    int closed = close(fileDesc);
    assert(closed == 0);

    printf("Attempting to Write to closed file test.txt\n");
    int numWrittenAgain = write(fileDesc, str, 30);
    assert(numWrittenAgain == -1);
    return 0;
}
