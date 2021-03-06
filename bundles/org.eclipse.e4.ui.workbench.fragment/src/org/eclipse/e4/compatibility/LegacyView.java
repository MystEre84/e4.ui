/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.compatibility;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.extensions.ExtensionUtils;
import org.eclipse.e4.extensions.ModelViewReference;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.workbench.ui.menus.MenuHelper;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.ViewSite;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.menus.IMenuService;

/**
 * This class is an implementation of an MPart that can be used to host a 3.x
 * ViewPart into an Eclipse 4.0 org.eclipse.e4.ui.model.application.
 * 
 * @since 4.0
 * 
 */
public class LegacyView {
	public final static String LEGACY_VIEW_URI = "bundleclass://org.eclipse.ui.workbench/org.eclipse.e4.compatibility.LegacyView"; //$NON-NLS-1$
	private IViewPart impl;

	public LegacyView(Composite parent, IEclipseContext context, MPart part) {
		// KLUDGE: the progress view assumes a grid layout, we should fix that
		// in 3.6
		Set kludge = new HashSet();
		kludge.add("org.eclipse.ui.views.ProgressView"); //$NON-NLS-1$
		kludge.add("org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$
		if (kludge.contains(part.getId())) {
			parent.setLayout(new GridLayout());
		} else {
			parent.setLayout(new FillLayout());
		}

		// Button btn = new Button(parent, SWT.BORDER);
		// btn.setText(part.getName());
		// if (btn != null)
		// return;
		String viewId = part.getId();
		IConfigurationElement viewContribution = findViewConfig(viewId);
		try {
			impl = (IViewPart) viewContribution
					.createExecutableExtension("class"); //$NON-NLS-1$
		} catch (CoreException e) {
			e.printStackTrace();
		}
		if (impl == null)
			return;

		try {
			final IEclipseContext localContext = part.getContext();
			localContext.set(IContextConstants.DEBUG_STRING, "Legacy View(" //$NON-NLS-1$
					+ part.getName() + ")"); //$NON-NLS-1$

			part.setObject(this);
			// Assign a 'site' for the newly instantiated part
			WorkbenchPage page = (WorkbenchPage) localContext
					.get(WorkbenchPage.class.getName());
			ModelViewReference ref = new ModelViewReference(part, page);
			ViewSite site = new ViewSite(ref, impl, page);
			site.setConfigurationElement(viewContribution);
			impl.init(site, null);
			// final ToolBarManager tbm = (ToolBarManager) site.getActionBars()
			// .getToolBarManager();
			// if (parent instanceof CTabFolder) {
			// final ToolBar tb = tbm.createControl(parent);
			// ((CTabFolder) parent).setTopRight(tb);
			// }

			// We need to create the actual TB (some parts call 'getControl')
			ToolBarManager tbMgr = (ToolBarManager) site.getActionBars()
					.getToolBarManager();
			tbMgr.createControl(parent);

			impl.createPartControl(parent);

			localContext.set(MPart.class.getName(), part);

			// Populate and scrape the old-style contributions...
			IMenuService menuSvc = (IMenuService) localContext
					.get(IMenuService.class.getName());

			String tbURI = "toolbar:" + part.getId(); //$NON-NLS-1$
			menuSvc.populateContributionManager(tbMgr, tbURI);
			MToolBar viewTB = MApplicationFactory.eINSTANCE.createToolBar();
			MenuHelper.processToolbarManager(localContext, viewTB,
					tbMgr.getItems());
			part.setToolbar(viewTB);
			tbMgr.getControl().dispose();

			String menuURI = "menu:" + part.getId(); //$NON-NLS-1$
			MenuManager menuMgr = (MenuManager) site.getActionBars()
					.getMenuManager();
			menuSvc.populateContributionManager(menuMgr, menuURI);
			MMenu viewMenu = MApplicationFactory.eINSTANCE.createMenu();
			viewMenu.setId(menuURI);
			MenuHelper.processMenuManager(localContext, viewMenu,
					menuMgr.getItems());
			part.getMenus().add(viewMenu);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return The implementation of the cient's 3.x view part
	 */
	public IViewPart getViewPart() {
		return impl;
	}

	private IConfigurationElement findViewConfig(String id) {
		IConfigurationElement[] views = ExtensionUtils
				.getExtensions(IWorkbenchRegistryConstants.PL_VIEWS);
		IConfigurationElement viewContribution = ExtensionUtils.findExtension(
				views, id);
		return viewContribution;
	}
}
