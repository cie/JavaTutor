package javatutor.intro;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IntroPart;

import javatutor.JavaTutor;
import javatutor.ui.JavaTutorEditor;

public class JavaTutorIntro extends IntroPart {

	private Browser browser;

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void standbyStateChanged(boolean standby) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createPartControl(Composite parent) {
		browser = new Browser(parent, 0);
		setText("<h2>Hello!</h2><p>This is a .... You have to ....<p>"
				+ "<button onclick='window.location=\"javatutor:letsStart\"'>Let's start!</button>");
		browser.addLocationListener(new LocationListener() {
			@Override
			public void changing(LocationEvent event) {
				if (event.location.equals("javatutor:letsStart")) {
					JavaTutor.letsStart(JavaTutorIntro.this);
					event.doit = false;
				}
				if (event.location.equals("javatutor:run")) {
					JavaTutor.run();
					event.doit = false;
				}
				if (event.location.equals("javatutor:nextTask")) {
					JavaTutor.nextTask();
					event.doit = false;
				}
			}
			
			@Override
			public void changed(LocationEvent event) {
				
			}
		});
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void setText(String html) {
		browser.setText(html);
	}

}
