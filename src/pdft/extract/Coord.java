package pdft.extract;

import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.PlaneView;
import facets.util.Tracer;
import facets.util.geom.Line;

class Coord //extends Tracer
        implements AvatarContent {
    final boolean forX;
    private static int ids;
    final int id;
    double at;
    Coord(boolean forX, double at) {
        this.forX = forX;
        this.at = at;
        id = ids++;
    }
    Line newViewLine(PlaneView view) {
        return new Line(new double[]{
                forX ? at : 0,
                forX ? 0 : at,
                forX ? at : view.showWidth(),
                forX ? view.showHeight() : at,
        });
    }
}
