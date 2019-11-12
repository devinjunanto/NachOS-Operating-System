/*
 * write1.c
 *
 * Write a string to stdout, one byte at a time.  Does not require any
 * of the other system calls to be implemented.
 *  works on both stdout and on files; 
 * checks arguments for correctness (valid file descriptor, file buffer pointer, count); 
 * the data written is actually stored in the file; 
 * the number of bytes written matches count.
 */

#include "syscall.h"
#include "stdlib.h"
#include "stdio.h"

int main(int argc, char *argv[])
{
	int val = 0;
	val = write1();
	val = write2();
	val = write3();
	val = write4();
	return val;
}

int write1()
{
	printf("\nRunning Write1\n\n");
	char *str = "\nroses are red\nviolets are blue\nI hate Nachos\nand so do you\n\n";

	while (*str)
	{
		int r = write(1, str, 1);
		if (r != 1)
		{
			printf("failed to write character (r = %d)\n", r);
			exit(-1);
		}
		str++;
	}

	return 0;
}

//Write to file test
int write2()
{
	printf("\nRunning Write2\n\n");
	char *str = "\nroses are red\nviolets are blue\nI hate Nachos\nand so do you\n\n";
	int fileDesc = creat("writeTest.txt");
	while (*str)
	{
		int r = write(fileDesc, str, 1);
		if (r != 1)
		{
			printf("failed to write character (r = %d)\n", r);
			exit(-1);
		}
		str++;
	}
	printf("Finished writing to file\n");

	return 0;
}

int write3()
{
	char str[2000];
	int writeCount = 1000;

	printf("\nRunning Write3\n\n");
	for (int i = 0; i < writeCount; i++)
	{
		strcat(str, 'a');
	}
	strcat(str, '\n'); // new line !

	int fileDesc = creat("LONGwriteTest.txt");
	int r = write(fileDesc, str, writeCount);
	printf("Just wrote %d bytes to file number %d", writeCount, fileDesc);
}

/*
 * write4.c
 *
 * Echo lines of input to the output.  Terminate on a ".".  Requires basic
 * functionality for both write and read.
 *
 * Invoking as "java nachos.machine.Machine -x write4.coff" will echo
 * the characters you type at the prompt (using the "../bin/nachos"
 * script turns off echo).
 *
 * Geoff Voelker
 * 11/9/15
 */

int write4()
{
	printf("\nRunning Write4\n\n");
	char buffer[80];
	char prompt[4];
	int i, n;

	prompt[0] = '-';
	prompt[1] = '>';
	prompt[2] = ' ';
	prompt[3] = '\0';

	while (1)
	{
		// print the prompt
		puts(prompt);

		// read the input terminated by a newline
		i = 0;
		do
		{
			buffer[i] = getchar();
		} while (buffer[i++] != '\n');
		buffer[i] = '\0';

		// if the input is just a period, then exit
		if (buffer[0] == '.' &&
			buffer[1] == '\n')
		{
			return 0;
		}

		// echo the input to the output
		puts(buffer);
	}
}
