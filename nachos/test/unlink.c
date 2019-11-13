/*
 * close.c
 * Test basic Close Functionality 
 */

#include "syscall.h"
#include "stdlib.h"

int main(int argc, char *argv[])
{
    int val = 0;
    val = unlink1();
    val = unlink2();
    printf("\n\nALL GOOD PASSED\n\n");
    return val;
}

// Unlink a file that doesnt exists on disk / not opened
// Returns success only if the file system successfully removes the file
int unlink1()
{
    printf("\nRunning Unlink1\n\n");
    char *name = "NONEXISTENT.txt";
    printf("Unlinking non existent file %s\n", name);

    int val = unlink(name);
    assert(val == -1);

    return 0;
}

// Test complex unlink functionality with miltiple open refrences to same file
int unlink2()
{
    printf("\nRunning Unlink2\n\n");
    printf("Opening/Creating File test.txt\n");
    char *name = "test.txt";
    char *name2 = "test2.txt";

    int fileDesc1 = creat(name);
    assert(fileDesc1 == 2);

    int fileDesc2 = open(name);
    assert(fileDesc2 == 3);

    printf("Unlinking File test.txt\n");
    int unlinkSuccess = unlink(name);
    assert(unlinkSuccess == 0);

    unlinkSuccess = unlink(name);
    assert(unlinkSuccess == -1);

    int fileDesc3 = open(name);
    assert(fileDesc3 == -1);

    int fileDesc4 = creat(name);
    assert(fileDesc4 == fileDesc1);

    int fileDesc5 = creat(name2);
    assert(fileDesc5 == fileDesc2);

    unlinkSuccess = unlink(name2);
    assert(unlinkSuccess == 0);
    unlinkSuccess = unlink(name);
    assert(unlinkSuccess == 0);
    return 0;
}
