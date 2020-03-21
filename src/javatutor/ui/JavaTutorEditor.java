package javatutor.ui;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.feathersjs.client.service.FeathersService.FeathersCallback;

import javatutor.engine.Hint;
import javatutor.engine.HintGenerator;
import javatutor.engine.Matching.Match;
import javatutor.feathers.App;
import javatutor.feathers.model.Event;
import javatutor.tasks.arrays.AboveBelowAverage;

@SuppressWarnings("restriction")
public class JavaTutorEditor extends CompilationUnitEditor {

	private StyledText textWidget;
	private ISourceViewer sourceViewer;
	private HintBubble bubble;

	public JavaTutorEditor() {
	}

	HintGenerator task = new AboveBelowAverage();
	Timer hintTimer = null;
	private Optional<Hint> currentHint = empty();

	public static final String ID = "JavaTutor.editor";

	@Override
	public void reconciled(CompilationUnit ast, boolean forced, IProgressMonitor monitor) {
		super.reconciled(ast, forced, monitor);
		if (sourceViewer == null) {
			// could not find a better place for initialization
			// where source viewer is already present
			setUp();
		}
		scheduleHint(ast);
		snapshot();
	}

	private void snapshot() {
		snapshot(null);
	}

	private void snapshot(String internalError) {
		Event e = new Event();
		sourceViewer.getTextWidget().getDisplay().syncExec(( ) -> {
			e.source = sourceViewer.getTextWidget().getText();
		});
		e.hintSource = currentHint.map(hint -> hint.source).orElse(null);
		e.button = null;
		e.internalError = internalError;
		e.timestamp = System.currentTimeMillis();
		App.get().service("events", Event.class).create(e, new FeathersCallback<Event>() {
			@Override
			public void onSuccess(Event t) {
			}

			@Override
			public void onError(String errorMessage) {
				System.err.println(errorMessage);
			}
		});
	}

	private void scheduleHint(CompilationUnit ast) {
		if (hintTimer != null) {
			hintTimer.cancel();
		}
		Optional<Hint> hint = task.generateHint(ast);
		if (hint.isPresent()) {
			if (!hint.equals(currentHint)) {
				setHint(empty());
			}
			hintTimer = new Timer();
			hintTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						setHint(hint);
						snapshot();
					} catch (RuntimeException e) {
						setHint(empty());
						e.printStackTrace();
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						snapshot(sw.toString());
					}
				}
			}, hint.get().delay);
		} else {
			setHint(empty());
		}

	}

	private void setHint(Optional<Hint> hint) {
		currentHint = hint;
		sourceViewer.getTextWidget().getDisplay().asyncExec(() -> {
			int maxWidth = textWidget.getTextBounds(0, textWidget.getText().length() - 1).width;
			if (!hint.isPresent()) {
				bubble.setHintText(empty());
				return;
			}
			Optional<Match> studentMatch = hint.get().studentMatch;
			Rectangle rect;
			if (studentMatch.isPresent()) {
				ASTNode node = studentMatch.get().matchedNode; // TODO show good example code!?!
				rect = sourceViewer.getTextWidget().getTextBounds(node.getStartPosition(),
						node.getStartPosition() + node.getLength());
			} else {
				Point sel = sourceViewer.getTextWidget().getSelection();
				rect = sourceViewer.getTextWidget().getTextBounds(sel.x, sel.y);
			}
			bubble.setLocation(maxWidth + 70, Math.max(50, rect.y - 20));
			String message = hint.get().message;
			bubble.setSize(320, Math.max(50, (message.length() / 40 + 1) * 30 + 10));
			bubble.setHintText(of(message));
		});
	}

	private void setUp() {
		sourceViewer = getSourceViewer();
		sourceViewer.getTextWidget().getDisplay().syncExec(() -> {
			textWidget = sourceViewer.getTextWidget();
			bubble = new HintBubble(textWidget, 0);
		});
	}

}
