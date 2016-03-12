// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) 2013-2016 Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) 2013-2016 Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU Affero General Public License v3+, or later.
// For more information see license.mkd.
// ---------------------------------------------------------------------------

package net.multiphasicapps.io;

import java.util.NoSuchElementException;

/**
 * This is a circular buffer which provides bytes for input and output as a
 * queue.
 *
 * If the queue reaches full capacity then it is increased in size.
 *
 * @since 2016/03/11
 */
public class CircularByteBuffer
	extends CircularGenericBuffer<byte[], Byte>
{
	/**
	 * Initializes a circular byte buffer.
	 *
	 * @since 2016/03/11
	 */
	public CircularByteBuffer()
	{
		super();
	}
	
	/**
	 * Initializes a circular byte buffer with the given lock object.
	 *
	 * @param __lock The lock to use, if {@code null} then one is initialized.
	 * @since 2016/03/11
	 */
	public CircularByteBuffer(Object __lock)
	{
		super(__lock);
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/03/11
	 */
	@Override
	protected int arrayLength(byte[] __arr)
	{
		return __arr.length;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/03/11
	 */
	@Override
	protected Byte arrayRead(byte[] __arr, int __dx)
	{
		return __arr[__dx];
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/03/11
	 */
	@Override
	protected CircularGenericBuffer arrayWrite(byte[] __arr, int __dx,
		Byte __v)
	{
		__arr[__dx] = __v;
		return this;
	}
}

