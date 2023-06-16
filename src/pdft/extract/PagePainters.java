package pdft.extract;

import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.Pickable;
import facets.facet.app.FacetAppSurface;
import facets.util.SizeEstimable;
import facets.util.Tracer;
import facets.util.geom.Point;

final class PagePainters extends Tracer implements Pickable,AvatarContent,SizeEstimable{

	public PagePainters(PageTexts pageTexts, int pageAt, FacetAppSurface app) {
	}

	@Override
	public Object checkCanvasHit(Point canvasAt, double hitGap) {

		return null;
	}

	@Override
	public long estimateSize() {
		return 0;
	}
}