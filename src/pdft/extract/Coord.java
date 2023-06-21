package pdft.extract;

import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.PlaneView;
import facets.util.geom.Line;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class Coord implements AvatarContent, Serializable {
    final boolean forX;
    double at;
    private static int ids;
    final int id=ids++;
      Coord(boolean forX, double at) {
        this.forX = forX;
        this.at = at;
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
        private final List<Coord> forX=new ArrayList();
        private final List<Coord> forY=new ArrayList();
        private final PlaneView view;
        Coords(PlaneView view){
            this.view = view;
            add(true);
            add(false);
        }
        void add(boolean forX) {
            double at = forX? view.showWidth()/10 : view.showHeight()/20;
            (forX?this.forX:forY).add(new Coord(forX, at));
        }
        List<Coord> getAll() {
            ArrayList<Coord> all = new ArrayList(forX);
            all.addAll(forY);
            return Collections.unmodifiableList(all);
        }
    }
}
