// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) 2013-2016 Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) 2013-2016 Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU Affero General Public License v3+, or later.
// For more information see license.mkd.
// ---------------------------------------------------------------------------

package net.multiphasicapps.interpreter.jvmops;

import java.io.IOException;
import net.multiphasicapps.interpreter.JVMByteOpHandler;
import net.multiphasicapps.interpreter.JVMClassFormatError;
import net.multiphasicapps.interpreter.JVMCodeParser;
import net.multiphasicapps.interpreter.JVMProgramState;
import net.multiphasicapps.interpreter.JVMVariableType;

/**
 * Handles opcodes 32 to 47.
 *
 * @since 2016/03/23
 */
public class JVMOpHandler32To47
	implements JVMByteOpHandler
{
	/**
	 * {@inheritDoc}
	 * @since 2016/03/23
	 */
	@Override
	public void handle(int __op, JVMCodeParser.HandlerBridge __br)
		throws IOException
	{
		// Depends on the operation
		switch (__op)
		{
				// lload_2 to lload_3
			case 32:
			case 33:
				__GenericLocalLoad__.__load(JVMVariableType.LONG,
					2 + (__op - 32), __br);
				break;
			
				// fload_0 to fload_3
			case 34:
			case 35:
			case 36:
			case 37:
				__GenericLocalLoad__.__load(JVMVariableType.FLOAT,
					__op - 34, __br);
				break;
			
				// dload_0 to dload_3
			case 38:
			case 39:
			case 40:
			case 41:
				__GenericLocalLoad__.__load(JVMVariableType.DOUBLE,
					__op - 38, __br);
				break;
			
				// aload_0 to aload_3
			case 42:
			case 43:
			case 44:
			case 45:
				__GenericLocalLoad__.__load(JVMVariableType.OBJECT,
					__op - 42, __br);
				break;
			
			default:
				throw new JVMClassFormatError(
					String.format("IN1h %d", __op));
		}
	}
}

