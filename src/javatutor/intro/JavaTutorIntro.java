package javatutor.intro;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IntroPart;

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
		browser.setText("<h2>Hello!</h2><p>This is a survey. The task is:</p>"
				+ "<h3>Analyze scores</h3><p>Write a program that reads an unspecified number of "
				+ "scores and determines how many scores are above or equal to the "
				+ "average and how many scores are below the average. "
				+ "Enter a negative number to signify the end of the input. "
				+ "Assume that the maximum number of scores is 100.</p><p>"
				+ "<button onclick='window.open();return false'>Let's start!</button>");
		browser.addOpenWindowListener(new OpenWindowListener() {
			@Override
			public void open(WindowEvent event) {
				letsStart();
			}
		});
	}

	protected void letsStart() {
		PlatformUI.getWorkbench().getIntroManager().setIntroStandby(JavaTutorIntro.this, true);
		IWorkbenchPage page = this.getIntroSite().getWorkbenchWindow().getActivePage();
		for (IViewReference view : page.getViewReferences()) {
			IViewPart v = view.getView(false);
			if (v != null && v.getAdapter(IIntroPart.class) != null)
				continue;
			page.hideView(view);
		}
		browser.setText("<h3>Analyze scores</h3>Write a program that reads an unspecified number of "
				+ "scores and determines how many scores are above or equal to the "
				+ "average and how many scores are below the average. "
				+ "Enter a negative number to signify the end of the input. "
				+ "Assume that the maximum number of scores is 100.");
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		String projectName = "JavaTutor tasks";
		IProjectDescription desc = workspace.newProjectDescription(projectName);
		desc.setNatureIds(new String[] {JavaCore.NATURE_ID});
		IProject project = root.getProject(projectName);
		try {
			project.create(desc, null);
			project.open(null);
			IJavaProject javaProject = JavaCore.create(project);
			IFolder src = project.getFolder("src");
			src.create(true, true, null);
			ArrayList<IClasspathEntry> cp = new ArrayList<>();
			cp.addAll(Arrays.asList(PreferenceConstants.getDefaultJRELibrary()));
			cp.add(JavaCore.newSourceEntry(src.getFullPath()));
			javaProject.setRawClasspath(cp.toArray(new IClasspathEntry[] {}), null);
			IFolder t = src.getFolder("javatutor"); t.create(true, true, null);
			t = t.getFolder("tasks"); t.create(true, true, null);
			t = t.getFolder("arrays"); t.create(true, true, null);
			IFile file = t.getFile("AboveBelowAverage.java");
			InputStream startFile = JavaTutorIntro.class.getClassLoader().getResourceAsStream("javatutor/tasks/arrays/AboveBelowAverage.txt");
			file.create(startFile, true, null);
			page.openEditor(new FileEditorInput(file), JavaTutorEditor.ID);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
