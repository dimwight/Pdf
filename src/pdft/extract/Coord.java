package pdft.extract;

import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.PlaneView;
import facets.util.Util;
import facets.util.geom.Line;

import java.io.Serializable;

final class Coord implements AvatarContent, Serializable ,Comparable<Coord>{
    static final int DIVISOR = 25;
    final boolean forX;
    private final double pageSize;
    private double at;
    private static int ids;
    final int id=ids++;
    @Override
    public String toString() {
        return (""+ Util.sf(at- margin())).replaceAll("\\..*$"," ");
    }
    private double margin() {
        return pageSize/DIVISOR;
    }
    private Coord(boolean forX, double pageSize, double at) {
        this.forX = forX;
        this.pageSize = pageSize;
        this.at = at;
    }
    Coord(boolean forX, double pageSize) {
        this(forX,pageSize,pageSize / DIVISOR);
    }
    Coord shifted(double shift) {
        return new Coord(forX,pageSize,at+shift);
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
        return at> margin();
    }
    boolean isJunk() {
       return at< margin();
    }
    @Override
    public int compareTo(Coord o) {
        return o.at<=at?1:-1;
    }
}
