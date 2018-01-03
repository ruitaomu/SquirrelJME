// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package net.multiphasicapps.squirreljme.runtime.javase;

import java.io.InputStream;
import java.io.OutputStream;
import net.multiphasicapps.squirreljme.kernel.client.ClientCaller;

/**
 * This is the Java implementation of the client caller.
 *
 * @since 2017/12/31
 */
public class JavaClientCaller
	extends ClientCaller
{
	/**
	 * Initializes the caller.
	 *
	 * @param __in The input stream.
	 * @param __out The output stream.
	 * @since 2017/12/31
	 */
	public JavaClientCaller(InputStream __in, OutputStream __out)
	{
		super(__in, __out);
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2017/12/31
	 */
	@Override
	public void setDaemonThread(Thread __t)
		throws IllegalThreadStateException, NullPointerException
	{
		if (__t == null)
			throw new NullPointerException("NARG");
		
		__t.setDaemon(true);
	}
}

