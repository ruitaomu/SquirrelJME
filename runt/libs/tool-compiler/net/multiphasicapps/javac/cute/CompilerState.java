// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package net.multiphasicapps.javac.cute;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import net.multiphasicapps.classfile.ClassName;
import net.multiphasicapps.collections.SortedTreeMap;
import net.multiphasicapps.javac.CompilerInput;
import net.multiphasicapps.javac.CompilerInputLocation;
import net.multiphasicapps.javac.CompilerPathSet;
import net.multiphasicapps.javac.CompilerInput;
import net.multiphasicapps.javac.NoSuchInputException;

/**
 * This contains the current state of the compiler.
 *
 * @since 2018/03/06
 */
public final class CompilerState
{
	/** Logging. */
	protected final PrintStream log;
	
	/** The class and source paths. */
	protected final Map<CompilerInputLocation, List<CompilerPathSet>> paths;
	
	/** Class nodes which have been loaded for structuring. */
	final Map<ClassName, ClassNode> _nodes =
		new SortedTreeMap<>();
	
	/** Class nodes which need to be compiled. */
	final Deque<SourcedClassNode> _tocompile =
		new ArrayDeque<>();
	
	/**
	 * Initializes the compiler state.
	 *
	 * @param __log The logging output.
	 * @param __ps Path sets for the compiler, used to build the class
	 * structure before compilation is performed.
	 * @throws NullPointerException On null arguments.
	 * @since 2018/03/06
	 */
	public CompilerState(PrintStream __log,
		Map<CompilerInputLocation, List<CompilerPathSet>> __ps)
		throws NullPointerException
	{
		if (__log == null || __ps == null)
			throw new NullPointerException("NARG");
		
		this.log = __log;
		this.paths = __ps;
	}
	
	/**
	 * Returns the class node for the given class.
	 *
	 * @param __cn The class node to get.
	 * @return The class node
	 * @throws MissingClassNodeException If the class node does not exist.
	 * @throws NullPointerException On null arguments.
	 * @since 2018/03/06
	 */
	public final ClassNode classNode(ClassName __cn)
		throws MissingClassNodeException, NullPointerException
	{
		if (__cn == null)
			throw new NullPointerException("NARG");
		
		// If a node has already been loaded then use it
		Map<ClassName, ClassNode> nodes = this._nodes;
		ClassNode rv = nodes.get(__cn);
		if (rv != null)
			return rv;
		
		// Primitive types and arrays are completely special and are
		// generated by the class code
		if (__cn.isPrimitive() || __cn.isArray())
			throw new todo.TODO();
		
		// Search the class path first before looking for sources to compile
		Map<CompilerInputLocation, List<CompilerPathSet>> paths = this.paths;
		String classfilename = __cn.binaryName() + ".class";
		for (CompilerPathSet ps : paths.get(CompilerInputLocation.CLASS))
			try
			{
				CompilerInput ci = ps.input(classfilename);
				
				throw new todo.TODO();
			}
			catch (NoSuchInputException e)
			{
			}
		
		// It is possible for inner classes to be referred to before the
		// outer class has been seen, so look in the base class instead and see
		// if that exists
		String sourcename = __cn.binaryName().toString(),
			baselookname = sourcename;
		int ls = sourcename.lastIndexOf('/'),
			ld = sourcename.lastIndexOf('$');
		if (ld >= 0 && ld > ls)
			baselookname = sourcename.substring(0, ld);
		
		// Go through source files instead and try to find a node to compile
		boolean badhit = false;
		for (int z = 0; z < 2; z++)
		{
			// The loop goes through things twice because someone could be
			// very evil and name a class a bunch of dollar signs despite not
			// being an actual inner class. To to prevent these evil classes
			// from being missed, go back around and try to find and load them
			// as their normal name
			String baselookfilename = (z == 0 ? baselookname + ".java" :
				sourcename + ".java");
			for (CompilerPathSet ps : paths.get(CompilerInputLocation.SOURCE))
				try
				{
					// Always use the base name because the requested thing
					// could be an inner class and the outer class will need to
					// be loaded before it can properly be used
					CompilerInput ci = ps.input(baselookfilename);
				
					// Load in node, it is automatically added by the
					// constructor
					SourcedClassNode.__loadNodes(ci, this);
				
					// If the source file pertains to the given name then it
					// will be returned here, otherwise it will fail for
					// cases where evil dollar signs are used or if a class
					// is in the wrong file.
					rv = nodes.get(__cn);
					if (rv != null)
						return rv;
					
					// This is used to detect where the wrong class is
					// declared in the wrong file.
					badhit = true;
				}
				catch (NoSuchInputException e)
				{
				}
		}
		
		// {@squirreljme.error AQ0r Could not locate the node for the given
		// class name, the source file where the class should have been located
		// did not actually contain the class. (The class name)}
		if (badhit)
			throw new MissingClassNodeException(
				String.format("AQ0r %s", __cn));
		
		// {@squirreljme.error AQ0p Could not locate the node for the given
		// class name. (The class name)}
		throw new MissingClassNodeException(String.format("AQ0p %s", __cn));
	}
	
	/**
	 * Returns the next class node to compile.
	 *
	 * @return The next node to compile or {@code null} if there are no
	 * nodes to currently compile.
	 * @since 2018/03/06
	 */
	public final SourcedClassNode nextCompile()
	{
		Deque<SourcedClassNode> tocompile = this._tocompile;
		return tocompile.pollFirst();
	}
	
	/**
	 * Logs the specified message.
	 *
	 * @param __t The type of message to display.
	 * @parma __i The current input file, may be {@code null}.
	 * @param __m The formatted message to show.
	 * @param __args The arguments to the formatted message.
	 * @throws NullPointerException On null arguments.
	 * @since 2018/03/06
	 */
	public final void message(MessageType __t, CompilerInput __i, String __m,
		Object... __args)
		throws NullPointerException
	{
		if (__t == null || __m == null)
			throw new NullPointerException("NARG");
		
		// Forward
		this.message(__t, __i, -1, -1, __m, __args);
	}
	
	/**
	 * Logs the specified message.
	 *
	 * @param __t The type of message to display.
	 * @parma __i The current input file, may be {@code null}.
	 * @param __row The current row, negative values are not valid.
	 * @param __col The current column, negative values are not valid.
	 * @param __m The formatted message to show.
	 * @param __args The arguments to the formatted message.
	 * @throws NullPointerException On null arguments.
	 * @since 2018/03/06
	 */
	public final void message(MessageType __t, CompilerInput __i, int __row,
		int __col, String __m, Object... __args)
		throws NullPointerException
	{
		if (__t == null || __m == null)
			throw new NullPointerException("NARG");
		
		// Force to exist
		if (__args == null)
			__args = new String[0];
		
		// Print specially formatted messages
		PrintStream log = this.log;
		
		// File
		if (__i != null)
			log.print(__i.name());
		else
			log.print("<unknown>");
		
		// Printing row?
		if (__row >= 0)
		{
			log.print(':');
			log.print(__row);
			
			// Add column also?
			if (__col >= 0)
			{
				log.print(',');
				log.print(__col);
			}
		}
		
		// Print spacer and the message type
		log.print(": [");
		log.print(__t);
		log.print("] ");
		
		// Print the message itself
		log.println(String.format(__m, __args));
	}
}

