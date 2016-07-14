// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) 2013-2016 Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) 2013-2016 Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// For more information see license.mkd.
// ---------------------------------------------------------------------------

package net.multiphasicapps.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

/**
 * The class represents an individual test to be ran, it allows for argument
 * input along with output results for potential sub-test areas.
 *
 * @since 2016/07/12
 */
public final class IndividualTest
{
	/** The group name. */
	protected final TestGroupName group;
	
	/** The sub-test name. */
	protected final TestSubName name;
	
	/** Test results. */
	private final List<Result> _results =
		new ArrayList<>();
	
	/** Next test ID. */
	private volatile int _nextid;
	
	/**
	 * Initializes the individual test with the given sub-name.
	 *
	 * @param __g The test group.
	 * @param __n The sub-test name.
	 * @throws NullPointerException On null arguments.
	 * @since 2016/07/12
	 */
	IndividualTest(TestGroupName __g, TestSubName __n)
		throws NullPointerException
	{
		// Check
		if (__g == null || __n == null)
			throw new NullPointerException("NARG");
		
		// Set
		this.group = __g;
		this.name = __n;
	}
	
	/**
	 * Returns the group name of the test.
	 *
	 * @return The group name.
	 * @since 2016/07/12
	 */
	public final TestGroupName groupName()
	{
		return this.group;
	}
	
	/**
	 * Initializes a new test result.
	 *
	 * @param __n The name of the test.
	 * @return The result of a test which may be set.
	 * @throws NullPointerException On null arguments.
	 * @since 2016/07/12
	 */
	public final Result result(String __n)
		throws NullPointerException
	{
		return this.result(TestFragmentName.of(__n));
	}
	
	/**
	 * Initializes a new test result.
	 *
	 * @param __n The name of the test fragment.
	 * @return The result of a test which may be set.
	 * @throws NullPointerException On null arguments.
	 * @since 2016/07/12
	 */
	public final Result result(TestFragmentName __n)
		throws NullPointerException
	{
		// Check
		if (__n == null)
			throw new NullPointerException("NARG");
		
		// Lock
		List<Result> results = this._results;
		synchronized (results)
		{
			Result rv;
			results.add((rv = new Result(__n, __nextId())));
			return rv;
		}
	}
	
	/**
	 * Returns an array of all test results.
	 *
	 * @return The test results.
	 * @since 2016/07/14
	 */
	public final Result[] results()
	{
		// Lock
		List<Result> results = this._results;
		synchronized (results)
		{
			return results.<Result>toArray(new Result[results.size()]);
		}
	}
	
	/**
	 * Returns the sub-name of the test.
	 *
	 * @return The test sub-name.
	 * @since 2016/07/12
	 */
	public final TestSubName subName()
	{
		return this.name;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/07/12
	 */
	@Override
	public String toString()
	{
		throw new Error("TODO");
	}
	
	/**
	 * Returns the next test ID to use.
	 *
	 * @return The next test ID.
	 * @since 2016/07/13
	 */
	private final int __nextId()
	{
		// Lock
		List<Result> results = this._results;
		synchronized (results)
		{
			return this._nextid++;
		}
	}
	
	/**
	 * This represents a result of a test fragment, since there may be a need
	 * for multiple tests to exist for a given sub-test.
	 *
	 * @since 2016/07/12
	 */
	public final class Result
		implements AutoCloseable
	{
		/** The fragment name. */
		protected final TestFragmentName fragment;
		
		/** The test ID. */
		protected final int id;
		
		/** Object to lock for test results. */
		private final Object _lock =
			new Object();
		
		/** The result of a given test. */
		private volatile Status _status;
		
		/** Was a result given? */
		private volatile boolean _done;
		
		/** The associated exception. */
		private volatile Throwable _exception;
		
		/**
		 * Initializes the result.
		 *
		 * @param __n The fragment name.
		 * @param __dx The test number.
		 * @throws NullPointerException On null arguments.
		 * @since 2016/07/12
		 */
		private Result(TestFragmentName __n, int __dx)
			throws NullPointerException
		{
			// Check
			if (__n == null)
				throw new NullPointerException("NARG");
			
			// Set
			this.fragment = __n;
			this.id = __dx;
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2016/07/13
		 */
		@Override
		public void close()
		{
			// Lock
			synchronized (this._lock)
			{
				// {@squrreljme.error AG07 No status was associated with the
				// given test. (The test group; The sub-test; The test
				// fragment)}
				if (this._status == null)
					throw new IllegalStateException(String.format(
						"AG07 %s %s %s", IndividualTest.this.group,
						IndividualTest.this.name, this.fragment));
			}
		}
	
		/**
		 * Compares two byte arrays to check how they compare to each other.
		 *
		 * @param __c The comparison to make.
		 * @param __a The expected array.
		 * @param __b The resulting array.
		 * @throws NullPointerException On null arguments.
		 * @since 2016/07/12
		 */
		public final void compareByteArrays(TestComparison __c, byte[] __a,
			byte[] __b)
			throws NullPointerException
		{
			// Check
			if (__c == null || __a == null || __b == null)
				throw new NullPointerException("NARG");
			
			// Lock
			synchronized (this._lock)
			{
				// Done
				__setDone();
				
				if (true)
					throw new Error("TODO");
			
				// No more results
				close();
			}
		}
	
		/**
		 * Compares two int values to check how they compare to each other.
		 *
		 * @param __c The comparison to make.
		 * @param __a The expected value.
		 * @param __b The resulting value.
		 * @throws NullPointerException On null arguments.
		 * @since 2016/07/12
		 */
		public final void compareInt(TestComparison __c, int __a, int __b)
			throws NullPointerException
		{
			// Check
			if (__c == null)
				throw new NullPointerException("NARG");
		
			// Lock
			synchronized (this._lock)
			{
				// Done
				__setDone();
				
				if (true)
					throw new Error("TODO");
			
				// No more results
				close();
			}
		}
	
		/**
		 * Compares two int arrays to check how they compare to each other.
		 *
		 * @param __c The comparison to make.
		 * @param __a The expected array.
		 * @param __b The resulting array.
		 * @throws NullPointerException On null arguments.
		 * @since 2016/07/12
		 */
		public final void compareIntArrays(TestComparison __c, int[] __a,
			int[] __b)
			throws NullPointerException
		{
			// Check
			if (__c == null || __a == null || __b == null)
				throw new NullPointerException("NARG");
		
			// Lock
			synchronized (this._lock)
			{
				// Done
				__setDone();
				
				if (true)
					throw new Error("TODO");
			
				// No more results
				close();
			}
		}
	
		/**
		 * Compares two object values to check how they compare to each other.
		 *
		 * @param __c The comparison to make.
		 * @param __a The expected value.
		 * @param __b The resulting value.
		 * @throws NullPointerException On null arguments.
		 * @since 2016/07/12
		 */
		public final void compareObject(TestComparison __c, Object __a,
			Object __b)
			throws NullPointerException
		{
			// Check
			if (__c == null)
				throw new NullPointerException("NARG");
		
			// Lock
			synchronized (this._lock)
			{
				// Done
				__setDone();
				
				if (true)
					throw new Error("TODO");
			
				// No more results
				close();
			}
		}
	
		/**
		 * Compares two string values to check how they compare to each other.
		 *
		 * @param __c The comparison to make.
		 * @param __a The expected value.
		 * @param __b The resulting value.
		 * @throws NullPointerException On null arguments.
		 * @since 2016/07/12
		 */
		public final void compareString(TestComparison __c, String __a,
			String __b)
			throws NullPointerException
		{
			// Check
			if (__c == null)
				throw new NullPointerException("NARG");
		
			// Lock
			synchronized (this._lock)
			{
				// Done
				__setDone();
				
				if (true)
					throw new Error("TODO");
			
				// No more results
				close();
			}
		}
		
		/**
		 * Returns associated exception information.
		 *
		 * @return An associated exception or {@code null} if none was thrown.
		 * @since 2016/07/14
		 */
		public final Throwable exception()
		{
			// Lock
			synchronized (this._lock)
			{
				return this._exception;
			}
		}
		
		/**
		 * This is invoked when an exception is thrown where it should be
		 * treated as failure (and not success).
		 *
		 * @param __t The exception that caused failure.
		 * @throws NullPointerException On null arguments.
		 * @since 2016/07/13
		 */
		public final void failingException(Throwable __t)
			throws NullPointerException
		{
			// Check
			if (__t == null)
				throw new NullPointerException("NARG");
		
			// Lock
			synchronized (this._lock)
			{
				// Done
				__setDone();
				
				// Force failure
				this._status = Status.FAIL;
				
				// Set exception
				this._exception = __t;
			
				// No more results
				close();
			}
		}
		
		/**
		 * Returns the fragment name being used.
		 *
		 * @return The fragment name.
		 * @since 2016/07/12
		 */
		public final TestFragmentName fragmentName()
		{
			return this.fragment;
		}
		
		/**
		 * Returns the test identifier.
		 *
		 * @return The test identifier.
		 * @since 2016/07/13
		 */
		public final int id()
		{
			return this.id;
		}
	
		/**
		 * Notes something that is neither a passing nor failing state.
		 *
		 * @param __v The value to note.
		 * @since 2016/07/12
		 */
		public final void note(Object __v)
		{
			// Lock
			synchronized (this._lock)
			{
				// Done
				__setDone();
				
				if (true)
					throw new Error("TODO");
			
				// No more results
				close();
			}
		}
		
		/**
		 * Returns the test status.
		 *
		 * @return The test status.
		 * @throws IllegalStateException If the test has no result.
		 * @since 2016/07/13
		 */
		public final Status status()
			throws IllegalStateException
		{
			// Lock
			synchronized (this._lock)
			{
				// {@squirreljme.error AG05 The result of the test is not
				// known. (The test group; The sub-test; The test fragment)}
				Status rv = this._status;
				if (!this._done	|| rv == null)
					throw new IllegalStateException(String.format(
						"AG05 %s %s %s", IndividualTest.this.group,
						IndividualTest.this.name, this.fragment));
				
				// Return it
				return rv;
			}
		}
		
		/**
		 * Marks that the test was a success with no further information.
		 *
		 * @since 2016/07/13
		 */
		public final void success()
		{
			// Lock
			synchronized (this._lock)
			{
				// Done
				__setDone();
				
				if (true)
					throw new Error("TODO");
			
				// No more results
				close();
			}
		}
		
		/**
		 * Sets that the test is done.
		 *
		 * @throws IllegalStateException If it is already done.
		 * @since 2016/07/13
		 */
		private final void __setDone()
			throws IllegalStateException
		{
			// Lock
			synchronized (this._lock)
			{
				// {@squirreljme.error AG06 A result for a given test fragment
				// has already been performed. (The test group; The sub-test;
				// The test fragment)}
				if (this._done)
					throw new IllegalStateException(String.format(
						"AG06 %s %s %s", IndividualTest.this.group,
						IndividualTest.this.name, this.fragment));
				
				// Mark done
				this._done = true;
			}
		}
	}
	
	/**
	 * This indicates the status of a given test.
	 *
	 * @since 2016/07/13
	 */
	public static enum Status
	{
		/** Not passing or failing, but just a note. */
		NOTE,
		
		/** Test passed. */
		PASS,
		
		/** Test failed. */
		FAIL,
		
		/** End. */
		;
	}
}

