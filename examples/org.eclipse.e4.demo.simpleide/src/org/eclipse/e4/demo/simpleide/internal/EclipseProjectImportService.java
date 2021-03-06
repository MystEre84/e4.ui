/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.demo.simpleide.internal;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.demo.simpleide.internal.datatransfer.ExternalProjectImportWizard;
import org.eclipse.e4.demo.simpleide.services.IImportResourceService;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.FrameworkUtil;

public class EclipseProjectImportService implements IImportResourceService {

	public String getIconURI() {
		return "platform:/plugin/" + FrameworkUtil.getBundle(getClass()).getSymbolicName() + "/icons/newprj_wiz.gif";
	}

	public String getLabel() {
		return "Existing Projects Into Workspace";
	}

	public void importResource(Shell shell, IEclipseContext context) {
		ExternalProjectImportWizard wz = ContextInjectionFactory.make(ExternalProjectImportWizard.class, context);
		WizardDialog dialog = new WizardDialog(shell, wz);
		dialog.open();
	}

	public String getCategoryName() {
		return "General";
	}

}
