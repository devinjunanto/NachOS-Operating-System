/*
 * open.c
 * Test basic Open Functionality 
 */

#include "syscall.h"
#include "stdlib.h"

int main(int argc, char *argv[])
{
    int val = 0;
    val = open1();
    val = open2();
    val = open3();
    val = open4();
    return val;
}

// Open a file that doesnt exists on disk
int open1()
{
    printf("\nRunning Open1\n\n");
    printf("Opening non existent file\n");
    char *name = "nonExistentFile.txt";

    int val = open(name);
    if (val == -1)
        printf("OKAY - Unable to open file(-1 returned)\n");
    else
        printf("BAD - -1 not returned\n");

    return 0;
}

// Open existing file and print fileDescriptor
int open2()
{
    printf("\nRunning Open2\n\n");
    printf("Opening File test.txt\n");
    char *name = "test.txt";

    int val = open(name);

    printf("The file descriptor is - %d\n", val);

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
    int i=5;
    printf("Opening File test.txt multiple times\n");
    char *name = "test.txt";
    while(i<17){
        int val = open(name);
        if(val != -1)
            printf("The file descriptor is - %d\n", val);
        else
            printf("Unable to open file number %d [MAX size is 16]\n",i);
        i++;
    }
    return 0;
}
