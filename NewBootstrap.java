// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) 2013-2016 Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) 2013-2016 Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// For more information see license.mkd.
// ---------------------------------------------------------------------------

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.attribute.FileTime;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

/**
 * This class implements a bootstrap which is capable to build an environment
 * which is capable of building SquirrelJME target binaries.
 *
 * You should only compile and run this class manually if your system is not
 * able to use the pre-existing build scripts. If this is the case then you
 * must set the following system properties:
 *
 * {@code net.multiphasicapps.squirreljme.bootstrap.binary} is the location
 * where output binaries are to be placed when they are compiled, along with
 * the bootstrap.
 * {@code net.multiphasicapps.squirreljme.bootstrap.source} is the location
 * of the SquirrelJME source tree.
 *
 * @since 2016/10/26
 */
public class NewBootstrap
	implements Runnable
{
	/** Deletes files. */
	public static final Consumer<Path, Object, IOException> DELETE =
		new Consumer<Path, Object, IOException>()
		{
			/**
			 * {@inheritDoc}
			 * @since 2016/10/27
			 */
			@Override
			public void accept(Path __v, Object __s)
				throws IOException
			{
				Files.delete(__v);
			}
		};
	
	/** Returns the latest date. */
	public static final Consumer<Path, Long[], IOException> DATE =
		new Consumer<Path, Long[], IOException>()
		{
			/**
			 * {@inheritDoc}
			 * @since 2016/10/27
			 */
			@Override
			public void accept(Path __v, Long[] __s)
				throws IOException
			{
				// Dates on directories might not truly be valid
				if (Files.isDirectory(__v))
					return;
				
				// Get date
				FileTime ft = Files.getLastModifiedTime(__v);
				long millis = ft.toMillis();
				
				// If it is newer, use that
				if (millis > __s[0])
					__s[0] = millis;
			}
		};
	
	/** The binary path. */
	protected final Path binarypath;
	
	/** The source path. */
	protected final Path sourcepath;
	
	/** The input launch arguments. */
	protected final String[] launchargs;
	
	/** Projects available for usage. */
	protected final Map<String, BuildProject> projects;
	
	/** The output bootstrap binary. */
	protected final Path bootstrapout;
	
	/** The directory where JARs are placed. */
	protected final Path buildjarout;
	
	/**
	 * Initializes the bootstrap base.
	 *
	 * @param __bin The binary output directory.
	 * @param __src The source input namespace directories.
	 * @param __args Arguments to the bootstrap.
	 * @throws IOException On read/write errors.
	 * @throws NullPointerException On null arguments.
	 * @since 2016/10/26
	 */
	public NewBootstrap(Path __bin, Path __src, String[] __args)
		throws IOException, NullPointerException
	{
		// Check
		if (__bin == null || __src == null || __args == null)
			throw new NullPointerException("NARG");
		
		// Set
		this.binarypath = __bin;
		this.sourcepath = __src;
		this.launchargs = __args.clone();
		this.buildjarout = __bin.resolve("bootsjme");
		this.bootstrapout = __bin.resolve("sjmeboot.jar");
		
		// Load all projects in the build directory
		Map<String, BuildProject> projects = new LinkedHashMap<>();
		try (DirectoryStream<Path> ds =
			Files.newDirectoryStream(__src.resolve("build")))
		{
			// Go through all directories
			for (Path p : ds)
			{
				// Must be a directory
				if (!Files.isDirectory(p))
					continue;
			
				// See if the manifest exists
				Path man = p.resolve("META-INF").resolve("MANIFEST.MF");
				if (!Files.exists(man))
					continue;
			
				// Load project
				BuildProject bp = new BuildProject(p, man);
				projects.put(bp.projectName(), bp);
			}
		}
		this.projects = projects;
	}
		
	/**
	 * {@inheritDoc}
	 * @since 2016/10/26
	 */
	@Override
	public void run()
	{
		// {@squirreljme.error NB03 The entry point project does not exist.}
		Map<String, BuildProject> projects = this.projects;
		BuildProject bp = projects.get("host-javase");
		if (bp == null)
			throw new IllegalStateException("NB03");
		
		// Could fail
		Path tempjar = null;
		try
		{
			// Compile JARs to be merged together as one
			Set<BuildProject> mergethese = bp.compile();
			
			// Get the time and date for the JARs to merge
			Path bootjar = this.bootstrapout;
			Long[] out = new Long[1];
			out[0] = Long.MIN_VALUE;
			NewBootstrap.<Long[]>__walk(this.buildjarout, out, DATE);
			long depjartime = out[0];
			
			// Get the time of the output JAR
			long bootjartime;
			if (Files.exists(bootjar))
			{
				out[0] = Long.MIN_VALUE;
				NewBootstrap.<Long[]>__walk(bootjar, out, DATE);
				bootjartime = out[0];
			}
			else
				bootjartime = Long.MIN_VALUE;
			
			// Repackage
			if (bootjartime == Long.MIN_VALUE || depjartime > bootjartime)
			{
				// {@squirreljme.error NB0a Merging output JAR file.}
				System.err.println("NB0a");
				
				// Create temporary JAR
				tempjar = Files.createTempFile("squirreljme-boot-out", ".jar");
				
				// Open output
				try (ZipOutputStream zos = new ZipOutputStream(Channels.
					newOutputStream(FileChannel.open(tempjar,
					StandardOpenOption.WRITE, StandardOpenOption.CREATE))))
				{
					// Copy contents from other JARs
					for (BuildProject dp : mergethese)
						__mergeInto(dp, zos, dp == bp);
					
					// Finish it
					zos.finish();
					zos.flush();
				}
				
				// Move it
				Files.move(tempjar, bootjar,
					StandardCopyOption.REPLACE_EXISTING);
			}
		}
		
		// {@squirreljme.error NB04 Failed to compile the bootstrap due to
		// a read/write error.}
		catch (IOException e)
		{
			throw new RuntimeException("NB04", e);
		}
		
		// Cleanup temporary JAR if it was created
		finally
		{
			if (tempjar != null)
				try
				{
					Files.delete(tempjar);
				}
				catch (IOException e)
				{
				}
		}
	}
	
	/**
	 * Main entry point for the new bootstrap system.
	 *
	 * @param __args Program arguments.
	 * @throws IOException On any read/write errors.
	 * @since 2016/10/26
	 */
	public static void main(String... __args)
		throws IOException
	{
		// Get directories for input and output
		Path bin = Paths.get(System.getProperty(
			"net.multiphasicapps.squirreljme.bootstrap.binary")),
			src = Paths
			.get(System.getProperty(
			"net.multiphasicapps.squirreljme.bootstrap.source"));
		
		// Only build?
		NewBootstrap nb = new NewBootstrap(bin, src, __args);
		
		// Run it
		nb.run();
	}
	
	/**
	 * Lowercases the specified string.
	 *
	 * @param __s The string to check.
	 * @return The string in correct form.
	 * @throws IllegalArgumentException If it is not valid.
	 * @throws NullPointerException On null arguments.
	 * @since 2016/10/27
	 */
	private static String __correctProjectName(String __s)
		throws IllegalArgumentException, NullPointerException
	{
		// Check
		if (__s == null)
			throw new NullPointerException("NARG");
		
		// Check if any characters need lowering first
		int n = __s.length();
		boolean dolower = false;
		for (int i = 0; i < n; i++)
		{
			char c = __s.charAt(i);
			
			// Lowercase?
			if (c >= 'A' && c <= 'Z')
				dolower = true;
			
			// Legal character?
			else if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') ||
				c == '-')
				continue;
			
			// {@squirreljme.error NB01 Illegal character used in project
			// name. (The input project name)}
			else
				throw new IllegalArgumentException(String.format("NB01 %s",
					__s));
		}
		
		// Use original string
		if (!dolower)
			return __s;
		
		// Lowercase everything
		StringBuilder sb = new StringBuilder(n);
		for (int i = 0; i < n; i++)
		{
			char c = __s.charAt(i);
			
			// Lowercase?
			if (c >= 'A' && c <= 'Z')
				sb.append((char)((c - 'A') + 'a'));
			
			// Keep
			else
				sb.append(c);
		}
		
		// Use that
		return sb.toString();
	}
	
	/**
	 * Merges the input JAR into the output JAR.
	 *
	 * @param __bp The source.
	 * @param __zos The destination.
	 * @param __useman Use the manifest?
	 * @throws IOException On read/write errors.
	 * @throws NullPointerException On null arguments.
	 * @since 2016/10/28
	 */
	private static void __mergeInto(BuildProject __bp, ZipOutputStream __zos,
		boolean __useman)
		throws IOException, NullPointerException
	{
		// Check
		if (__bp == null || __zos == null)
			throw new NullPointerException("NARG");
		
		// Go through the input
		try (ZipInputStream zis = new ZipInputStream(Channels.newInputStream(
			FileChannel.open(__bp.jarout, StandardOpenOption.READ))))
		{
			// Copy all entries
			ZipEntry e;
			byte[] buf = new byte[4096];
			while (null != (e = zis.getNextEntry()))
			{
				// If the entry is the manifest, only use it if it was
				// requested
				if (e.getName().equals("META-INF/MANIFEST.MF"))
					if (!__useman)
					{
						zis.closeEntry();
						continue;
					}
				
				// Write to target
				__zos.putNextEntry(e);
				
				// Copy loop
				for (;;)
				{
					int rc = zis.read(buf);
					
					// EOF?
					if (rc < 0)
						break;
					
					// Write
					__zos.write(buf, 0, rc);
				}
				
				// Close
				__zos.closeEntry();
				zis.closeEntry();
			}
		}
	}
	
	/**
	 * Walks the given path and calls the given consumer for every file and
	 * directory.
	 *
	 * @param <S> The secondary value to pass.
	 * @param __p The path to walk.
	 * @param __s The secondary value.
	 * @param __c The function to call for paths.
	 * @throws IOException On read errors.
	 * @throws NullPointerException On null arguments, except for the secondary
	 * value.
	 * @since 2016/09/18
	 */
	private static <S> void __walk(Path __p, S __s,
		Consumer<Path, S, IOException> __c)
		throws IOException, NullPointerException
	{
		// Check
		if (__p == null || __c == null)
			throw new NullPointerException("NARG");
		
		// If a directory, walk through all the files
		if (Files.isDirectory(__p))
			try (DirectoryStream<Path> ds = Files.newDirectoryStream(__p))
			{
				for (Path s : ds)
					__walk(s, __s, __c);
			}
		
		// Always accept, directories are accepted last since directories
		// cannot be deleted if they are not empty
		__c.accept(__p, __s);
	}
	
	/**
	 * Calculates the name that a file would appear as inside of a ZIP file.
	 *
	 * @param __root The root path.
	 * @param __p The file to add.
	 * @return The ZIP compatible name.
	 * @throws NullPointerException On null arguments.
	 * @since 2016/03/21
	 */
	private static String __zipName(Path __root, Path __p)
		throws NullPointerException
	{
		// Check
		if (__root == null || __p == null)
			throw new NullPointerException();
		
		// Calculate relative name
		Path rel = __root.toAbsolutePath().relativize(__p.toAbsolutePath());
		
		// Build name
		StringBuilder sb = new StringBuilder();
		for (Path comp : rel)
		{
			// Prefix slash
			if (sb.length() > 0)
				sb.append('/');
			
			// Add component
			sb.append(comp);
		}
		
		// Return it
		return sb.toString();
	}
	
	/**
	 * This represents a single project which may be built.
	 *
	 * @since 2016/10/27
	 */
	public class BuildProject
	{
		/** The project base path. */
		protected final Path basepath;
		
		/** The project name. */
		protected final String name;
		
		/** The output path for this JAR. */
		protected final Path jarout;
		
		/** Dependencies of this build project. */
		protected final Set<String> depends;
		
		/** In compilation? */
		private volatile boolean _incompile;
		
		/** Was the source date calculated? */
		private volatile long _sourcedate =
			Long.MIN_VALUE;
		
		/**
		 * Initializes the build project.
		 *
		 * @param __b The project base path.
		 * @param __mp The manifest path.
		 * @throws IOException On read/write errors.
		 * @throws NullPointerException On null arguments.
		 * @since 2016/10/27
		 */
		BuildProject(Path __b, Path __mp)
			throws IOException, NullPointerException
		{
			// Check
			if (__b == null || __mp == null)
				throw new NullPointerException("NARG");
			
			// Set
			this.basepath = __b;
			
			// Load the manifest
			Manifest man;
			try (InputStream in = Channels.newInputStream(
				FileChannel.open(__mp, StandardOpenOption.READ)))
			{
				man = new Manifest(in);
			}
			
			// Handle main attributes
			Attributes attr = man.getMainAttributes();
			
			// {@squirreljme.error NB02 No project name was specified for
			// the given manifest. (The manifest path)}
			String rn = attr.getValue("X-SquirrelJME-BuildHostName");
			if (rn == null)
				throw new IllegalArgumentException(String.format("NB02 %s",
					__mp));
			String name;
			this.name = (name = __correctProjectName(rn.trim()));
			
			// Where is this output?
			this.jarout = NewBootstrap.this.buildjarout.resolve(name + ".jar");
			
			// Determine dependencies
			Set<String> depends = new LinkedHashSet<>();
			String rd = attr.getValue("X-SquirrelJME-BuildHostDepends");
			if (rd != null)
				for (String s : rd.split("[ \t]"))
					depends.add(__correctProjectName(s.trim()));
			this.depends = depends;
		}
		
		/**
		 * Returns the date of the binary.
		 *
		 * The value here is not cached.
		 *
		 * @return The date of the binary.
		 * @since 2016/10/27
		 */
		public long binaryDate()
		{
			// If it does not exist, the date is not valid
			Path jarout = this.jarout;
			if (!Files.exists(jarout))
				return Long.MIN_VALUE;
			
			// Calculate
			try
			{
				Long[] out = new Long[1];
				out[0] = Long.MIN_VALUE;
				NewBootstrap.<Long[]>__walk(jarout, out, DATE);
				return out[0];
			}
			
			// Ignore
			catch (IOException e)
			{
				return Long.MIN_VALUE;
			}
		}
		
		/**
		 * Compiles this project and any dependencies it may have.
		 *
		 * @return The set of projects which were compiled.
		 * @throws IOException On read/write errors.
		 * @since 2016/10/27
		 */
		public Set<BuildProject> compile()
			throws IOException
		{
			Set<BuildProject> rv = new LinkedHashSet<>();
			
			// Build loop
			try
			{
				// {@squirreljme.error NB05 The specified project eventually
				// depends on itself. (The name of this project)}
				if (this._incompile)
					throw new IllegalStateException(String.format("NB05 %s",
						this.name));
				this._incompile = true;
				
				// Compile dependencies first
				Map<String, BuildProject> projects =
					NewBootstrap.this.projects;
				for (String dep : this.depends)
				{
					// {@squirreljme.error NB06 The dependency of a given
					// project does not exist. (This project; The project it
					// depends on)}
					BuildProject dp = projects.get(dep);
					if (dp == null)
						throw new IllegalStateException(String.format(
							"NB06 %s %s", this.name, dep));
					
					// Compile the dependency and add it to the merge group
					for (BuildProject bp : dp.compile())
						rv.add(bp);
				}
				
				// Other complation state
				__compileStep(rv);
			}
			
			// Clear compile state
			finally
			{
				this._incompile = false;
			}
			
			// Add self to dependency chain
			rv.add(this);
			
			// Return it
			return rv;
		}
		
		/**
		 * Returns the name of this project.
		 *
		 * @return The project name.
		 * @since 2016/10/27
		 */
		public String projectName()
		{
			return this.name;
		}
		
		/**
		 * Returns the date when the project sources were last touched.
		 *
		 * @return The project sources date.
		 * @since 2016/10/27
		 */
		public long sourcesDate()
		{
			// Use cached value
			long rv = this._sourcedate;
			if (rv != Long.MIN_VALUE)
				return rv;
			
			// Calculate otherwise
			try
			{
				Long[] out = new Long[1];
				out[0] = Long.MIN_VALUE;
				NewBootstrap.<Long[]>__walk(this.basepath, out, DATE);
				this._sourcedate = (rv = out[0]);
			}
			
			// Ignore
			catch (IOException e)
			{
			}
			
			// Return it
			return rv;
		}
		
		/**
		 * Secondary compilation step.
		 *
		 * @param __deps Dependencies used.
		 * @throws IOException On read/write errors.
		 * @throws NullPointerException On null arguments.
		 * @since 2016/10/27
		 */
		private void __compileStep(Set<BuildProject> __deps)
			throws IOException, NullPointerException
		{
			// Check
			if (__deps == null)
				throw new NullPointerException("NARG");
			
			// Best dependency date
			long depdate = Long.MIN_VALUE;
			for (BuildProject bp : __deps)
				depdate = Math.max(depdate, bp.binaryDate());
			
			// Do not recompile if it is up to date
			long srcdate = sourcesDate(),
				bindate =  binaryDate();
			Path jarout = this.jarout;
			if (Files.exists(jarout) && srcdate <= bindate &&
				bindate > depdate)
				return;
			
			// {@squirreljme.error NB09 Now compiling the specified project.
			// (The name of the project being compiled)}
			String name = this.name;
			System.err.printf("NB09 %s%n", name);
			
			// {@squirreljme.error NB07 No system Java compiler is
			// available.}
			JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
			if (javac == null)
				throw new IllegalStateException("NB07");
			
			// Need to clear files
			Path tempdir = null;
			try
			{
				// Create temporary directory
				tempdir = Files.createTempDirectory(
					"squirreljme-build-" + name);
				
				// Source code is just this project
				final Path basepath = this.basepath;
				
				// Get all source code to be compiled
				final Set<File> fsources = new LinkedHashSet<>();
				NewBootstrap.<Object>__walk(basepath, null,
					new Consumer<Path, Object, IOException>()
					{
						/**
						 * {@inheritDoc}
						 * @since 2016/10/27
						 */
						@Override
						public void accept(Path __p, Object __s)
							throws IOException
						{
							// Ignore directories
							if (Files.isDirectory(__p))
								return;
							
							// Add to sources if it is a source file
							if (__p.getFileName().toString().endsWith(".java"))
								fsources.add(__p.toFile());
						}
					});
				
				// If there are no sources to compile, do not bother
				if (!fsources.isEmpty())
				{
					// Need to access the disk
					StandardJavaFileManager jfm = javac.getStandardFileManager(
						null, null, null);
					
					// Source code is just this project
					jfm.setLocation(StandardLocation.SOURCE_PATH,
						Arrays.<File>asList(basepath.toFile()));
				
					// Outputs to the temporary directory
					jfm.setLocation(StandardLocation.CLASS_OUTPUT,
						Arrays.<File>asList(tempdir.toFile()));
				
					// The class path is just the dependencies
					Set<File> fdeps = new LinkedHashSet<>();
					for (BuildProject bp : __deps)
						fdeps.add(bp.jarout.toFile());
					jfm.setLocation(StandardLocation.CLASS_PATH, fdeps);
					
					// Create task
					JavaCompiler.CompilationTask task = javac.getTask(null,
						jfm, null, Arrays.<String>asList("-source", "1.7",
						"-target", "1.7", "-g", "-Xlint:deprecation",
						"-Xlint:unchecked"), null, jfm.getJavaFileObjects(
						fsources.<File>toArray(new File[fsources.size()])));
				
					// {@squirreljme.error NB08 Compilation of this project
					// failed. (The name of this project)}
					if (!task.call())
						throw new RuntimeException(String.format("NB08 %s",
							name));
				}
				
				// Create directory used for output
				Path jpar = jarout.getParent();
				if (jpar != null)
					Files.createDirectories(jpar);
				
				// Generate JAR output
				Path tempjar = Files.createTempFile("buildjar", "jar");
				try (final ZipOutputStream zos = new ZipOutputStream(Channels.
					newOutputStream(FileChannel.open(tempjar,
					StandardOpenOption.WRITE, StandardOpenOption.CREATE))))
				{
					// Write files in the output and input
					final byte[] buf = new byte[4096];
					Consumer<Path, Path, IOException> func =
						new Consumer<Path, Path, IOException>()
						{
							/**
							 * {@inheritDoc}
							 * @since 2016/10/28
							 */
							@Override
							public void accept(Path __p, Path __s)
								throws IOException
							{
								ZipOutputStream out = zos;
								
								// Ignore directories
								if (Files.isDirectory(__p) || __p.
									getFileName().toString().endsWith("java"))
									return;
								
								// Ignore files ending in .java
								Path fn = __p.getFileName();
								String fns = __p.getFileName().toString();
								if (fns.endsWith(".java"))
									return;
								
								// Create new entry
								out.putNextEntry(
									new ZipEntry(__zipName(__s, __p)));
								
								// Copy data
								try (InputStream is = Channels.newInputStream(
									FileChannel.open(__p,
									StandardOpenOption.READ)))
								{
									for (;;)
									{
										int rc = is.read(buf);
										
										// EOF?
										if (rc < 0)
											break;
										
										// Copy
										out.write(buf, 0, rc);
									}
								}
								
								// Close
								out.closeEntry();
							}
						};
					
					// Add sources and classes
					NewBootstrap.<Path>__walk(basepath, basepath, func);
					NewBootstrap.<Path>__walk(tempdir, tempdir, func);
					
					// Finish it
					zos.finish();
					zos.flush();
					
					// Move JAR
					Files.move(tempjar, jarout,
						StandardCopyOption.REPLACE_EXISTING);
				}
				
				// Failed, delete output JAR
				catch (IOException|RuntimeException|Error e)
				{
					// Delete file
					try
					{
						Files.delete(tempjar);
					}
					catch (IOException f)
					{
						// Ignore
					}
					
					// Rethrow
					throw e;
				}
			}
			
			// Always clear at the end
			finally
			{
				if (tempdir != null)
					try
					{
						NewBootstrap.<Object>__walk(tempdir, null, DELETE);
					}
					catch (IOException e)
					{
						// Ignore
					}
			}
		}
	}
	
	/**
	 * Consumes a value which may throw an exception.
	 *
	 * @param <V> The value to consume.
	 * @param <S> A secondary value that may be passed.
	 * @param <E> The exception to potentially throw.
	 * @since 2016/10/27
	 */
	public static interface Consumer<V, S, E extends Exception>
	{
		/**
		 * Accepts a value.
		 *
		 * @param __v The value to access.
		 * @param __s An optional secondary value.
		 * @throws E If this given exception type is thrown.
		 * @since 2016/10/27
		 */
		public abstract void accept(V __v, S __s)
			throws E;
	}
}

