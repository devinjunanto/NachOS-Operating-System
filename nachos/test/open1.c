/*
 * open1.c
 * Test basic Open Functionality - open a file that doesnt exists on disk
 */

#include "syscall.h"
#include "stdlib.h"

int main(int argc, char *argv[])
{
    printf("\nRunning Open1\n\n");
    char *name = "nonExistentFile.txt";

    int val = open(name);
    assert(val == -1);

    return 0;
}