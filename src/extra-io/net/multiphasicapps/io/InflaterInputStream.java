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

import java.io.InputStream;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import net.multiphasicapps.collections.HuffmanTree;

/**
 * This input stream reads deflated input (using the deflate algorithm) and
 * decompresses it to provide the original data.
 *
 * This follows RFC 1951.
 *
 * @since 2016/03/09
 */
public class InflaterInputStream
	extends InputStream
{
	/** The size of the sliding window. */
	public static final int SLIDING_WINDOW_SIZE =
		32768;
	
	/** No compression. */
	protected static final int TYPE_NO_COMPRESSION =
		0b00;
	
	/** Fixed huffman table compression. */
	protected static final int TYPE_FIXED_HUFFMAN =
		0b01;
	
	/** Dynamic huffman table compression. */
	protected static final int TYPE_DYNAMIC_HUFFMAN =
		0b10;
	
	/** An error. */
	protected static final int TYPE_ERROR =
		0b11;
	
	/** Global static huffman tree cache. */
	private static volatile Reference<HuffmanTree<Integer>> _GLOBAL_TREE;
	
	/** Lock on the global tree. */
	private static final Object _GLOBAL_TREE_LOCK =
		new Object();
	
	/** Lock. */
	protected final Object lock =
		new Object();
	
	/** The wrapped bit stream. */
	protected final BitInputStream in;
	
	/** The sliding window where historical bytes may be referenced. */
	protected final SlidingByteWindow slidingwindow =
		new SlidingByteWindow(SLIDING_WINDOW_SIZE);
	
	/** Finished reading? */
	private volatile boolean _finished;
	
	/**
	 * This initializes the input stream which is used to inflate deflated
	 * data.
	 *
	 * @param __w The input deflated stream.
	 * @throws NullPointerException On null arguments.
	 * @since 2016/03/09
	 */
	public InflaterInputStream(InputStream __w)
		throws NullPointerException
	{
		// Check
		if (__w == null)
			throw new NullPointerException();
		
		// Set
		in = new BitInputStream(__w, true);
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/03/09
	 */
	@Override
	public void close()
		throws IOException
	{
		throw new Error("TODO");
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/03/09
	 */
	@Override
	public int read()
		throws IOException
	{
		// Lock
		synchronized (lock)
		{
			// There might not be enough data for an output read to occur.
			for (;;)
			{
				// If finished, then stop
				if (_finished)
					return -1;
			
				// Is this the final block?
				boolean isfinal = in.read();
			
				// Mark finished?
				_finished |= isfinal;
			
				// Read the compression type
				int type = (int)in.readBits(2);
				
				// DEBUG
				System.err.printf("DEBUG -- Finl: %s%n", isfinal);
				System.err.printf("DEBUG -- Type: %d%n", type);
			
				// No compression
				if (type == TYPE_NO_COMPRESSION)
					throw new Error("TODO");
			
				// Huffman compressed
				else if (type == TYPE_FIXED_HUFFMAN ||
					type == TYPE_DYNAMIC_HUFFMAN)
				{
					// The tree to use for the data
					HuffmanTree<Integer> ht;
					
					// Load in dynamic huffman table?
					if (type == TYPE_DYNAMIC_HUFFMAN)
						throw new Error("TODO");
				
					// Use the fixed one
					else
						ht = __fixedTree();
					
					throw new Error("TODO");
				}
			
				// Unknown or error
				else
					throw new InflaterException.HeaderErrorTypeException();
			}
		}
	}
	
	/**
	 * This returns the potentially cached fixed huffman tree which is used
	 * as input for dynamic huffman trees and the fixed huffman tree for
	 * potentially small data sources.
	 *
	 * @since 2016/03/10
	 */
	private static final HuffmanTree<Integer> __fixedTree()
	{
		// Lock on the global tree
		synchronized (_GLOBAL_TREE_LOCK)
		{
			// Get reference
			Reference<HuffmanTree<Integer>> ref = _GLOBAL_TREE;
			HuffmanTree<Integer> rv = null;
			
			// Cached already?
			if (ref != null)
				rv = ref.get();
			
			// Needs creation?
			if (rv == null)
			{
				// Create a new one
				rv = new HuffmanTree<Integer>();
				
				// 0 - 143, 8 bits
				for (int i = 0; i <= 143; i++)
					rv.setLiteralRepresentation(0b00110000 + i, 0b11111111, i);
				
				// 144 - 255, 9 bits
				for (int i = 144, j = 0; i <= 255; i++, j++)
					rv.setLiteralRepresentation(0b110010000 + j, 0b111111111,
						i);
				
				// 256 - 279, 7 bits
				for (int i = 256, j = 0; i <= 279; i++, j++)
					rv.setLiteralRepresentation(0b0000000 + j, 0b1111111, i);
				
				// 280 - 287, 8 bits
				for (int i = 280, j = 0; i <= 287; i++, j++)
					rv.setLiteralRepresentation(0b11000000 + j, 0b11111111, i);
				
				System.err.println(rv);
				// Cache it
				_GLOBAL_TREE = new WeakReference<>(rv);
			}
			
			// Return it
			return rv;
		}
	}
}

