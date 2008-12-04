/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.demio.views.nav;

/**
 * This action toggles whether this navigator links its selection to the active
 * editor.
 * 
 * @since 2.1
 */
public class ToggleLinkingAction extends ResourceNavigatorAction {

	private static final String COMMAND_ID = "org.eclipse.ui.navigate.linkWithEditor"; //$NON-NLS-1$

	/**
     * Constructs a new action.
     */
    public ToggleLinkingAction(IResourceNavigator navigator, String label) {
        super(navigator, label);
        setActionDefinitionId(COMMAND_ID);
        setChecked(navigator.isLinkingEnabled());
    }

    /**
     * Runs the action.
     */
    public void run() {
        getNavigator().setLinkingEnabled(isChecked());
    }

}
