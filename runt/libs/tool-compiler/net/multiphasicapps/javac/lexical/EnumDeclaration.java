// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package net.multiphasicapps.javac.lexical;

import net.multiphasicapps.javac.token.ExpandedToken;
import net.multiphasicapps.javac.token.ExpandingSource;
import net.multiphasicapps.javac.token.TokenType;

/**
 * This represents the declaration of an enumeration.
 *
 * @since 2018/04/10
 */
public final class EnumDeclaration
	extends ClassDeclaration
{
	/**
	 * {@inheritDoc}
	 * @since 2018/04/15
	 */
	@Override
	public final boolean equals(Object __o)
	{
		throw new todo.TODO();
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2018/04/15
	 */
	@Override
	public final int hashCode()
	{
		throw new todo.TODO();
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2018/04/15
	 */
	@Override
	public final String toString()
	{
		throw new todo.TODO();
	}
	
	/**
	 * Parses an enumeration.
	 *
	 * @param __m The modifiers for the annotation.
	 * @param __t The input token source.
	 * @return The parsed type.
	 * @throws LexicalStructureException If the class is not valid.
	 * @throws NullPointerException On null arguments.
	 * @since 2018/04/15
	 */
	public static final EnumDeclaration parseEnum(Modifier[] __m,
		ExpandingSource __t)
		throws LexicalStructureException, NullPointerException
	{
		if (__m == null || __t == null)
			throw new NullPointerException("NARG");
		
		throw new todo.TODO();
	}
}

