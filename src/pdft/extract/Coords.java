package pdft.extract;

import facets.core.app.avatar.PlaneView;
import facets.util.StatefulCore;
import facets.util.geom.Line;
import org.apache.pdfbox.util.TextPosition;

import java.util.*;

final class Coords extends StatefulCore {
    private final List<Coord> forX = new ArrayList();
    private final List<Coord> forY = new ArrayList();
    Coords(PlaneView view) {
        super("Coords-" + view.title());
        add(true, view);
        add(false, view);
    }
    void add(boolean forX, PlaneView view) {
        double at = forX ? view.showWidth() / 10 : view.showHeight() / 10;
        (forX ? this.forX : this.forY).add(0, new Coord(forX, at));
        updateStateStamp();
        traceForX("add");
    }
    public void sortAll() {
        Collections.sort(forX);
        Collections.sort(forY);
        traceForX("sortAll");
    }
    void remove(Coord coord) {
        (coord.forX ? forX : forY).remove(coord);
        traceForX("remove");
        updateStateStamp();
    }
    private void traceForX(String from) {
        trace("."+ from + ": forX=", forX.toArray(new Coord[0]));
    }
    Coord[] getAll() {
        while (forX.size()>3&&!forX.get(3).isLive())forX.remove(3);
        while (forY.size()>3&&!forY.get(3).isLive())forY.remove(3);
        ArrayList<Coord> all = new ArrayList(forX);
        all.addAll(forY);
        return all.toArray(new Coord[0]);
    }
    String constructTable(List<TextPosition> chars) {
        sortAll();
        List<Coord> useX = jumpZeroes(forX);
        List<Coord> useY = jumpZeroes(forY);
        if (useX.size()<2||useY.size()<2)return "[no coords]";
        if (false) return "[table]";
        ListIterator<Coord> forX_ = useX.listIterator();
        ListIterator<Coord> forY_ = useY.listIterator();
        do{
            Coord left = forX_.next();
            Coord right = forX_.next();
            do {
                Coord top = forY_.next();
                Coord  bottom  = forY_.next();
                Line values = new Line(new double[]{left.getAt(), right.getAt(),
                        top.getAt(), bottom.getAt()});
            } while (forY_.hasNext());
        }while (forX_.hasNext());

        return "[table]";
    }
    private List<Coord> jumpZeroes(List<Coord> zeroed) {
        ArrayList<Coord> jumped = new ArrayList<>();
        Iterator<Coord> i = zeroed.iterator();
        while (i.hasNext()) {
            Coord next = i.next();
            if (next.isLive())
                jumped.add(next);
        }
        return jumped;
    }
}
