// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) 2013-2016 Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) 2013-2016 Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// For more information see license.mkd.
// ---------------------------------------------------------------------------

package net.multiphasicapps.squirreljme.singularexport;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.multiphasicapps.squirreljme.classformat.ClassDecoder;
import net.multiphasicapps.squirreljme.java.manifest.JavaManifest;
import net.multiphasicapps.squirreljme.java.manifest.JavaManifestAttributes;
import net.multiphasicapps.squirreljme.java.manifest.JavaManifestException;
import net.multiphasicapps.squirreljme.java.manifest.JavaManifestKey;
import net.multiphasicapps.squirreljme.java.manifest.MutableJavaManifest;
import net.multiphasicapps.squirreljme.java.manifest.
	MutableJavaManifestAttributes;
import net.multiphasicapps.squirreljme.java.symbols.ClassLoaderNameSymbol;
import net.multiphasicapps.squirreljme.java.symbols.ClassNameSymbol;
import net.multiphasicapps.squirreljme.projects.ProjectGroup;
import net.multiphasicapps.squirreljme.projects.ProjectInfo;
import net.multiphasicapps.squirreljme.projects.ProjectList;
import net.multiphasicapps.squirreljme.projects.ProjectType;
import net.multiphasicapps.util.sorted.SortedTreeSet;
import net.multiphasicapps.zip.streamwriter.ZipStreamWriter;
import net.multiphasicapps.zip.ZipCompressionType;

/**
 * Main entry class for the singular package export system.
 *
 * @since 2016/09/29
 */
public class Main
{
	/**
	 * Main entry point.
	 *
	 * @param __args Program arguments.
	 * @since 2016/09/29
	 */
	public static void main(String... __args)
	{
		// Force to exist
		if (__args == null)
			__args = new String[0];
		
		// Queue all strings
		Deque<String> args = new ArrayDeque<>();
		for (String s : __args)
			args.offerLast(s);
		
		// Include optional projects?
		boolean optionals = false;
		
		// Output file
		Path outfile = null;
		
		// The project to be used as the backing virtual engine
		String virtenginename = null;
		
		// Handle all arguments
		while (!args.isEmpty())
		{
			String s = args.peekFirst();
			
			// If not a switch then remove it
			if (!s.startsWith("-"))
				break;
			
			// Otherwise remove it
			args.removeFirst();
			
			// Handle
			switch (s)
			{
					// Include optional dependencies
				case "-a":
					optionals = true;
					break;
					
					// The output file
				case "-o":
					outfile = Paths.get(args.removeFirst());
					break;
					
					// Include a virtual environment
				case "-v":
					virtenginename = args.removeFirst();
					break;
				
					// {@squirreljme.error DV01 Unknown command line
					// argument. (The switch)}
				default:
					throw new IllegalArgumentException(String.format("DV01 %s",
						s));
			}
		}
		
		// {@squirreljme.error DV02 No global project list has been
		// initialized, this project may only be launched from the build
		// system.}
		ProjectList pl = ProjectList.getGlobalProjectList();
		if (pl == null)
			throw new IllegalStateException("DV02");
			
		// {@squirreljme.error DV03 No projects were specified on the command
		// line. Usage: [-a] [-o file] [-v project] (projects).
		// -a: Include optional dependencies.
		// -o: Output to this given file.
		// -v: The project to use as a wrapper (main entry point) for the
		// virtualized project set.}
		if (args.isEmpty())
			throw new IllegalArgumentException("DV03");
		
		// Locate the virtual engine to use as a abse
		ProjectInfo virtengine = null;
		if (virtenginename != null)
		{
			// {@squirreljme.error DV08 Could not find the project for the
			// given virtual project. (The virtual engine name)}
			ProjectGroup veg = pl.get(virtenginename);
			if (veg == null)
				throw new IllegalStateException(String.format("DV08 %s",
					virtenginename));
			
			// Need to compile the virtual engine
			try
			{
				virtengine = veg.compileSource(null, optionals);
			
				// {@squirreljme.error DV07 Could not obtain the binary for the
				// virtual engine. (The virtual engine name)}
				if (virtengine == null)
					throw new IllegalStateException(String.format("DV07 %s",
						virtenginename));
			}
			
			// {@squirreljme.error DV09 Read/write error handling the virtual
			// engine project.}
			catch (IOException e)
			{
				throw new RuntimeException("DV09");
			}
		}
		
		// Build and obtain binary projects to be included in the build
		Set<ProjectInfo> projects = new SortedTreeSet<>();
		ProjectInfo mainproj = __loadProjects(pl, projects, args, optionals);
		
		// Use default output file?
		if (outfile == null)
			outfile = Paths.get("x-squirreljme-" + mainproj.name() + ".jar");
		
		// Merge them together into one
		__merge(mainproj, projects, outfile, virtengine, optionals);
	}
	
	/**
	 * Loads all project binaries.
	 *
	 * @param __pl The project list to source projects from.
	 * @param __into The destination set.
	 * @param __from The source project name queue.
	 * @param __opt Include optional dependencies?
	 * @return The main project.
	 * @throws NullPointerException On null arguments.
	 * @since 2016/09/29
	 */
	private static ProjectInfo __loadProjects(ProjectList __pl,
		Set<ProjectInfo> __into, Collection<String> __from, boolean __opt)
		throws NullPointerException
	{
		// Check
		if (__into == null || __from == null)
			throw new NullPointerException("NARG");	
		
		// The main project (the first one used)
		ProjectInfo rv = null;
		
		// Read all arguments
		for (String rpn : __from)
		{
			// Get group for this project
			ProjectGroup pg = __pl.get(rpn);
			
			// {@squirreljme.error DV04 No project with the specified name
			// exists. (The project name)}
			if (pg == null)
				throw new IllegalArgumentException(String.format("DV04 %s",
					rpn));
			
			// Compile it along with optional dependencies
			ProjectInfo bin;
			try
			{
				bin = pg.compileSource(null, __opt);
			}
			
			// {@squirreljme.error DV05 Read/write error reading the project
			// information.}
			catch (IOException e)
			{
				throw new RuntimeException("DV05", e);
			}
			
			// Set main project if it has not been set, since there will need
			// to be a known Main-Class or main MIDlet.
			if (rv == null)
				rv = bin;
			
			// Add dependencies
			__into.add(bin);
			__pl.recursiveDependencies(__into, ProjectType.BINARY, pg.name(),
				__opt);
		}
		
		// Return the main project
		return rv;
	}
	
	/**
	 * Sets up the manifest used for output.
	 *
	 * @param __main The main project.
	 * @param __mjm The output manifest.
	 * @param __virt The virtual engine to use.
	 * @throws NullPointerException On null arguments, except for
	 * {@code __virt}.
	 * @since 2016/09/29
	 */
	private static void __manifest(ProjectInfo __main,
		MutableJavaManifest __mjm, ProjectInfo __virt)
		throws NullPointerException
	{
		// Check
		if (__main == null || __mjm == null)
			throw new NullPointerException("NARG");
		
		// Remove some keys in the main attribute set
		MutableJavaManifestAttributes attr = __mjm.getMainAttributes();
		Iterator<Map.Entry<JavaManifestKey, String>> it = attr.entrySet().
			iterator();
		while (it.hasNext())
		{
			Map.Entry<JavaManifestKey, String> e = it.next();
			String s = e.getKey().toString();
			switch (s)
			{
					// Remove
				case "class-path":
				case "x-squirreljme-depends":
				case "x-squirreljme-optional":
				case "x-squirreljme-uuid":
					it.remove();
					break;
					
					// Modify
				case "x-squirreljme-name":
					e.setValue("x-squirreljme-" + __main.name());
					break;
					
					// Potentially keep
				default:
					if (s.startsWith("midlet-dependency-") ||
						s.startsWith("liblet-dependency-"))
						it.remove();
					break;
			}
		}
	}
	
	/**
	 * Merges all of the input binaries 
	 *
	 * @param __main The main class to get the manifest from.
	 * @param __p The projects to merge together.
	 * @param __of The output file.
	 * @param __virt The virtual engine to use.
	 * @param __opt Include optional projects?
	 * @throws NullPointerException On null arguments, except for
	 * {@code __virt}.
	 * @since 2016/09/29
	 */
	private static void __merge(ProjectInfo __main,
		Collection<ProjectInfo> __p, Path __of, ProjectInfo __virt,
		boolean __opt)
		throws NullPointerException
	{
		// Check
		if (__main == null || __p == null || __of == null)
			throw new NullPointerException("NARG");
		
		// Use the original base for non-virtual projects and virtual ones
		Collection<ProjectInfo> nonvirtual, virtual;
		if (__virt == null)
		{
			nonvirtual = __p;
			virtual = Arrays.<ProjectInfo>asList();
		}
		
		// Perform the load project stage for projects starting from the
		// specified virtual project
		else
		{
			Set<ProjectInfo> workvirt = new HashSet<>();
			
			// Load virtual projects
			ProjectInfo mainproj = __loadProjects(__main.projectList(),
				workvirt, Arrays.<String>asList(__virt.name().toString()),
				__opt);
			
			// Do not virtualize the virtual engine, but do virtualize the
			// original input projects
			nonvirtual = workvirt;
			virtual = __p;
		}
		
		// Perform the merge
		Path tempjar = null;
		try
		{
			// Create file
			tempjar = Files.createTempFile("squirreljme-merge", ".jar");
			
			// Working data needed for export
			__Data__ data = new __Data__();
			
			// Write the output ZIP
			try (ZipStreamWriter zsw = new ZipStreamWriter(Channels.
				newOutputStream(FileChannel.open(tempjar,
				StandardOpenOption.WRITE, StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING))))
			{
				// Output a manifest
				MutableJavaManifest mjm = new MutableJavaManifest(
					__main.manifest());
				__manifest(__main, mjm, __virt);
				try (OutputStream os = zsw.nextEntry("META-INF/MANIFEST.MF",
					ZipCompressionType.DEFAULT_COMPRESSION))
				{
					mjm.write(os);
				}
				
				// Handle non-virtual projects
				for (ProjectInfo pi : nonvirtual)
				{
					// Ignore the CLDC libraries because these would be handled
					// by the host system (hopefully anyway) and due to the
					// fact that these libraries are very much SquirrelJME
					// only.
					String pn = pi.name().toString();
					if ((pn.equals("cldc-compact") ||
						pn.equals("cldc-full")))
						continue;
					
					// Process
					__process(zsw, pi, data, null);
				}
				
				// Handle virtual projects
				for (ProjectInfo pi : virtual)
					__process(zsw, pi, data, __virt);
				
				// Write virtualized project names
				try (PrintStream ps = new PrintStream(zsw.nextEntry(
					"$squirreljme$virtual-projects",
					ZipCompressionType.DEFAULT_COMPRESSION), true, "utf-8"))
				{
					// Print project name
					for (ProjectInfo pi : virtual)
						ps.println(pi.name());
					
					// Flush
					ps.flush();
				}
				
				// Write virtualized resource map
				try (PrintStream ps = new PrintStream(zsw.nextEntry(
					"$squirreljme$virtual-resources",
					ZipCompressionType.DEFAULT_COMPRESSION), true, "utf-8"))
				{
					// Print resource that exist
					for (String k : data._resources.keySet())
						ps.println(k);
					
					// Flush
					ps.flush();
				}
				
				// Write services
				for (Map.Entry<String, List<String>> e :
					data._services.entrySet())
				{
					try (OutputStream os = zsw.nextEntry("META-INF/services/" +
						e.getKey(), ZipCompressionType.DEFAULT_COMPRESSION);
						PrintStream pos = new PrintStream(os, true))
					{
						for (String q : e.getValue())
							pos.println(q);
						
						// Flush, just in case
						pos.flush();
					}
				}
			}
			
			// Ok, move the result
			Files.move(tempjar, __of);
		}
		
		// {@squirreljme.error DV06 Failed to read/write for project merge.}
		catch (IOException e)
		{
			throw new RuntimeException("DV06", e);
		}
		
		// Delete temporaries
		finally
		{
			// Delete it
			try
			{
				Files.delete(tempjar);
			}
			
			// Ignore
			catch (IOException e)
			{
			}
		}
	}
	
	/**
	 * Processes the given project.
	 *
	 * @param __zsw The output ZIP.
	 * @param __pi The project information.
	 * @param __data The output data as needed.
	 * @param __virt The virtualization engine used.
	 * @throws IOException On read/write errors.
	 * @throws NullPointerException On null arguments, except for
	 * {@code __virt}.
	 * @since 2016/09/30
	 */
	private static void __process(ZipStreamWriter __zsw, ProjectInfo __pi,
		__Data__ __data, ProjectInfo __virt)
		throws IOException, NullPointerException
	{
		// Check
		if (__zsw == null || __pi == null || __data == null)
			throw new NullPointerException("NARG");
		
		// The virtual package handler, Needs VirtualSquirrelJME and
		// VirtualObject instances.
		ClassNameSymbol vpack;
		if (__virt == null)
			vpack = null;
		
		// Depends on the manifest detail
		else
		{
			// {@squirreljme.error DV0a The virtual engine lacks the
			// X-SquirrelJME-VirtualEngine attribute which is used to define
			// which package contains the virtual handlers.}
			String vpn = __virt.manifest().getMainAttributes().get(
				new JavaManifestKey("X-SquirrelJME-VirtualEngine"));
			if (vpn == null)
				throw new IllegalStateException("DV0a");
			
			// Set
			vpack = ClassLoaderNameSymbol.of(vpn).asClassName();
		}
		
		// Handle contents
		byte[] buf = new byte[512];
		for (String c : __pi.contents())
		{
			// If set then the data is copied directly using the given name
			String copyname = null;
			
			// Non-virtual handling, pretty much straight through transfer of
			// classes and such
			if (__virt == null)
			{
				// Ignore manifests
				if (c.equals("META-INF/MANIFEST.MF"))
					continue;
			
				// Handle services using other means
				if (c.startsWith("META-INF/services/"))
				{
					__data.__servicesFile(c, new BufferedReader(
						new InputStreamReader(__pi.open(c), "utf-8")));
					continue;
				}
				
				// Copy the data
				copyname = c;
			}
			
			// Virtualizing class
			else if (c.endsWith(".class"))
			{
				// {@squirreljme.error DV0b Virtualizing the specified class.
				// (The class being virtualized)}
				System.err.printf("DV0b %s%n", c);
				
				// Open the input class
				try (DataInputStream dis = new DataInputStream(__pi.open(c)))
				{
					// Virtualize
					__VirtualClass__ vc = new __VirtualClass__(vpack);
					new ClassDecoder(dis, vc).decode();
					
					// Write the output class
					try (DataOutputStream dos = new DataOutputStream(
						__zsw.nextEntry(vc.__name(),
						ZipCompressionType.DEFAULT_COMPRESSION)))
					{
						vc.__output(dos);
					}
				}
			}
			
			// Virtualizing resource
			else
			{
				// Add virtual resource to the resource list
				copyname = __data.__addResource(__pi, c);
			}
			
			// If copying the data with no processing, copy it
			if (copyname != null)
				try (OutputStream os = __zsw.nextEntry(copyname,
					ZipCompressionType.DEFAULT_COMPRESSION);
					InputStream is = __pi.open(c))
				{
					for (;;)
					{
						// Read
						int rc = is.read(buf);
				
						// EOF?
						if (rc < 0)
							break;
				
						// Write
						os.write(buf, 0, rc);
					}
				}
		}
	}
}

