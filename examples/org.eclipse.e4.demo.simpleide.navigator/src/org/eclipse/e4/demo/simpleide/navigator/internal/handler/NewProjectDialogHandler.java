package org.eclipse.e4.demo.simpleide.navigator.internal.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.inject.Named;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.demo.simpleide.navigator.IProjectCreator;
import org.eclipse.e4.demo.simpleide.navigator.internal.ServiceRegistryComponent;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NewProjectDialogHandler {
	private Map<IProjectCreator, Image> images = new HashMap<IProjectCreator, Image>();
	private String projectName = "";
	private IProjectCreator creator;
	
	@Execute
	public void openNewProjectEditor(@Named(IServiceConstants.ACTIVE_SHELL) Shell parentShell, IWorkspace workspace, IProgressMonitor monitor, final ServiceRegistryComponent serviceRegistry) {
		TitleAreaDialog dialog = new TitleAreaDialog(parentShell) {
			private Text projectName;
			private TableViewer projectType;

			@Override
			protected Control createDialogArea(Composite parent) {
				Composite comp =  (Composite) super.createDialogArea(parent);
				getShell().setText("New Project");
				setTitle("New Project");
				setMessage("Create a new project by entering a name and select a project type");
				
				final Image titleImage = new Image(parent.getDisplay(), getClass().getClassLoader().getResourceAsStream("/icons/wizard/newjprj_wiz.png"));
				
				setTitleImage(titleImage);
				
				final Image shellImg = new Image(parent.getDisplay(), getClass().getClassLoader().getResourceAsStream("/icons/newjprj_wiz.gif"));
				getShell().setImage(shellImg);
				getShell().addDisposeListener(new DisposeListener() {
					
					public void widgetDisposed(DisposeEvent e) {
						shellImg.dispose();
						titleImage.dispose();
					}
				});
				
				Composite container = new Composite(comp, SWT.NONE);
				container.setLayoutData(new GridData(GridData.FILL_BOTH));
				container.setLayout(new GridLayout(2,false));
				
				Label l = new Label(container, SWT.NONE);
				l.setText("Name");
				
				projectName = new Text(container, SWT.BORDER);
				projectName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				
				l = new Label(container, SWT.NONE);
				l.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
				l.setText("Type"); 
				
				projectType = new TableViewer(container);
				projectType.setContentProvider(new ArrayContentProvider());
				projectType.setLabelProvider(new LabelProvider() {
					@Override
					public String getText(Object element) {
						IProjectCreator el = (IProjectCreator) element;
						return el.getLabel();
					}
					
					@Override
					public Image getImage(Object element) {
						IProjectCreator el = (IProjectCreator) element;
						Image img = images.get(el);
						if( img == null ) {
							img = el.createIcon(getShell().getDisplay());
							images.put(el, img);
						}
						return img;
					}
				});
				
				Vector<IProjectCreator> creators = serviceRegistry.getCreators();
				projectType.setInput(creators);
				if( creators.size() > 0 ) {
					projectType.setSelection(new StructuredSelection(creators.get(0)));
				}
				projectType.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
				
				getShell().addDisposeListener(new DisposeListener() {
					
					public void widgetDisposed(DisposeEvent e) {
						for( Image img : images.values() ) {
							img.dispose();
						}
						images.clear();
					}
				});
				
				return comp; 
			}
			
			@Override
			protected void okPressed() {
				if( projectType.getSelection().isEmpty() ) {
					setMessage("Please select a project type", IMessageProvider.ERROR);
					return;
				}
				
				if( projectName.getText().trim().length() == 0 ) {
					setMessage("Please enter a projectname", IMessageProvider.ERROR);
					return;
				}
				
				NewProjectDialogHandler.this.creator = (IProjectCreator) ((IStructuredSelection)projectType.getSelection()).getFirstElement();
				NewProjectDialogHandler.this.projectName = projectName.getText();
				
				super.okPressed();
			}
		};
		
		if( dialog.open() == IDialogConstants.OK_ID ) {
			creator.createProject(parentShell, workspace, monitor, projectName);
		}
	} 
}
