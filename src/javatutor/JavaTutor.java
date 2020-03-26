package javatutor;

import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
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

import javatutor.feathers.model.Student;
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
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	//private static Task task;
	// TODO Move this to editor

	private static IWorkbenchPage page;

	private static ICompilationUnit cu;

	private static List<Task> tasks;

	private static int taskIndex = -1;

	private static JavaTutorIntro intro;

	private static Student student;

	public static Student getStudent() {
		return student;
	}

	public static Task getTask() {
		return task;
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
		JavaTutor.intro = intro;
		try {
			intro.getIntroSite().getShell().getDisplay().syncExec(() -> {
				page = intro.getIntroSite().getWorkbenchWindow().getActivePage();
			});
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(monitor -> {
				SubMonitor progress = SubMonitor.convert(monitor, "Open task", 100);
				try {
					registerStudent();
					task = loadTask(progress.split(30));
					setUpWorkspace(progress.split(40));
					openEditor(createJavaFile(task));
					progress.split(10);
					setUpWindow(task);
				} catch (FeathersException | CoreException | Done e) {
					throw new InvocationTargetException(e);
				}
			});
		} catch (InvocationTargetException e1) {
			Throwable e = e1.getTargetException();
			handle(e);
		} catch (RuntimeException e) {
			handle(e);
		} catch (InterruptedException e) {
		}
	}

	private static void handle(Throwable e) {
		if (e instanceof InvocationTargetException) {
			handle(((InvocationTargetException) e).getTargetException());
			return;
		}
		if (e instanceof OperationCanceledException || e instanceof InterruptedException)
			return;
		if (e instanceof Done) {
			Display.getDefault().syncExec(() -> {
				MessageDialog.openInformation(null, "All tasks done", "You have finished all tasks. Well done!");
			});
			return;
		}
		e.printStackTrace();
		Display.getDefault().syncExec(() -> {
			MessageDialog.openError(null, "Error", e.getClass().getSimpleName() + ": " + e.getMessage());
		});
	}

	private static void openEditor(final IFile file) {
		page.getWorkbenchWindow().getShell().getDisplay().syncExec(() -> {
			try {
				page.openEditor(new FileEditorInput(file), JavaTutorEditor.ID);
			} catch (PartInitException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private static void setUpWindow(Task task) {
		page.getWorkbenchWindow().getShell().getDisplay().syncExec(() -> {
			for (IViewReference view : page.getViewReferences()) {
				IViewPart v = view.getView(false);
				if (v != null && v.getAdapter(IIntroPart.class) != null) {
					continue;
				}
				if (view.getId().equals("org.eclipse.ui.console.ConsoleView")) {
					continue;
				}
				page.hideView(view);
			}
			try {
				page.showView("org.eclipse.ui.console.ConsoleView");
			} catch (PartInitException e1) {
				e1.printStackTrace();
			}
			intro.setText("<h2>" + task.title + "</h2><p>" + task.instructions
					+ "</p><p><button onclick='window.location=\"javatutor:run\"'>Run code</button>&nbsp;&nbsp;"
					+ "<button onclick='window.location=\"javatutor:nextTask\"'>Next task</button></p>");
			PlatformUI.getWorkbench().getIntroManager().setIntroStandby(intro, true);
		});
	}

	private static void setUpWorkspace(SubMonitor progress) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		project = root.getProject(PROJECT_NAME);

		if (!project.exists()) {
			createProject(progress.split(40));
		}
		progress.setWorkRemaining(40);
	}

	private static void createProject(SubMonitor progress) {
		progress.beginTask("Create project", 100);
		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IProjectDescription desc = workspace.newProjectDescription(PROJECT_NAME);
			desc.setNatureIds(new String[] { JavaCore.NATURE_ID });
			project.create(null);
			project.open(null);
			project.setDescription(desc, null);
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
			IFolder binFolder = project.getFolder("bin");
			if (!binFolder.exists()) {
				binFolder.create(true, true, null);
			}
			javaProject.setOutputLocation(binFolder.getFullPath(), null);
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

	private static void registerStudent() throws InterruptedException, FeathersException {
		student = Student.create();
		tasks = student.tasks;
	}

	private static Task loadTask(SubMonitor progress) throws InterruptedException, FeathersException, Done {
		if (tasks == null)
			tasks = Task.find().data;
		if (tasks.size() == 0) {
			throw new RuntimeException("No tasks have been assigned.");
		}
		if (taskIndex == tasks.size() - 1) {
			throw new Done();
		}
		return tasks.get(++taskIndex);
	}

	private static IFile createJavaFile(Task task) throws CoreException {
		// https://www.programcreek.com/2011/05/eclipse-jdt-tutorial-java-model/
		IPackageFragment pkg = src.createPackageFragment(task.packageName, true, null);
		cu = pkg.createCompilationUnit(task.className + ".java", task.initialCode, true, null);
		return (IFile) cu.getResource();
	}

	public static void run() {
		try {
			PlatformUI.getWorkbench().saveAllEditors(false);
			DebugPlugin plugin = DebugPlugin.getDefault();
			String cfgName = "current JavaTutor task";
			ILaunchManager lm = plugin.getLaunchManager();
			ILaunchConfigurationType t = lm.getLaunchConfigurationType(ID_JAVA_APPLICATION);
			ILaunchConfigurationWorkingCopy wc;
			wc = t.newInstance(null, cfgName);
			wc.setAttribute(ATTR_PROJECT_NAME, project.getName());
			wc.setAttribute(ATTR_MAIN_TYPE_NAME, cu.findPrimaryType().getFullyQualifiedName());
			ILaunchConfiguration config = wc.doSave();
			config.launch(ILaunchManager.DEBUG_MODE, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public static void nextTask() {
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(monitor -> {
				SubMonitor progress = SubMonitor.convert(monitor, "Open task", 100);
				try {
					task = loadTask(SubMonitor.convert(progress));
					openEditor(createJavaFile(task));
					setUpWindow(task);
				} catch (FeathersException | CoreException | Done e) {
					throw new InvocationTargetException(e);
				}
			});
		} catch (Exception e) {
			handle(e);
		}
	}

}
