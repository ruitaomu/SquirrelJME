// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package net.multiphasicapps.squirreljme.jit;

/**
 * This contains the entire state of the program. The program consists of
 * multiple {@link BasicBlockZone}. Each basic block as an entry state and
 * encompasses a set of instructions. Control flows from one basic block to
 * another depending on the output and input alias state. The program is parsed
 * in a queue order generating virtual instructions as needed for each region
 * of basic blocks for differing aliasing types.
 *
 * @since 2017/05/13
 */
public class ProgramState
{
	/** This contains the basic block zones, sorted at zone start address. */
	private final BasicBlockZone[] _zones;
	
	/**
	 * Initializes the program state.
	 *
	 * @since 2017/05/14
	 */
	ProgramState()
	{
		throw new todo.TODO();
	}
}

