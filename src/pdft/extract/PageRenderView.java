package pdft.extract;

import facets.core.app.avatar.PlaneViewWorks;
import facets.core.superficial.app.SSelection;
import facets.util.geom.Vector;
import facets.util.shade.Shades;
import org.apache.pdfbox.pdmodel.PDPage;

import java.awt.*;
import java.util.Map;

final class PageRenderView extends PlaneViewWorks {
    private final Map<Integer, Coords> pageCoords;
    Coords coords;
    Coord selection;
    void defineSelection(Coord selectionNow) {
        if(selectionNow.isJunk()) {
            coords.remove(selection);
            selectionNow=null;
        }
        else if(selectionNow!=null&&
                !selection.isLive()){
            coords.add(selection.forX, this);
        }
        selection = selectionNow;
        coords.sortAll();
    }
   public SSelection newViewerSelection() {
        return new SSelection() {
            @Override
            public Object content() {
                return coords.getAll();
            }
            @Override
            public Object single() {
                return true?null: multiple()[0];
            }

            @Override
            public Object[] multiple() {
                return new Object[]{
                        selection != null ? selection
                                :(selection= (Coord) coords.getAll()[0])
                };
            }
        };
    }
    PageRenderView(Map<Integer, Coords> pageCoords, PageAvatarPolicies avatars) {
        super("Re&nder", 0, 0, new Vector(0, 0), avatars);
        this.pageCoords = pageCoords;
    }
    void setToPage(PDPage page, int at) {
        boolean rotated = page.findRotation() != 0;
        Dimension size = page.findMediaBox().createDimension();
        double across = rotated ? size.height : size.width, down = rotated ? size.width
                : size.height;
        setShowValues(across, down, new Vector(0,0), 1);
        coords = pageCoords.get(at);
        if(coords==null)pageCoords.put(at,coords=new Coords(this));
    }
    @Override
    public int pickHitPixels() {
        return 20;
    }
    @Override
    public Object backgroundStyle() {
        return Shades.gray;
    }
}
