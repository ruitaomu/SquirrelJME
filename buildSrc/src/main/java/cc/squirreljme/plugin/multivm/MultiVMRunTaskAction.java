// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.plugin.multivm;

import org.gradle.api.Action;
import org.gradle.api.Task;

/**
 * Runs the program within the virtual machine.
 *
 * @since 2020/08/07
 */
public class MultiVMRunTaskAction
	implements Action<Task>
{
	/** The source set used. */
	protected final String sourceSet;
	
	/** The virtual machine type. */
	protected final VirtualMachineSpecifier vmType;
	
	/**
	 * Initializes the task action.
	 * 
	 * @param __sourceSet The source set to use.
	 * @param __vmType The virtual machine type.
	 * @throws NullPointerException On null arguments.
	 * @since 2020/08/16
	 */
	public MultiVMRunTaskAction(String __sourceSet,
		VirtualMachineSpecifier __vmType)
		throws NullPointerException
	{
		if (__sourceSet == null || __vmType == null)
			throw new NullPointerException("NARG");
		
		this.sourceSet = __sourceSet;
		this.vmType = __vmType;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2020/08/07
	 */
	@Override
	public void execute(Task __task)
	{
		throw new Error("TODO");
	}
}
