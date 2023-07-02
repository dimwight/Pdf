package pdft.extract;

import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.PlaneView;
import facets.util.Util;
import facets.util.geom.Line;

import java.io.Serializable;

final class Coord implements AvatarContent, Serializable ,Comparable<Coord>{
    final boolean forX;
    private double at;
    private final double atFirst;
    private static int ids;
    final int id=ids++;
    @Override
    public String toString() {
        return (""+ Util.sf(at-atFirst)).replaceAll("\\..*$"," ");
    }
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
    void setJunk() {
        at=atFirst*-1;
    }
    @Override
    public int compareTo(Coord o) {
        return o.at<=at?1:-1;
    }
}
