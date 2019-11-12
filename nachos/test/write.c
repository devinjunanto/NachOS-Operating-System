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
	val = write5();
	val = SNEK(argc, *argv);
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

//Writes a really long strong into the file
int write3()
{
	char str[(2000+100)];
	int writeCount = 2000;
	char add[] = 'A';

	printf("\nRunning Write3\n\n");
	for (int i = 0; i < writeCount; i++)
	{
		strcat(str, add);
	}
	strcat(str, '\n'); // new line !

	int fileDesc = creat("LONGwriteTest.txt");
	int r = write(fileDesc, str, writeCount);
	printf("Just wrote %d bytes to file number %d", r, fileDesc);
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

/*
 * write10.c
 *
 * Test the write system call under a variety of good and bad
 * conditions, verifying output where possible.  Requires basic
 * functionality for open, creat, close, and read.
 *
 * Motto: Always check the return value of system calls.
 *
 * Geoff Voelker
 * 11/9/15
 */

int bigbuf1[1024];
int bigbuf2[1024];
int bigbufnum = 1024;

int do_creat(char *fname)
{
	int fd;

	printf("creating %s...\n", fname);
	fd = creat(fname);
	if (fd >= 0)
	{
		printf("...passed (fd = %d)\n", fd);
	}
	else
	{
		printf("...failed (%d)\n", fd);
		exit(-1001);
	}
	return fd;
}

int do_open(char *fname)
{
	int fd;

	printf("opening %s...\n", fname);
	fd = open(fname);
	if (fd >= 0)
	{
		printf("...passed (fd = %d)\n", fd);
	}
	else
	{
		printf("...failed (%d)\n", fd);
		exit(-1002);
	}
	return fd;
}

void do_close(int fd)
{
	int r;

	printf("closing %d...\n", fd);
	r = close(fd);
	if (r < 0)
	{
		printf("...failed (r = %d)\n", r);
		exit(-1003);
	}
}

/*
 * Write "len" bytes of "buffer" into the file "fname".  "stride"
 * controls how many bytes are written in each system call.
 */
void do_write(char *fname, char *buffer, int len, int stride)
{
	int fd, r, remain;
	char *ptr;

	fd = do_creat(fname);

	ptr = buffer, remain = len;
	printf("writing %d bytes to file, %d bytes at a time...\n", len, stride);
	while (remain > 0)
	{
		int n = ((remain < stride) ? remain : stride);
		r = write(fd, ptr, n);
		if (r < 0)
		{
			printf("...failed (r = %d)\n", r);
			exit(-1004);
		}
		else if (r != n)
		{
			printf("...failed (expected to write %d bytes, but wrote %d)\n", n, r);
			exit(-1005);
		}
		else
		{
			printf("...passed (wrote %d bytes)\n", r);
		}

		ptr += stride;
		remain -= stride;
	}

	do_close(fd);
}

/*
 * Validate that the bytes of the file "fname" are the same as the
 * bytes in "truth".  Only compare "len" number of bytes.  "buffer" is
 * the temporary buffer used to read the contents of the file.  It is
 * allocated by the caller and needs to be at least "len" number of
 * bytes in size.
 */
void do_validate(char *fname, char *buffer, char *truth, int len)
{
	int fd, r;

	fd = do_open(fname);

	printf("reading %s into buffer...\n", fname);
	r = read(fd, buffer, len);
	if (r < 0)
	{
		printf("...failed (r = %d)\n", r);
		do_close(fd);
		return;
	}
	else if (r != len)
	{
		printf("...failed (expected to read %d bytes, but read %d)\n", len, r);
		do_close(fd);
		return;
	}
	else
	{
		printf("...success\n");
	}

	r = 0;
	printf("validating %s...\n", fname);
	while (r < len)
	{
		if (buffer[r] != truth[r])
		{
			printf("...failed (offset %d: expected %c, read %c)\n",
				   r, truth[r], buffer[r]);
			exit(-1006);
			break;
		}
		r++;
	}
	if (r == len)
	{
		printf("...passed\n");
	}

	do_close(fd);
}

int write5()
{
	char buffer[128], *file, *ptr;
	int buflen = 128;
	int fd, r, len, i;

	/* write a small amount of data in a few different ways */
	file = "write.out";
	char *str = "roses are red\nviolets are blue\nI love Nachos\nand so do you\n";
	len = strlen(str);

	/* write all bytes at once */
	do_write(file, str, len, len);
	do_validate(file, buffer, str, len);

	/* write 8 bytes at a time */
	do_write(file, str, len, 8);
	do_validate(file, buffer, str, len);

	/* write 1 byte at a time */
	do_write(file, str, len, 1);
	do_validate(file, buffer, str, len);

	/* ok, now write lots of binary data.  if you want to manually
     * confirm what was written, running "od -i ../test/binary.out"
     * will print the file and interpret the data as integers. */
	file = "binary.out";
	len = sizeof(bigbuf1); /* len in units of bytes, bigbufnum in ints */
	for (i = 0; i < bigbufnum; i++)
	{
		bigbuf1[i] = i;
	}

	/* write all at once */
	do_write(file, (char *)bigbuf1, len, len);
	do_validate(file, (char *)bigbuf2, (char *)bigbuf1, len);

	/* write 128 bytes at a time */
	do_write(file, (char *)bigbuf1, len, 128);
	do_validate(file, (char *)bigbuf2, (char *)bigbuf1, len);

	/* test corner cases for each of the three parameters to the write
     * system call. */

	/* test fd */
	fd = -10, len = 10; /* value of len should not matter... */
	printf("writing to an invalid fd (%d)...\n", fd);
	r = write(fd, buffer, len);
	if (r < 0)
	{
		printf("...passed (r = %d)\n", r);
	}
	else
	{
		printf("...failed (r = %d, should be -1)\n", r);
		exit(-2000);
	}

	fd = 256, len = 10; /* value of len should not matter... */
	printf("writing to an invalid fd (%d)...\n", fd);
	r = write(fd, buffer, len);
	if (r < 0)
	{
		printf("...passed (r = %d)\n", r);
	}
	else
	{
		printf("...failed (r = %d, should be -1)\n", r);
		exit(-3000);
	}

	fd = 8, len = 10; /* value of len should not matter... */
	printf("writing to an unopened fd (%d)...\n", fd);
	r = write(fd, buffer, len);
	if (r < 0)
	{
		printf("...passed (r = %d)\n", r);
	}
	else
	{
		printf("...failed (r = %d, should be -1)\n", r);
		exit(-4000);
	}

	file = "bad.out";
	fd = do_creat(file);

	/* test buffer */
	printf("writing count = 0 bytes...\n");
	r = write(fd, buffer, 0);
	if (r == 0)
	{
		printf("...passed\n");
	}
	else
	{
		printf("...failed (r = %d)\n", r);
		exit(-5000);
	}

	printf("writing with an invalid buffer (should not crash, only return an error)...\n");
	r = write(fd, (char *)0xBADFFF, 10);
	if (r < 0)
	{
		printf("...passed (r = %d)\n", r);
	}
	else
	{
		printf("...failed (r = %d)\n", r);
		exit(-6000);
	}

	/* test count */
	printf("writing with an invalid count (should not crash, only return an error)...\n");
	r = write(fd, (char *)str, -1);
	if (r < 0)
	{
		printf("...passed (r = %d)\n", r);
	}
	else
	{
		printf("...failed (r = %d)\n", r);
		exit(-7000);
	}

	printf("writing with a buffer that extends beyond the end of the\n");
	printf("address space.  write should return an error.\n");
	r = write(fd, (char *)0, (80 * 1024));
	if (r > 0)
	{
		printf("...failed (r = %d)\n", r);
		exit(-8000);
	}
	else
	{
		printf("...passed (r = %d)\n", r);
	}
	return 0;
}

/*
 * snake.c -- draw a snake randomly moving around on the screen
 *
 * Geoff Voelker
 * 11/20/04
 */

char cmd[16];

#define WRITE(_cmd, _len) write(1, _cmd, _len)
/* #define WRITE(_cmd, _len) Write(_cmd, _len, 1) */

#define CMD3(_a, _b, _c) \
	cmd[0] = 0x1B;       \
	cmd[1] = _a;         \
	cmd[2] = _b;         \
	cmd[3] = _c;         \
	cmd[4] = '\0';       \
	WRITE(cmd, 5);
#define CMD7(_a, _b, _c, _d, _e, _f, _g) \
	cmd[0] = 0x1B;                       \
	cmd[1] = _a;                         \
	cmd[2] = _b;                         \
	cmd[3] = _c;                         \
	cmd[4] = _d;                         \
	cmd[5] = _e;                         \
	cmd[6] = _f;                         \
	cmd[7] = _g;                         \
	cmd[8] = '\0';                       \
	WRITE(cmd, 9)

#define putch(_a)  \
	cmd[0] = _a;   \
	cmd[1] = '\0'; \
	WRITE(cmd, 2)

#define MAX_ROWS 23
#define MAX_COLS 79

#define NUM_WORM_SEGS 4

#define DOWN 0
#define UP 1
#define LEFT 2
#define RIGHT 3

typedef struct worm_t
{
	int head;
	int nsegs;
	int ticks;
	int dir;
	char row[NUM_WORM_SEGS];
	char col[NUM_WORM_SEGS];
} worm_t;

unsigned int seed;

void irand_init()
{
	seed = 93186752;
}

/* a very poor random number generator */
unsigned int
irand(int l, int h)
{
	unsigned int a = 1588635695, m = 429496U, q = 2, r = 1117695901;
	unsigned int val;

	seed = a * (seed % q) - r * (seed / q);
	val = (seed / m) % (h - l) + l;

	return val;
}

#define clear_screen() CMD3('[', '2', 'J');

void position(char row, char col)
{
	int rowh, rowl, colh, coll;

	/* split into two digits, convert to ascii digits */
	rowh = row / 10;
	rowl = row % 10;
	rowh = '0' + rowh;
	rowl = '0' + rowl;
	colh = col / 10;
	coll = col % 10;
	colh = '0' + colh;
	coll = '0' + coll;

	CMD7('[', rowh, rowl, ';', colh, coll, 'H');
}

void worm_init(worm_t *worm)
{
	int i, j;

	worm->nsegs = NUM_WORM_SEGS;
	worm->head = worm->nsegs - 1;
	worm->ticks = irand(2, 6);
	worm->dir = RIGHT;
	for (i = 0; i < worm->nsegs; i++)
	{
		worm->row[i] = 1;
		worm->col[i] = i + 6;
	}
}

void worm_draw(worm_t *worm)
{
	int w;

	for (w = 0; w < worm->nsegs; w++)
	{
		position(worm->row[w], worm->col[w]);
		putch('*');
	}
}

void worm_erase(worm_t *worm)
{
	int w;

	for (w = 0; w < worm->nsegs; w++)
	{
		position(worm->row[w], worm->col[w]);
		putch(' ');
	}
}

void worm_redirect(worm_t *worm)
{
	int head = worm->head;

	worm->ticks--;

	/* if we hit a wall, immediately change direction */
	if (worm->row[head] == 0 && worm->dir == UP)
		worm->ticks = 0;
	if (worm->row[head] == MAX_ROWS && worm->dir == DOWN)
		worm->ticks = 0;
	if (worm->col[head] == 0 && worm->dir == LEFT)
		worm->ticks = 0;
	if (worm->col[head] == MAX_COLS && worm->dir == RIGHT)
		worm->ticks = 0;

	if (worm->ticks <= 0)
	{
		/* choose a new direction */

		int newdir;

		while (1)
		{
			newdir = irand(0, 4);

			/* bounce off walls */
			if (worm->row[head] == 0 && newdir == UP)
				continue;
			if (worm->row[head] == MAX_ROWS && newdir == DOWN)
				continue;
			if (worm->col[head] == 0 && newdir == LEFT)
				continue;
			if (worm->col[head] == MAX_COLS && newdir == RIGHT)
				continue;

			/* do not go in reverse */
			if (worm->dir == UP && newdir == DOWN)
				continue;
			if (worm->dir == DOWN && newdir == UP)
				continue;
			if (worm->dir == LEFT && newdir == RIGHT)
				continue;
			if (worm->dir == RIGHT && newdir == LEFT)
				continue;

			break;
		}

		worm->dir = newdir;
		worm->ticks = irand(2, 6);
	}
}

void worm_advance(worm_t *worm)
{
	int tail;

	/* clear tail */
	tail = (worm->head + 1) % worm->nsegs;

	worm_erase(worm);
	worm_redirect(worm);

	/* advance head (tail position is the new head position) */
	switch (worm->dir)
	{
	case UP:
		worm->row[tail] = worm->row[worm->head] - 1;
		worm->col[tail] = worm->col[worm->head];
		break;
	case DOWN:
		worm->row[tail] = worm->row[worm->head] + 1;
		worm->col[tail] = worm->col[worm->head];
		break;
	case LEFT:
		worm->row[tail] = worm->row[worm->head];
		worm->col[tail] = worm->col[worm->head] - 1;
		break;
	case RIGHT:
		worm->row[tail] = worm->row[worm->head];
		worm->col[tail] = worm->col[worm->head] + 1;
		break;
	default:
		break;
	}

	worm->head = tail;
	worm_draw(worm);
}

int SNEK(int argc, char *argv[])
{
	int i, j, k;
	worm_t worm_data, *worm = &worm_data;

	irand_init();

	clear_screen();

	worm_init(worm);
	worm_draw(worm);

	while (1)
	{
		/* spin */
		for (j = 0; j < 100000; j++)
			;
		/* next move */
		worm_advance(worm);
	}
}
