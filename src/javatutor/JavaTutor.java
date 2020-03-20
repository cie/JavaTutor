package javatutor;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.feathersjs.client.Feathers;
import com.feathersjs.client.plugins.providers.FeathersSocketClient;
import com.feathersjs.client.plugins.providers.FeathersSocketIO;
import com.feathersjs.client.service.Result;

import javatutor.intro.JavaTutorIntro;
import javatutor.model.Task;
import javatutor.ui.JavaTutorEditor;

/**
 * The activator class controls the plug-in life cycle
 */
public class JavaTutor extends AbstractUIPlugin {

	private static final String PROJECT_NAME = "JavaTutor tasks";

	// The plug-in ID
	public static final String PLUGIN_ID = "JavaTutor"; //$NON-NLS-1$

	// The shared instance
	private static JavaTutor plugin;

	private static IProject project;

	private static IFolder src;

	private static String BASE_URL = "http://localhost:3030";

	/**
	 * The constructor
	 */
	public JavaTutor() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		Feathers app = Feathers.getInstance();
		app.setBaseUrl(BASE_URL);
		app.configure(new FeathersSocketIO());
		FeathersSocketClient provider = (FeathersSocketClient) app.getProvider();
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static JavaTutor getDefault() {
		return plugin;
	}

	public static void letsStart(JavaTutorIntro intro) {
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					SubMonitor progress = SubMonitor.convert(monitor, "Open task", 100);

					Feathers app = Feathers.getInstance();
					Result<Task> tasks;
//		try {
//			tasks = FeathersTools.awaitResult(c -> app.service("tasks", Task.class).find(c), Task.class);
//		} catch (InterruptedException | FeathersException e1) {
//			MessageDialog.openError(null, "Error", e1.getMessage());
//			return;
//		}
					IWorkbenchPage page = intro.getIntroSite().getWorkbenchWindow().getActivePage();
					for (IViewReference view : page.getViewReferences()) {
						IViewPart v = view.getView(false);
						if (v != null && v.getAdapter(IIntroPart.class) != null)
							continue;
						page.hideView(view);
					}
					IWorkspace workspace = ResourcesPlugin.getWorkspace();
					IWorkspaceRoot root = workspace.getRoot();
					project = root.getProject(PROJECT_NAME);

					if (!project.exists()) {
						createProject(progress.split(40));
					}
					progress.setWorkRemaining(40);
					final IFile file;
					try {
						file = createFile();
					} catch (CoreException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					progress.split(30);
					intro.getIntroSite().getShell().getDisplay().asyncExec(() -> {
						try {
							page.openEditor(new FileEditorInput(file), JavaTutorEditor.ID);
						} catch (PartInitException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						intro.setText("<h3>Analyze scores</h3>Write a program that reads an unspecified number of "
								+ "scores and determines how many scores are above or equal to the "
								+ "average and how many scores are below the average. "
								+ "Enter a negative number to signify the end of the input. "
								+ "Assume that the maximum number of scores is 100.");
						PlatformUI.getWorkbench().getIntroManager().setIntroStandby(intro, true);
					});
				}
			});
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof OperationCanceledException) return;
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void createProject(SubMonitor progress) {
		progress.beginTask("Create project", 100);
		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IProjectDescription desc = workspace.newProjectDescription(PROJECT_NAME);
			desc.setNatureIds(new String[] { JavaCore.NATURE_ID });
			project.create(desc, null);
			project.open(null);
			IJavaProject javaProject = JavaCore.create(project);
			src = project.getFolder("src");
			src.create(true, true, null);
			ArrayList<IClasspathEntry> cp = new ArrayList<>();
			cp.addAll(Arrays.asList(PreferenceConstants.getDefaultJRELibrary()));
			cp.add(JavaCore.newSourceEntry(src.getFullPath()));
			javaProject.setRawClasspath(cp.toArray(new IClasspathEntry[] {}), null);
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

	private static IFile createFile() throws CoreException {
		IFolder t = src.getFolder("javatutor");
		t.create(true, true, null);
		t = t.getFolder("tasks");
		t.create(true, true, null);
		t = t.getFolder("arrays");
		t.create(true, true, null);
		IFile file = t.getFile("AboveBelowAverage.java");
		InputStream contents = JavaTutorIntro.class.getClassLoader()
				.getResourceAsStream("javatutor/tasks/arrays/AboveBelowAverage.txt");
		file.create(contents, true, null);
		return file;
	}

}
