/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.modifier;

import javax.inject.Inject;

import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.ScriptableObject;

public class ElementView {
	private Realm realm;
	private IObservableValue selectedElement = null;
	private IObservableValue jsText;
	private Text jsInputField;
	private Context jsContext;
	private ImporterTopLevel jsScope;

	public ElementView(final Composite parent) {
		realm = Realm.getDefault();
		selectedElement = new WritableValue(realm);
		jsText = new ComputedValue(realm) {
			@Override
			protected Object calculate() {
				return formatJSField((EObject) selectedElement.getValue());
			}
		};

		jsInputField = new Text(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER);

		Observables.pipe(jsText, SWTObservables.observeText(jsInputField));

		final Button runJSBtn = new Button(parent, SWT.PUSH);
		runJSBtn.setText("Run Script"); //$NON-NLS-1$
		runJSBtn.setBounds(10, 320, 100, 25);
		runJSBtn.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				runJS(jsInputField.getText());
			}
		});

		GridLayoutFactory.fillDefaults().generateLayout(parent);
		initJS();
	}

	protected String formatJSField(EObject value) {
		if (value == null) {
			return "";
		}
		String delim = jsInputField.getLineDelimiter();
		String jsString = "// " + value.eClass().getName()
				+ " is selected...\r\n" + "me = selectedElement;" + delim
				+ "out.println(me);" + delim;
		for (EStructuralFeature feature : value.eClass()
				.getEAllStructuralFeatures()) {
			Object val = value.eGet(feature);

			if (!(val instanceof String) && !(val instanceof Boolean)
					&& !(val instanceof Number)) {
				continue;
			}

			String propName = feature.getName().substring(0, 1).toUpperCase()
					+ feature.getName().substring(1);
			propName = feature.getName();
			String propString;

			if (val instanceof String) {
				propString = "me." + propName + "=\"" + val + "\"";
			} else {
				propString = "me." + propName + "=" + val + "";
			}

			jsString += propString + delim;
		}

		return jsString;
	}

	/**
	 * TBD this method is not needed; clean it up
	 * @param selection
	 */
	@Inject
	public void setInput(final EObject selection) {
		if (selection==null) {
			return;
		}
		realm.asyncExec(new Runnable() {
			public void run() {
				selectedElement.setValue(selection);
			}
		});
	}

	/**
	 * @param selection
	 */
	@Inject
	public void setSelection(final EObject selection) {
		if (selection==null) {
			return;
		}
		realm.asyncExec(new Runnable() {
			public void run() {
				selectedElement.setValue(selection);
			}
		});
	}

	protected void runJS(String jScript) {
		try {
			ScriptableObject.putProperty(jsScope, "selectedElement",
					new EMFScriptable((EObject) selectedElement.getValue()));
			jsContext.evaluateString(jsScope, jScript,
					"LCV Evaluator", 0, null); //$NON-NLS-1$
		} catch (RuntimeException e) {
			e.printStackTrace();
			initJS();
		}
	}

	private void initJS() {
		if (jsContext != null)
			Context.exit();

		jsContext = Context.enter();
		jsScope = new ImporterTopLevel(jsContext);
		Object wrappedOut = Context.javaToJS(System.out, jsScope);
		ScriptableObject.putProperty(jsScope, "out", wrappedOut); //$NON-NLS-1$
	}
}
