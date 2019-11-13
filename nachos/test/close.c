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
    // val = open3();
    // val = open4();
    return val;
}

// Close a file that doesnt exists on disk / not opened
int close1()
{
    printf("\nRunning Close1\n\n");
    printf("Closing non existent openfile num - 7 \n");

    int val = close(7);
    if (val == -1)
        printf("OKAY - Unable to close file(-1 returned)\n");
    else
        printf("BAD - -1 not returned\n");
    
    assert(val == -1);

    return 0;
}

// Open existing file and print fileDescriptor
int close2()
{
    printf("\nRunning Close2\n\n");
    printf("Opening File test.txt\n");
    char *name = "test.txt";
    char *name2 = "test2.txt";

    int val = open(name);

    printf("The file descriptor is - %d\n", val);
    int closeVal = close(val);
    printf("Closeing File test.txt - %d\n",val);

    int newVal = creat(name2);
    printf("Opening/Creating File test2.txt\n");
    printf("The file descriptor is - %d\n", val);
    assert(val == newVal);
    return 0;
}

// Open existing file twice and verify that different file descriptors are returned
int open3()
{
    printf("\nRunning Open3\n\n");
    printf("Opening File test.txt\n");
    char *name = "test.txt";
    int val = open(name);
    printf("Opening File test.txt again!!\n");
    int val2 = open(name);

    assert(val != val2);
    printf("The file descriptors are - %d and %d\n", val, val2);

    return 0;
}

//each process can use 16 file descriptors.
int open4()
{
    printf("\nRunning Open4\n\n");
    int i = 5;
    printf("Opening File test.txt multiple times\n");
    char *name = "test.txt";
    while (i < 17)
    {
        int val = open(name);
        if (val != -1)
            printf("The file descriptor is - %d\n", val);
        else
            printf("Unable to open file number %d [MAX size is 16]\n", i);
        i++;
    }
    return 0;
}
