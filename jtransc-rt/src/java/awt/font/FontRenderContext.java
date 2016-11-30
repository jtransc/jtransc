package java.awt.font;

import java.awt.geom.AffineTransform;

public class FontRenderContext {
	protected FontRenderContext() {
	}

	public FontRenderContext(AffineTransform tx, boolean isAntiAliased, boolean usesFractionalMetrics) {
	}

	public FontRenderContext(AffineTransform tx, Object aaHint, Object fmHint) {
	}

	native public boolean isTransformed();

	native public int getTransformType();

	native public AffineTransform getTransform();

	native public boolean isAntiAliased();

	native public boolean usesFractionalMetrics();

	native public Object getAntiAliasingHint();

	native public Object getFractionalMetricsHint();

	native public boolean equals(Object obj);

	native public boolean equals(FontRenderContext rhs);
}
