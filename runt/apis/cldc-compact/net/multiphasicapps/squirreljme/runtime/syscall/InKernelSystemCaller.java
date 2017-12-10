// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package net.multiphasicapps.squirreljme.runtime.syscall;

import net.multiphasicapps.squirreljme.runtime.kernel.Kernel;
import net.multiphasicapps.squirreljme.runtime.kernel.KernelTask;

/**
 * This is a system caller which directly calls the kernel and uses the
 * provided task as the context for permissions and the source performing any
 * given action.
 *
 * This class must never expose the {@link Kernel} or {@link KernelTask}
 * instances.
 *
 * @since 2017/12/10
 */
public final class InKernelSystemCaller
	extends SystemCaller
{
	/** The kernel to call. */
	protected final Kernel kernel;
	
	/** The task to call the kernel as. */
	protected final KernelTask task;
	
	/**
	 * Initializes the in-kernel system caller.
	 *
	 * @param __k The kernel to call.
	 * @param __t The task of the process to call.
	 * @throws NullPointerException On null arguments.
	 * @since 2017/12/10
	 */
	public InKernelSystemCaller(Kernel __k, KernelTask __t)
		throws NullPointerException
	{
		if (__k == null || __t == null)
			throw new NullPointerException("NARG");
		
		this.kernel = __k;
		this.task = __t;
	}
}

