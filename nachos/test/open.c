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
    return 0;
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
    
    int val = open(*name);
    
    printf("The file descriptor is - ");
    printf(val);
    
    return 0;
}