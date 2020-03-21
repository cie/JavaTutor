package javatutor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
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

import com.feathersjs.client.FeathersException;
import com.feathersjs.client.service.Result;

import javatutor.feathers.model.Task;
import javatutor.intro.JavaTutorIntro;
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

	private static IPackageFragmentRoot src;

	/**
	 * The constructor
	 */
	public JavaTutor() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		// App.get();
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
					Task task;
					try {
						task = loadTask();
					} catch (FeathersException e2) {
						throw new InvocationTargetException(e2);
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
						file = createJavaFile(task);
					} catch (CoreException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					progress.split(30);
					intro.getIntroSite().getShell().getDisplay().syncExec(() -> {
						IWorkbenchPage page = intro.getIntroSite().getWorkbenchWindow().getActivePage();
						for (IViewReference view : page.getViewReferences()) {
							IViewPart v = view.getView(false);
							if (v != null && v.getAdapter(IIntroPart.class) != null)
								continue;
							page.hideView(view);
						}
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
			if (e.getTargetException() instanceof OperationCanceledException)
				return;
			e.printStackTrace();
		} catch (InterruptedException e) {
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
			IFolder srcFolder = project.getFolder("src");
			srcFolder.create(true, true, null);
			src = javaProject.getPackageFragmentRoot(srcFolder);
			ArrayList<IClasspathEntry> cp = new ArrayList<>();
			cp.add(JavaCore.newSourceEntry(srcFolder.getFullPath()));
			// https://www.programcreek.com/2011/05/eclipse-jdt-tutorial-java-model/
			cp.add(JavaRuntime.getDefaultJREContainerEntry());
			javaProject.setRawClasspath(cp.toArray(new IClasspathEntry[] {}), project.getFullPath().append("bin"),
					null);
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

	private static IFile createJavaFile(Task task) throws CoreException {
		// https://www.programcreek.com/2011/05/eclipse-jdt-tutorial-java-model/
		IPackageFragment pkg = src.createPackageFragment(task.packageName, true, null);
		ICompilationUnit cu = pkg.createCompilationUnit(task.className + ".java", task.initialSource, true, null);
		return (IFile) cu.getResource();
	}

	private static Task loadTask() throws InterruptedException, FeathersException {
		Result<Task> tasks;
		tasks = Task.find();
		if (tasks.data.size() == 0) {
			throw new RuntimeException("Could not load tasks");
		}
		return tasks.data.get(0);
	}

}
