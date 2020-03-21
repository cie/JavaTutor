package javatutor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Assert;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite.SuiteClasses;

import javatutor.engine.Hint;
import javatutor.engine.HintGenerator;
import javatutor.engine.Matching;

@RunWith(HintGenerationTest.class)
@SuiteClasses({})
public class HintGenerationTest extends Runner {
	private class Task {
		private String packageName;
		private String className;

		Task(String packageName, String className) {
			this.packageName = packageName;
			this.className = className;
		}

		List<Test> tests = new ArrayList<Test>();
		public Description description;

		public void add(Test test) {
			tests.add(test);
		}
	}

	private class Test {
		String src;
		String expected;
		String packageName;
		String className;
		String testClassName;
		Description description;

		Test(File src, File expected, String packageName, String className, String testClassName) {
			try {
				this.src = new String(Files.readAllBytes(Paths.get(src.getAbsolutePath())));
				this.expected = new String(Files.readAllBytes(Paths.get(expected.getAbsolutePath())));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			this.packageName = packageName;
			this.className = className;
			this.testClassName = testClassName;
		}

		public Optional<Hint> generateHint(CompilationUnit ast) {
			try {
				String fullyQualifiedName = this.packageName + "." + this.className;
				Class<?> c = getClass().getClassLoader().loadClass(fullyQualifiedName);
				if (!HintGenerator.class.isAssignableFrom(c)) {
					throw new RuntimeException(
							this.packageName + "." + this.className + " must implement HintGenerator");
				}
				@SuppressWarnings("unchecked")
				Class<? extends HintGenerator> clazz = (Class<? extends HintGenerator>) c;
				HintGenerator gen = clazz.newInstance();
				return gen.generateHint(ast);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

	}

	Pattern TEST_FILE_NAME = Pattern.compile("(([a-zA-Z0-9_]+?)_.*)\\.java");
	private Map<String, Task> tasks;
	private Description all;

	public HintGenerationTest(Class<?> _class) {
		tasks = new HashMap<>();
		for (File f : new File("tasks/javatutor/tasks/arrays").listFiles()) {
			if (f.isDirectory())
				continue;

			Matcher m = TEST_FILE_NAME.matcher(f.getName());
			if (m.matches()) {
				String taskName = m.group(2);
				Task task = tasks.get(taskName);
				if (task == null)
					tasks.put(taskName, task = new Task("javatutor.tasks.arrays", taskName));
				File expected = new File(f.getParentFile(), m.group(1) + ".hint");
				if (!expected.exists())
					throw new RuntimeException(expected.getAbsolutePath() + " does not exist.");
				task.add(new Test(f, expected, "javatutor.tasks.arrays", taskName, m.group(1)));
			}
		}
	}

	@Override
	public Description getDescription() {
		all = Description.createSuiteDescription("All tasks");
		for (Entry<String, Task> e : tasks.entrySet()) {
			Description task = Description.createSuiteDescription(e.getKey(), e.getKey());
			e.getValue().description = task;
			all.addChild(task);
			for (Test t : e.getValue().tests) {
				Description test = Description.createTestDescription("asdf",
						t.testClassName.replace(t.className + "_", ""), t.testClassName);
				t.description = test;
				task.addChild(test);
			}
		}
		return all;
	}

	@Override
	public void run(RunNotifier notifier) {
		for (Entry<String, Task> task : tasks.entrySet()) {
			for (Test test : task.getValue().tests) {
				notifier.fireTestStarted(test.description);
				CompilationUnit ast = Matching.parseCompilationUnit(test.src);
				Optional<Hint> hint = test.generateHint(ast);
				if (!hint.isPresent()) {
					notifier.fireTestFailure(new Failure(test.description, new AssertionError("No hint given")));
				} else {
					try {
						Assert.assertEquals(test.expected.trim(), hint.get().source.trim());
					} catch (AssertionError e) {
						notifier.fireTestFailure(new Failure(test.description, e));
					}
				}
				notifier.fireTestFinished(test.description);
			}
		}
	}

}
