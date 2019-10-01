// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.jvm;

/**
 * This contains the index of system calls.
 *
 * @since 2019/05/23
 */
public interface SystemCallIndex
{
	/**
	 * Checks if the system call is supported.
	 *
	 * @param 1 The system call index to query.
	 * @return Zero if not supported, otherwise a non-zero value.
	 */
	public static final short QUERY_INDEX =
		0;
	
	/**
	 * Gets the last error code.
	 *
	 * This value that stores the error state is thread-local and it may be
	 * stored with a precision of at least 16-bits.
	 *
	 * If the system call index is not valid then it is assumed to be
	 * {@link #QUERY_INDEX}.
	 *
	 * @param 1 The system call index to query.
	 * @return The last error code, will be zero if the last command succeeded.
	 */
	public static final short ERROR_GET =
		1;
	
	/**
	 * Sets the last error code.
	 *
	 * This value that stores the error state is thread-local and it may be
	 * stored with a precision of at least 16-bits.
	 *
	 * If the system call index is not valid then it is assumed to be
	 * {@link #QUERY_INDEX}.
	 *
	 * @param 1 The system call index to query.
	 * @param 2 The value to set error register to.
	 * @return Zero on success
	 */
	public static final short ERROR_SET =
		2;
	
	/**
	 * Current wall clock milliseconds (low).
	 *
	 * @return The lower 32-bits of the time.
	 */
	public static final short TIME_LO_MILLI_WALL =
		3;
	
	/**
	 * Current wall clock milliseconds (high).
	 *
	 * @return The higher 32-bits of the time.
	 */
	public static final short TIME_HI_MILLI_WALL =
		4;
	
	/**
	 * Current monotonic clock nanoseconds (low).
	 *
	 * @return The lower 32-bits of the time.
	 */
	public static final short TIME_LO_NANO_MONO =
		5;
	
	/**
	 * Current monotonic clock nanoseconds (high).
	 *
	 * @return The higher 32-bits of the time.
	 */
	public static final short TIME_HI_NANO_MONO =
		6;
	
	/**
	 * VM Information: Free memory in bytes.
	 *
	 * @return The free memory amount in bytes.
	 */
	public static final short VMI_MEM_FREE =
		7;
	
	/**
	 * VM Information: Used memory in bytes.
	 *
	 * @return The used memory amount in bytes.
	 */
	public static final short VMI_MEM_USED =
		8;
	
	/**
	 * VM Information: Max memory in bytes.
	 *
	 * @return The max memory amount in bytes.
	 */
	public static final short VMI_MEM_MAX =
		9;
	
	/**
	 * Suggests that the garbage collector should run, note that this may be
	 * a deferred operation and might not be immediate.
	 *
	 * @return Generally zero although any other value could be returned.
	 */
	public static final short GARBAGE_COLLECT =
		10;
	
	/**
	 * Exits the VM with the given exit code.
	 *
	 * @param 1 The exit code to exit the process with.
	 * @return This generally does not return, if it does then the error code
	 * will likely specify why this failed.
	 */
	public static final short EXIT =
		11;
	
	/**
	 * The API Level of the VM, this has been deprecated since the current
	 * SquirrelJME API specified in these system calls better handles various
	 * features.
	 *
	 * @return The API level of the virtual machine.
	 */
	@Deprecated
	public static final short API_LEVEL =
		12;
	
	/**
	 * The pipe descriptor for stdin.
	 *
	 * @return The pipe descriptor for standard input.
	 */
	public static final short PD_OF_STDIN =
		13;
	
	/**
	 * The pipe descriptor for stdout.
	 *
	 * @return The pipe descriptor for standard output.
	 */
	public static final short PD_OF_STDOUT =
		14;
	
	/**
	 * The pipe descriptor for stderr.
	 *
	 * @return The pipe descriptor for standard error.
	 */
	public static final short PD_OF_STDERR =
		15;
	
	/**
	 * Pipe descriptor: Write single byte.
	 *
	 * @param 1 The pipe descriptor.
	 * @param 2 The value of the byte to write, only the lowest 8-bits is used.
	 * @return The number of bytes written to the output, if this returns
	 * a value lower than zero then it indicates an error.
	 */
	public static final short PD_WRITE_BYTE =
		16;
	
	/**
	 * Bulk sets the memory inside of a region, this follows the same pattern
	 * as C's {@code memset()} operation.
	 *
	 * @param 1 The address to set.
	 * @param 2 The value to set the region with.
	 * @param 3 The number of bytes to set.
	 * @return The number of bytes actually written, if this is zero then
	 * it is likely the system call is not supported.
	 */
	public static final short MEM_SET =
		17;
	
	/**
	 * Bulk sets the memory inside of a region writing full integer values at
	 * a time which is generally faster, this follows the same pattern as C's
	 * {@code memset()} operation.
	 *
	 * @param 1 The address to set.
	 * @param 2 The value to set the region with.
	 * @param 3 The number of bytes to set, the lower 2-bits ({@code 0x3}) will
	 * be masked off so the length is always a multiple of four.
	 * @return The number of bytes actually written, if this is zero then
	 * it is likely the system call is not supported.
	 */
	public static final short MEM_SET_INT =
		18;
	
	/**
	 * Get the height of the call stack.
	 *
	 * @return The height of the call stack.
	 */
	public static final short CALL_STACK_HEIGHT =
		19;
	
	/** Gets the specified call stack item. */
	public static final short CALL_STACK_ITEM =
		20;
	
	/** Returns the string of the given pointer. */
	public static final short LOAD_STRING =
		21;
	
	/** Fatal ToDo hit. */
	public static final short FATAL_TODO =
		22;
	
	/** Supervisor booted okay. */
	public static final short SUPERVISOR_BOOT_OKAY =
		23;
	
	/** Get property of the framebuffer. */
	public static final short FRAMEBUFFER_PROPERTY =
		24;
	
	/** The current byte order. */
	public static final short BYTE_ORDER_LITTLE =
		25;
	
	/** Returns the pointer to the option JAR data. */
	public static final short OPTION_JAR_DATA =
		26;
	
	/** Returns the size of the option JAR data. */
	public static final short OPTION_JAR_SIZE =
		27;
	
	/** The number of system calls that are defined in this run-time. */
	public static final short NUM_SYSCALLS =
		26;
}

