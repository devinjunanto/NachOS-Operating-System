/*
 * exec1.c
 *
 * Simple program for testing exec.  It does not pass any arguments to
 * the child.
 */

#include "syscall.h"

int main(int argc, char *argv[])
{
    exec1();
}

int exec1()
{
    printf("\n\nRunning exec1\n");
    char *prog = "write1.coff";
    int pid;

    pid = exec(prog, 0, 0);
    printf("\nexec returned with val - %d", pid);
    if (pid < 0)
    {
        exit(-1);
    }
    exit(0);
}