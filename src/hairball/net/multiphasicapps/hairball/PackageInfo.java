// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) 2013-2016 Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) 2013-2016 Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU Affero General Public License v3+, or later.
// For more information see license.mkd.
// ---------------------------------------------------------------------------

package net.multiphasicapps.hairball;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;
import net.multiphasicapps.collections.MissingCollections;

/**
 * This contains package information.
 *
 * @since 2016/02/28
 */
public class PackageInfo
{
	/** The package root. */
	protected final Path root;
	
	/** Is this package valid? */
	protected final boolean valid;
	
	/** The name of this package. */
	protected final String name;
	
	/** Dependencies of this package. */
	protected final Set<String> depends;
	
	/**
	 * The path to the package root.
	 *
	 * @param __p Package root.
	 * @throws NullPointerException On null arguments.
	 * @since 2016/02/28
	 */
	public PackageInfo(Path __p)
		throws NullPointerException
	{
		// Check
		if (__p == null)
			throw new NullPointerException();
		
		// Set
		root = __p;
		
		// Guessed manifest location
		Path mfp = root.resolve("META-INF").resolve("MANIFEST.MF");
		
		// Is hairball?
		boolean ishb = false;
		
		// Dependencies?
		Set<String> deps = new HashSet<>();
		
		// Read the manifest
		boolean iv = false;
		try (FileChannel fc = FileChannel.open(mfp, StandardOpenOption.READ);
			BufferedReader br = new BufferedReader(new InputStreamReader(
				Channels.newInputStream(fc), "utf-8")))
		{
			// Read in all manifest source lines
			Deque<String> lines = new LinkedList<>();
			for (;;)
			{
				// Read
				String ln = br.readLine();
				
				// EOF?
				if (ln == null)
					break;
				
				// If the line is blank, stop reading the manifest
				if (ln.length() <= 0)
					break;
				
				// Lowercase it to make it easier to parse
				ln = __asciiLowerCase(ln);
				
				// If it starts with a space, then it is a continuation of the
				// last line and just gets pasted on it
				if (ln.startsWith(" "))
				{
					// The manifest could be badly formed
					try
					{
						// Get the last line that this is to be appended to
						String top = lines.getLast();
						
						// Remove the last line
						lines.removeLast();
						
						// Append it
						lines.offerLast(top + ln.substring(1));
					}
					
					// Bad manifest format
					catch (NoSuchElementException nsee)
					{
						throw new IOException("Malformed manifest file.",
							nsee);
					}
				}
				
				// Otherwise it is not, just add it
				else
					lines.add(ln);
			}
			
			// Go through lines again
			for (String ln : lines)
			{
				// Find the first colon
				int col = ln.indexOf(':');
				
				// If not found, ignore
				if (col < 0)
					continue;
				
				// Split key and value
				String key = ln.substring(0, col).trim();
				String val = ln.substring(col + 1).trim();
				
				// Depends on the key
				switch (key)
				{
						// Is this a hairball package?
					case "hairball-package":
						// This just is indicative that it is one.
						break;
						
						// Dependency on another package
					case "hairball-depends":
						// Dependencies are split by spaces
						int sn = val.length();
						for (int q = 0; q <= sn; q++)
						{
							// Get character here
							char c = (q < sn ? val.charAt(q) : ' ');
							
							// Ignore starting spaces
							if (c <= ' ')
								continue;
							
							// Otherwise read until the next space
							int start = q;
							for (; q < sn; q++)
							{
								// Get character
								c = val.charAt(q);
								
								// Is a space? Stop
								if (c <= ' ')
									break;
							}
							
							// Add dependency
							deps.add(val.substring(start, q));
						}
						break;
					
						// Unknown
					default:
						break;
				}
			}
			
			// Is valid?
			iv = ishb;
		}
		
		// Failed to read or some other error
		catch (IOException ioe)
		{
			// Only log it if this is not a package
			if (!(ioe instanceof __NotAPackageException__))
			{
				System.err.printf("Could not read package: %s%n",
					ioe.getCause());
				ioe.printStackTrace(System.err);
			}
		}
		
		// Set validity
		valid = iv;
		
		// Lock in dependencies
		depends = MissingCollections.<String>unmodifiableSet(deps);
		
		// Set name
		name = __asciiLowerCase(root.getFileName().toString());
	}
	
	/**
	 * Is this a valid package?
	 *
	 * @return {@code true} if it is.
	 * @since 2016/02/28
	 */
	public boolean isValid()
	{
		return valid;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/02/28
	 */
	@Override
	public String toString()
	{
		return name;
	}
	
	/**
	 * Performs an ASCII lowercase on the given string.
	 *
	 * @param __in Input string to lowercase.
	 * @return The lowercased string using only ASCII data.
	 * @since 2016/02/28
	 */
	private static final String __asciiLowerCase(String __in)
		throws NullPointerException
	{
		// Check
		if (__in == null)
			throw new NullPointerException();
		
		// Output
		StringBuilder sb = new StringBuilder();
		
		// Go through input string and lowercase
		int n = __in.length();
		for (int i = 0; i < n; i++)
		{
			char c = __in.charAt(i);
			
			// Lower it
			if (c >= 'A' && c <= 'Z')
				c = (char)('a' + (c - 'A'));
			
			sb.append(c);
		}
		
		// Build it
		return sb.toString();
	}
	
	/**
	 * This is thrown when a given path is not a package.
	 *
	 * @since 2016/02/28
	 */
	private static class __NotAPackageException__
		extends IOException
	{
	}
}

