// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.runtime.cldc.debug;

import cc.squirreljme.jvm.Assembly;
import cc.squirreljme.jvm.SystemCallIndex;
import cc.squirreljme.jvm.config.ConfigRomKey;
import cc.squirreljme.jvm.config.LineEndingType;
import todo.OOPS;
import todo.TODO;

/**
 * This contains static method forwarders.
 *
 * @since 2020/03/21
 */
public final class Debugging
{
	/** Only bytes up to this value are permitted in the output. */
	public static final int _BYTE_LIMIT =
		0x7E;
	
	/** The descriptor used for standard error. */
	private static int _pipe =
		Integer.MIN_VALUE;
	
	/** Line ending type. */
	private static int _line =
		Integer.MIN_VALUE;
	
	/** Used to prevent loops. */
	@SuppressWarnings("StaticVariableMayNotBeInitialized")
	private static volatile boolean _noLoop;
	
	/**
	 * Not used.
	 *
	 * @since 2020/03/21
	 */
	private Debugging()
	{
	}
	
	/**
	 * Emits a debugging note.
	 *
	 * @param __fmt The format.
	 * @since 2020/05/13
	 */
	public static void debugNote(String __fmt)
	{
		Debugging.__format('D', 'B', __fmt, (Object[])null);
	}
	
	/**
	 * Emits a debugging note.
	 *
	 * @param __fmt The format.
	 * @param __args The arguments to the string.
	 * @since 2020/03/27
	 */
	public static void debugNote(String __fmt, Object... __args)
	{
		Debugging.__format('D', 'B', __fmt, __args);
	}
	
	/**
	 * Emits an oops error.
	 *
	 * @param __args Argument to the error.
	 * @return The generated error.
	 * @since 2020/03/22
	 */
	@SuppressWarnings("deprecation")
	public static Error oops(Object... __args)
	{
		return new OOPS();
	}
	
	/**
	 * Emits a To-Do error.
	 *
	 * @return The generated error.
	 * @since 2020/03/21
	 */
	@SuppressWarnings("deprecation")
	public static Error todo()
	{
		return new TODO();
	}
	
	/**
	 * Emits a To-Do error.
	 *
	 * @param __args Arguments to the error.
	 * @return The generated error.
	 * @since 2020/03/21
	 */
	@SuppressWarnings("deprecation")
	public static Error todo(Object... __args)
	{
		return new TODO();
	}
	
	/**
	 * Emits a To-Do note.
	 *
	 * @param __fmt Format string.
	 * @since 2020/05/13
	 */
	public static void todoNote(String __fmt)
	{
		Debugging.__format('T', 'D', __fmt, (Object[])null);
	}
	
	/**
	 * Emits a To-Do note.
	 *
	 * @param __fmt Format string.
	 * @param __args Arguments.
	 * @since 2020/03/31
	 */
	public static void todoNote(String __fmt, Object... __args)
	{
		Debugging.__format('T', 'D', __fmt, __args);
	}
	
	/**
	 * Returns a TODO for an object.
	 *
	 * @param <T> The type.
	 * @param __args The calling arguments.
	 * @return Never returns.
	 * @since 2020/04/09
	 */
	public static <T> T todoObject(Object... __args)
	{
		throw Debugging.todo(__args);
	}
	
	/**
	 * Prints formatted text to the console output.
	 *
	 * @param __cha First grouping character.
	 * @param __chb Second grouping character.
	 * @param __format Format string.
	 * @param __args Arguments.
	 * @since 2020/05/07
	 */
	@SuppressWarnings({"ConfusingArgumentToVarargsMethod",
		"StaticVariableUsedBeforeInitialization"})
	private static void __format(char __cha, char __chb, String __format,
		Object... __args)
	{
		// Print quickly and stop because this may infinite loop
		if (Debugging._noLoop)
		{
			Debugging.__print('X');
			return;
		}
		
		// Print otherwise
		try
		{
			// Do not re-enter this loop
			Debugging._noLoop = true;
			
			// Print header marker
			Debugging.__print(__cha, __chb);
			Debugging.__print(':', ' ');
			
			// The specifier to print along with the field index
			boolean specifier = false,
				hasArgIndex = false,
				firstChar = false;
			int argIndex = 0,
				baseArg = 0,
				width = -1,
				argCount = (__args == null ? 0 : __args.length);
			
			// Print down the format
			for (int i = 0, n = __format.length(); i < n; i++)
			{
				char c = __format.charAt(i);
				
				// Printing a specifier
				if (specifier)
				{
					// Ignore flags
					if (c == '-' || c == '#' || c == '+' ||
						c == ' ' || c == ',' || c == '(' ||
						(firstChar && c == '0'))
						;
					
					// Ignore precision
					else if (c == '.')
						;
					
					// Could be width or argument index position
					else if ((c >= '1' && c <= '9') ||
						(!firstChar && c == '0'))
					{
						if (hasArgIndex)
						{
							if (width < 0)
								width = 0;
							width = (width * 10) + (c - '0');
						}
						else
							argIndex = (argIndex * 10) + (c - '0');
					}
					
					// Is argument index
					else if (!hasArgIndex && c == '$')
						hasArgIndex = true;
					
					// Percent or newline
					else if (c == '%' || c == 'n')
					{
						if (c == '%')
							Debugging.__print('%');
						else
							Debugging.__printLine();
						
						// Stop
						specifier = false;
					}
					
					// A type to be printed, probably
					else
					{
						// If no argument index was flagged, then this is
						// likely the width
						if (!hasArgIndex && argIndex > 0)
							width = argIndex;
						
						// Select argument to print
						int choice = (hasArgIndex ? argIndex : baseArg++);
						Object value = (choice < 0 || choice >= argCount ?
							null : __args[choice]);
						
						// Print its value
						if (value == null)
						{
							Debugging.__print('n', 'u');
							Debugging.__print('l', 'l');
						}
						
						// Assume a string
						else
						{
							String string = value.toString();
							
							// Print left padding?
							int strLen = string.length(),
								pad = width - strLen;
							while ((pad--) > 0)
								Debugging.__print(' ');
							
							// Print actual string
							for (int j = 0; j < strLen; j++)
								Debugging.__print(string.charAt(j));
						}
						
						// Stop
						specifier = false;
					}
					
					// No longer will be the first character
					if (firstChar)
						firstChar = false;
				}
				
				// Format specifier?
				else if (c == '%')
				{
					specifier = true;
					firstChar = true;
					argIndex = 0;
				}
				
				// Plain character?
				else
					Debugging.__print(c);
			}
			
			// End of line
			Debugging.__printLine();
		}
		
		// Hopefully this does not happen but just in case, we want to catch
		// any exceptions that are tossed while printing format strings
		catch (Throwable t)
		{
			// Indicate this has occurred
			Debugging.__print('X');
			Debugging.__print('X');
			
			// End of line
			Debugging.__printLine();
		}
		
		// Clear loop prevention flag
		finally
		{
			Debugging._noLoop = false;
		}
	}
	
	/**
	 * Prints the given character.
	 *
	 * @param __c The character to print.
	 * @since 2020/05/07
	 */
	@SuppressWarnings({"ConfusingArgumentToVarargsMethod",
		"SameParameterValue"})
	private static void __print(char __c)
	{
		Debugging.__print(__c, '\0');
	}
	
	/**
	 * Prints the given characters.
	 *
	 * @param __c The character to print.
	 * @param __d Second character to print.
	 * @since 2020/05/07
	 */
	@SuppressWarnings("FeatureEnvy")
	private static void __print(char __c, char __d)
	{
		// If we do not know the pipe for standard error, get it
		int pipe = Debugging._pipe;
		if (pipe == Integer.MIN_VALUE)
			Debugging._pipe = (pipe = Assembly.sysCallPV(
				SystemCallIndex.PD_OF_STDERR));
				
		// Print first character, snip to ASCII
		Assembly.sysCallPV(SystemCallIndex.PD_WRITE_BYTE, pipe,
			(__c > Debugging._BYTE_LIMIT ? '?' : __c));
		
		// Print other characters in bulk, snip to ASCII
		if (__d != 0)
			Assembly.sysCallPV(SystemCallIndex.PD_WRITE_BYTE, pipe,
				(__d > Debugging._BYTE_LIMIT ? '?' : __d));
	}
	
	/**
	 * Prints end of line.
	 *
	 * @since 2020/05/07
	 */
	@SuppressWarnings("ConfusingArgumentToVarargsMethod")
	private static void __printLine()
	{
		// Get the line type used
		int line = Debugging._line;
		if (line == Integer.MIN_VALUE)
			Debugging._line = (line = Assembly.sysCallV(
				SystemCallIndex.CONFIG_GET_VALUE, ConfigRomKey.LINE_ENDING));
		
		// Print it depending on what is desired
		switch (line)
		{
			case LineEndingType.CR:
				Debugging.__print('\r', '\0');
				break;
				
			case LineEndingType.LF:
				Debugging.__print('\n', '\0');
				break;
				
			case LineEndingType.CRLF:
			default:
				Debugging.__print('\r', '\n');
				break;
		}
	}
}