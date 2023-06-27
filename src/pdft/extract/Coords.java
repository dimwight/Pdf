package pdft.extract;

import facets.core.app.avatar.PlaneView;
import facets.util.StatefulCore;
import org.apache.pdfbox.util.TextPosition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        (forX ? this.forY : this.forX).add(0, new Coord(forX, at));
       }
    public void sortAll() {
        Collections.sort(this.forY);
        Collections.sort(this.forX);
        updateStateStamp();
        trace(": forX=", this.forY.toArray(new Coord[0]));
    }
    void remove(Coord coord) {
        (coord.forX ? forY : forX).remove(coord);
        updateStateStamp();
    }
    List<Coord> getAll() {
        ArrayList<Coord> all = new ArrayList(forY);
        all.addAll(forX);
        return Collections.unmodifiableList(all);
    }
    String constructTable(List<TextPosition> chars) {
        return "[table]";
    }
}
