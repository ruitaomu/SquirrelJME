// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) 2013-2016 Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) 2013-2016 Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU Affero General Public License v3+, or later.
// For more information see license.mkd.
// ---------------------------------------------------------------------------

package net.multiphasicapps.squirreljme.launcher;

import java.io.Closeable;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.multiphasicapps.imagereader.ImageData;
import net.multiphasicapps.imagereader.ImageReaderFactory;
import net.multiphasicapps.imagereader.ImageType;
import net.multiphasicapps.imagereader.xpm.XPMImageReader;
import net.multiphasicapps.squirreljme.classpath.ClassPath;
import net.multiphasicapps.squirreljme.classpath.ClassUnit;
import net.multiphasicapps.squirreljme.classpath.ClassUnitProvider;
import net.multiphasicapps.squirreljme.kernel.Kernel;
import net.multiphasicapps.squirreljme.kernel.KernelProcess;
import net.multiphasicapps.squirreljme.kernel.KIOException;
import net.multiphasicapps.squirreljme.kernel.KIOSocket;
import net.multiphasicapps.squirreljme.ui.UIDisplay;
import net.multiphasicapps.squirreljme.ui.UIException;
import net.multiphasicapps.squirreljme.ui.UIImage;
import net.multiphasicapps.squirreljme.ui.UILabel;
import net.multiphasicapps.squirreljme.ui.UIList;
import net.multiphasicapps.squirreljme.ui.UIListData;
import net.multiphasicapps.squirreljme.ui.UIManager;
import net.multiphasicapps.squirreljme.ui.UIMenu;
import net.multiphasicapps.squirreljme.ui.UIMenuItem;

/**
 * This class is the standard launcher which is used to run list programs,
 * launch programs, and perform other various things.
 *
 * Due to the design of SquirrelJME, only a single launcher is required
 * because the heavy lifting of UI code is done by the implementation
 * specific display manager.
 *
 * @since 2016/05/20
 */
public class LauncherInterface
	implements Runnable
{
	/** The kernel to launch and control for. */
	protected final Kernel kernel;
	
	/** The process of the kernel. */
	protected final KernelProcess kernelprocess;
	
	/** The display manager to use to interact with the user. */
	protected final UIManager displaymanager;
	
	/** The list manager. */
	private final __ClassUnitList__ _cul;
	
	/** Class unit providers. */
	private final ClassUnitProvider[] _cups;
	
	/** The primary display. */
	private volatile UIDisplay _maindisp;
	
	/**
	 * Initializes the launcher interface.
	 *
	 * @param __k The kernel to provide a launcher for.
	 * @param __dm The kernel display manager.
	 * @param __cup The class unit providers which are available.
	 * @throws IllegalStateException If the server could not be found or did
	 * not permit a connection.
	 * @throws NullPointerException On null arguments.
	 * @since 2016/05/20
	 */
	public LauncherInterface(Kernel __k, UIManager __dm,
		ClassUnitProvider... __cups)
		throws IllegalStateException, NullPointerException
	{
		// Check
		if (__k == null || __dm == null || __cups == null)
			throw new NullPointerException("NARG");
		
		// Set
		this.kernel = __k;
		KernelProcess kernelprocess = __k.kernelProcess();
		this.kernelprocess = kernelprocess;
		this.displaymanager = __dm;
		this._cups = __cups.clone();
		this._cul = new __ClassUnitList__(this);
		
		// Setup new launcher thread which runs under the kernel
		kernelprocess.createThread(this);
	}
	
	/**
	 * Loads an image from the given resource.
	 *
	 * @param __res The resource name to load.
	 * @return The read image data.
	 * @throws NullPointerException On null arguments.
	 * @since 2016/05/26
	 */
	protected final ImageData loadImage(String __res)
		throws NullPointerException
	{
		// Check
		if (__res == null)
			throw new NullPointerException("NARG");
		
		// Try reading the XPM image data
		try (InputStream is = getClass().getResourceAsStream(__res))
		{
			// If no image exists, ignore
			if (is == null)
				return null;
			
			// Read the image data
			return ImageReaderFactory.instance().readImage("xpm", is);
		}
		
		// Ignore read errors
		catch (OutOfMemoryError|UIException|IOException e)
		{
			return null;
		}
	}
	
	/**
	 * Returns the used display manager.
	 *
	 * @return The used display manager.
	 * @since 2016/05/25
	 */
	public final UIManager manager()
	{
		return this.displaymanager;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/05/20
	 */
	@Override
	public void run()
	{
		// Setup the local user interface
		setup();
		
		// Infinite loop
		for (;;)
		{
			// Sleep for a bit
			try
			{
				Thread.sleep(100L);
			}
			
			// Yield to let other threads besides the launcher run/
			catch (InterruptedException e)
			{
				Thread.yield();
			}
		}
	}
	
	/**
	 * Sets up the local launcher user interface and initailizes basic
	 * widgets to provide an interface to the user.
	 *
	 * @since 2016/05/21
	 */
	protected void setup()
	{
		// Create new display to be shown to the user
		UIManager displaymanager = this.displaymanager;
		UIDisplay maindisp = displaymanager.createDisplay();
		this._maindisp = maindisp;
		
		// Set title
		maindisp.setText("SquirrelJME");
		
		// Setup image which acts as the icon
		UIImage icon = displaymanager.createImage();
		
		// Load all preferred icon sizes
		int[] pis = displaymanager.preferredIconSizes();
		int pisn = pis.length;
		for (int i = 0; i < pisn; i += 2)
		{
			// Get dimensions
			int w = pis[i];
			int h = pis[i + 1];
			
			// Load
			ImageData id = loadImage("/net/multiphasicapps/squirreljme/" +
				"mascot/xpm/low/head_" + w + "x" + h + ".xpm");
			
			// Add if loaded
			if (id != null)
				icon.addImageData(id);
		}
		
		// Set icon
		maindisp.setIcon(icon);
		
		// Setup menu
		UIMenu mainmenu = displaymanager.createMenu();
		mainmenu.setText("Menu");
		
		// Quit item
		UIMenuItem quititem = displaymanager.createMenuItem();
		quititem.setText("Quit");
		mainmenu.add(quititem);
		
		// Use the menu
		maindisp.setMenu(mainmenu);
		
		// The list which contains the programs which are available to run
		__ClassUnitList__ cul = this._cul;
		maindisp.add(maindisp.size(), cul.getUIList());
		
		// Refresh the program list
		cul.refresh();
		
		// Done, make it visible
		maindisp.setVisible(true);
	}
	
	/**
	 * Returns the class unit providers which are currently available.
	 *
	 * @return the class unit providers.
	 * @since 2016/05/26
	 */
	final ClassUnitProvider[] __cups()
	{
		return this._cups;
	}
}

