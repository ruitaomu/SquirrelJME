// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.springcoat.vm;

import java.util.ArrayList;
import java.util.List;
import net.multiphasicapps.classfile.ByteCode;

/**
 * This class contains information about a thread within the virtual machine.
 *
 * @since 2018/09/01
 */
public final class SpringThread
{
	/** The thread ID. */
	protected final int id;
	
	/** The name of this thread. */
	protected final String name;
	
	/** The stack frames. */
	private final List<SpringThread.Frame> _frames =
		new ArrayList<>();
	
	/**
	 * Initializes the thread.
	 *
	 * @param __id The thread ID.
	 * @param __n The name of the thread.
	 * @throws NullPointerException On null arguments.
	 * @since 2018/09/01
	 */
	SpringThread(int __id, String __n)
		throws NullPointerException
	{
		if (__n == null)
			throw new NullPointerException("NARG");
		
		this.id = __id;
		this.name = __n;
	}
	
	/**
	 * Returns the current frame of execution or {@code null} if there is none.
	 *
	 * @return The current frame of execution or {@code null} if there is none.
	 * @since 2018/09/03
	 */
	public final SpringThread.Frame currentFrame()
	{
		List<SpringThread.Frame> frames = this._frames;
		synchronized (frames)
		{
			if (frames.isEmpty())
				return null;
			return frames.get(frames.size() - 1);
		}
	}
	
	/**
	 * Enters the specified method and sets up a stack frame for it.
	 *
	 * @param __m The method to enter.
	 * @return The used stack frame.
	 * @throws NullPointerException On null arguments.
	 * @throws SpringVirtualMachineException If the method is abstract.
	 * @since 2018/09/03
	 */
	public final SpringThread.Frame enterFrame(SpringMethod __m)
		throws NullPointerException, SpringVirtualMachineException
	{
		if (__m == null)
			throw new NullPointerException("NARG");
		
		// Debug
		todo.DEBUG.note("enterFrame(%s::%s)", __m.inClass(),
			__m.nameAndType());
		
		// {@squirreljme.error BK09 Cannot enter the frame for a method which
		// is abstract. (The class the method is in; The method name and type)}
		if (__m.isAbstract())
			throw new SpringVirtualMachineException(String.format("BK09 %s %s",
				__m.inClass(), __m.nameAndType()));
		
		// Create new frame
		Frame rv = new Frame(__m);
		
		// Lock on frames as a new one is added
		List<SpringThread.Frame> frames = this._frames;
		synchronized (frames)
		{
			frames.add(rv);
		}
		
		return rv;
	}
	
	/**
	 * Returns the name of the thread.
	 *
	 * @return The name of the thread.
	 * @since 2018/09/03
	 */
	public final String name()
	{
		return this.name;
	}
	
	/**
	 * Returns the number of frames that are available in this thread.
	 *
	 * @return The number of available frames.
	 * @since 2018/09/03
	 */
	public final int numFrames()
	{
		List<SpringThread.Frame> frames = this._frames;
		synchronized (frames)
		{
			return frames.size();
		}
	}
	
	/**
	 * This class represents the stack frame and is used to store local
	 * variables and other such things.
	 *
	 * @since 2018/09/03
	 */
	public static class Frame
	{
		/** The current method in this frame. */
		protected final SpringMethod method;
		
		/** The method code for execution. */
		protected final ByteCode code;
		
		/** Local variables. */
		private final Object[] _locals;
		
		/** The stack. */
		private final Object[] _stack;
		
		/** The top of the stack. */
		private volatile int _stacktop;
		
		/** The current program counter. */
		private volatile int _pc;
		
		/**
		 * Initializes the frame.
		 *
		 * @param __m The method used for the frame.
		 * @throws NullPointerException On null arguments.
		 * @since 2018/09/03
		 */
		private Frame(SpringMethod __m)
			throws NullPointerException
		{
			if (__m == null)
				throw new NullPointerException("NARG");
			
			this.method = __m;
			
			// We will need to initialize the local and stack data from the
			// information the byte code gives
			ByteCode code;
			this.code = (code = __m.byteCode());
			
			// Initialize variable storage
			Object[] locals;
			this._locals = (locals = new Object[code.maxLocals()]);
			this._stack = new Object[code.maxStack()];
			
			// Debug
			todo.DEBUG.note("Frame has %d locals, %d stack", locals.length,
				this._stack.length);
		}
		
		/**
		 * Returns the byte code to execute.
		 *
		 * @return The byte code to execute.
		 * @since 2018/09/03
		 */
		public final ByteCode byteCode()
		{
			return this.code;
		}
		
		/**
		 * Returns the instruction counter.
		 *
		 * @return The instruction counter.
		 * @since 2018/09/03
		 */
		public final int pc()
		{
			return this._pc;
		}
		
		/**
		 * Sets the program counter.
		 *
		 * @param __pc The program counter to set.
		 * @since 2018/09/03
		 */
		public final void setPc(int __pc)
		{
			this._pc = __pc;
		}
	}
}
