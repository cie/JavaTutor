package javatutor;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;

@SuppressWarnings("restriction")
public class JavaTutorEditor extends CompilationUnitEditor {

	public JavaTutorEditor() {
		
	}
	
	@Override
	public void aboutToBeReconciled() {
		//MessageDialog.openInformation(getSite().getShell(), "Hmm", "reconclie");
		super.aboutToBeReconciled();
		
		ASTParser parser = ASTParser.newParser(AST.JLS13);
		parser.setSource(getInputJavaElement());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setStatementsRecovery(true);
		
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		cu.accept(new ASTVisitor() {
			public boolean visit(ForStatement f) {
				return false;
			}
		});
	}
	
}
