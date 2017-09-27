// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package net.multiphasicapps.squirreljme.jit.cff;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * This represents a single class file.
 *
 * @since 2017/09/26
 */
public final class ClassFile
{
	/** The magic number of the class file. */
	private static final int _MAGIC_NUMBER =
		0xCAFEBABE;
	
	/**
	 * Initializes the class file.
	 *
	 * @since 2017/09/26
	 */
	ClassFile()
	{
		throw new todo.TODO();
	}
	
	/**
	 * This parses the input stream as a class file and returns the
	 * representation of that class file.
	 *
	 * @param __is The input stream to source classes from.
	 * @return The decoded class file.
	 * @throws InvalidClassFormatException If the class file is not formatted
	 * correctly.
	 * @throws IOException On read errors.
	 * @throws NullPointerException On null arguments.
	 * @since 2017/09/26
	 */
	public static ClassFile decode(InputStream __is)
		throws InvalidClassFormatException, IOException, NullPointerException
	{
		// Check
		if (__is == null)
			throw new NullPointerException("NARG");
		
		// {@squirreljme.error JI2u The magic number for the class is not
		// valid. (The read magic number; The expected magic number)}
		DataInputStream in = new DataInputStream(__is);
		int magic = in.readInt();
		if (magic != _MAGIC_NUMBER)
			throw new InvalidClassFormatException(String.format(
				"JI2u %08x %08x", magic, _MAGIC_NUMBER));
		
		// {@squirreljme.error JI08 The version number of the input class
		// file is not valid. (The version number)}
		int cver = in.readShort() | (in.readShort() << 16);
		ClassVersion version = ClassVersion.findVersion(cver);
		if (version == null)
			throw new InvalidClassFormatException(String.format("JI08 %d.%d",
				cver >>> 16, (cver & 0xFFFF)));
		
		// Decode the constant pool
		Pool pool = Pool.decode(in);
		
		throw new todo.TODO();
	}
}

