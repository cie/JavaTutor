package javatutor.intro;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.part.IntroPart;

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
			if (v != null && v.getAdapter(IIntroPart.class) != null) continue;
			page.hideView(view);
		}
		browser.setText("<h3>Analyze scores</h3>Write a program that reads an unspecified number of "
				+ "scores and determines how many scores are above or equal to the "
				+ "average and how many scores are below the average. "
				+ "Enter a negative number to signify the end of the input. "
				+ "Assume that the maximum number of scores is 100.");

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
