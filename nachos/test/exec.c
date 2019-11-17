/*
 * exec1.c
 *
 * Simple program for testing exec.  It does not pass any arguments to
 * the child.
 */

#include "syscall.h"

int main(int argc, char *argv[])
{
    int val = exec1();
    //val = exec2();
    //exit(0);
}

int exec1()
{
    printf("\n\nRunning exec1\n");
    char *prog = "exit.coff";
    int pid;

    pid = exec(prog, 0, 0);
    if (pid < 0)
    {
        exit(-1);
    }
    printf("\n\Executed prog and gave it PID - %d\n", pid);
    return pid;
}

int exec2()
{
    printf("\n\nRunning exec2\n");
    char *prog = "exit.coff";
    int pid;

    pid = exec(prog, 0, 0);
    if (pid < 0)
    {
        exit(-1);
    }
    printf("\n\Executed prog and gave it PID - %d\n", pid);
    exit(0);
}