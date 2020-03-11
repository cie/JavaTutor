package javatutor.ui;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import javatutor.engine.Hint;
import javatutor.engine.Matching.Match;
import javatutor.engine.Task;
import javatutor.tasks.arrays.above_average.AboveAverageTask;

@SuppressWarnings("restriction")
public class JavaTutorEditor extends CompilationUnitEditor {

	private StyledText textWidget;
	private ISourceViewer sourceViewer;
	private HintBubble bubble;

	public JavaTutorEditor() {
	}

	Task task = new AboveAverageTask();

	@Override
	public void reconciled(CompilationUnit ast, boolean forced, IProgressMonitor progressMonitor) {
		super.reconciled(ast, forced, progressMonitor);
		if (sourceViewer == null) {
			// could not find a better place for initialization
			// where source viewer is already present
			setUp();
		}
		try {
			Optional<Hint> hint = task.generateHint(ast);
			setHint(hint);
		} catch (RuntimeException e) {
			MessageDialog.openError(sourceViewer.getTextWidget().getShell(), "Internal error in JavaTutor",
					e.getLocalizedMessage());
		}
	}

	private void setHint(Optional<Hint> hint) {
		sourceViewer.getTextWidget().getDisplay().asyncExec(() -> {
			int maxWidth = textWidget.getTextBounds(0, textWidget.getText().length()-1).width;
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
