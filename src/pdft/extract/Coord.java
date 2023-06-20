package pdft.extract;

import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.PlaneView;
import facets.util.geom.Line;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class Coord //extends Tracer
        implements AvatarContent, Serializable {
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
    final static class Coords implements Serializable {
        final List<Coord> forX=new ArrayList();
        final List<Coord> forY=new ArrayList();
        Coords(){
            add(true);
            add(false);
        }
        void add(boolean forX) {
            (forX?this.forX:forY).add(new Coord(forX,10));
        }
         List<Coord> getAll() {
            ArrayList<Coord> all = new ArrayList(forX);
            all.addAll(forY);
            return all;
        }
    }
}
