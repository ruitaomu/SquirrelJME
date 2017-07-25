// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Steven Gawroriski <xer@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package net.multiphasicapps.squirreljme.jit.java;

/**
 * This class is used to construct instances of {@link StackMapTable}, this
 * reads the raw data from the {@code StackMap} or {@code StackMapTable}
 * attribute.
 *
 * @since 2017/07/15
 */
public class StackMapTableBuilder
{
	/** The byte code for the method owning this, needed for {@code new}. */
	protected final ByteCode code;
	
	/** The constant pool, needed for type decoding. */
	protected final Pool pool;
	
	/**
	 * Initializes the stack map table builder.
	 *
	 * @param __f The flags used for the method.
	 * @param __t The descriptor for the method.
	 * @param __oc The class which contains the method to build a stack map
	 * for, this is needed for {@code this} references.
	 * @param __bc The byte code for the current method, required for handling
	 * initialization of new objects.
	 * @param __pool The constant pool.
	 * @since 2017/07/24 
	 */
	public StackMapTableBuilder(MethodFlags __f, MethodDescriptor __t,
		ClassName __oc, ByteCode __bc, Pool __pool)
		throws NullPointerException
	{
		// Check
		if (__f == null || __t == null || __oc == null || __bc == null ||
			__pool == null)
			throw new NullPointerException("NARG");
		
		// Set base
		this.code = __bc;
		this.pool = __pool;
		
		throw new todo.TODO();
	}
	
	/**
	 * Builds the stack map table representation.
	 *
	 * @return The resulting stack map table.
	 * @since 2017/07/16
	 */
	public StackMapTable build()
	{
		throw new todo.TODO();
	}
}

