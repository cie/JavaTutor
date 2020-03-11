package javatutor.ui;

import java.text.MessageFormat;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.widgets.Composite;

public class SuggestionBubble extends Composite {

	private Browser suggestion;
	final int margin = 6;
	final int radius = 10;
	final int fontSize = 10;
	final int tailLength = 30;
	final int tailWidth = 18;
	final Color BG = new Color(null, 255, 245, 230);

	protected final class BubblePainter implements PaintListener {
		private Font font;
		public BubblePainter() {
			font = getDisplay().getSystemFont();
		}
		@Override
		public void paintControl(PaintEvent e) {
			GC gc = e.gc;
			gc.setForeground(new Color(null, 0, 0, 0));
			gc.setBackground(BG);
			gc.fillPath(bubblePath(tailLength, 0, radius, getSize().x - tailLength - 1, getSize().y - 2));
			gc.drawPath(bubblePath(tailLength, 0, radius, getSize().x - tailLength - 1, getSize().y - 2));
			gc.setFont(font);
		}

		private Path bubblePath(int x, int y, int r, int w, int h) {
			boolean tailOnLeft = true;
			Path path = new Path(null);
			path.moveTo(x+r, y);
			path.lineTo(x+w-r, y);
			path.addArc(x + w - 2 * r, y, 2 * r, 2 * r, 90, -90);
			path.lineTo(x + w, y + h - r);
			path.addArc(x + w - 2 * r, y + h - 2 * r, 2 * r, 2 * r, 0, -90);
			path.lineTo(x + r, y + h);
			path.addArc(x, y + h - 2 * r, 2 * r, 2 * r, -90, -90);
			if (tailOnLeft) {
				path.lineTo(x, y + r + (h - 2 * r) * 25 / 100 + tailWidth);
				path.lineTo(x - tailLength, y + r + (h - 2 * r) * 25 / 100 + tailWidth / 2);
				path.lineTo(x, y + r + (h - 2 * r) * 25 / 100);
				path.lineTo(x, r);
			} else {
				path.lineTo(x, r);
			}
			path.addArc(x, y, 2 * r, 2 * r, 180, -90);
			return path;
		}
	}

	public SuggestionBubble(Composite parent, int style) {
		super(parent, style);

		setSize(320, 250);

		setBackground(new Color(null, 0, 0, 0, 0));
		addPaintListener(new BubblePainter());

		suggestion = new Browser(this, SWT.NONE);
		suggestion.setBackground(null);
		suggestion.setBackgroundMode(SWT.INHERIT_FORCE);
	}

	public void setHintText(Optional<String> html) {
		if (!html.isPresent()) {
			setVisible(false);
			return;
		}
		setVisible(true);
		suggestion.setText(MessageFormat.format(
				"<style>body'{'background: rgb({0},{1},{2})'; margin: 0; font-size: {3}px}'</style>{4}",
				BG.getRed(),
				BG.getGreen(), BG.getBlue(), fontSize, html.get()));
	}

	@Override
	public void setSize(int width, int height) {
		if (suggestion == null)
			return;
		super.setSize(width, height);
		int marginLeft = margin + tailLength;
		int marginTop = margin;
		int marginRight = margin;
		int marginBottom = margin;
		suggestion.setLocation(marginLeft, marginTop);
		suggestion.setSize(width - marginLeft - marginRight, height - marginTop - marginBottom);
	}

}
