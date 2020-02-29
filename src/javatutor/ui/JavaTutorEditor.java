package javatutor.ui;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;

@SuppressWarnings("restriction")
public class JavaTutorEditor extends CompilationUnitEditor {

	private StyledText textWidget;
	private ISourceViewer sourceViewer;
	private SuggestionBubble bubble;


	public JavaTutorEditor() {
	}
	
	
	@Override
	public void aboutToBeReconciled() {
		super.aboutToBeReconciled();
		if (sourceViewer == null) {
			setUp();
		}

		bubble.setSuggestion("<p>Hi</p>");
	}


	private void setUp() {
		// could not find a better place for initialization
		// where source viewer is already present
		sourceViewer = getSourceViewer();
		textWidget = sourceViewer.getTextWidget();
		bubble = new SuggestionBubble(textWidget, 0);
		bubble.setLocation(50, 59);
	}
	
}
