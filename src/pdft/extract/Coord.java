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
    private double at;
    private final double atFirst;
    private static int ids;
    final int id=ids++;
   Coord(boolean forX, double at) {
        this.forX = forX;
        this.setAt(at);
        atFirst=at;
    }
    Line newViewLine(PlaneView view) {
        return new Line(new double[]{
                forX ? getAt() : 0,
                forX ? 0 : getAt(),
                forX ? getAt() : view.showWidth(),
                forX ? view.showHeight() : getAt(),
        });
    }
    double getAt() {
        return at;
    }
    void setAt(double at) {
        this.at = at;
    }
    boolean isLive() {
        return at>atFirst;
    }
    boolean isJunk() {
       return at<atFirst;
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
        void remove(Coord coord) {
            (coord.forX?forX:forY).remove(coord);
        }
    }
}
