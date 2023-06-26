package pdft.extract;

import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.PlaneViewWorks;
import facets.core.superficial.app.SSelection;
import facets.util.geom.Vector;
import facets.util.shade.Shades;
import org.apache.pdfbox.pdmodel.PDPage;
import pdft.extract.Coord.Coords;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

final class PageRenderView extends PlaneViewWorks {
    private final Map<Integer, Coords> pageCoords;
    Coords coords;
    private List<AvatarContent> test = Arrays.asList(
            new AvatarContent[]{
                    new Coord(true, 10f),
                    new Coord(false, 10f),
                    new Coord(true, 200f),
                    new Coord(false, 100f),
            });
    private static final boolean TEST =false;
    Coord selectionNow;
    void defineSelection(Coord definition) {
        if(definition.isJunk()) {
            coords.remove(selectionNow);
            definition=null;
        }
        else if(selectionNow!=null&&
                !selectionNow.isLive()){
            coords.add(selectionNow.forX, this);
        }
        selectionNow = definition;
    }
   public SSelection newViewerSelection() {
        return new SSelection() {
            @Override
            public Object content() {
                return (TEST ? test :coords.getAll())
                        .toArray(new AvatarContent[0]);
            }

            @Override
            public Object single() {
                return true?null: multiple()[0];
            }

            @Override
            public Object[] multiple() {
                return new Object[]{
                        selectionNow == null ? coords.getAll().get(0)
                                : selectionNow
                };
            }
        };
    }
    PageRenderView(Map<Integer, Coords> pageCoords, PageAvatarPolicies avatars) {
        super("Re&nder", 0, 0, new Vector(0, 0), avatars);
        this.pageCoords = pageCoords;
    }
    void setToPage(PDPage page) {
        boolean rotated = page.findRotation() != 0;
        Dimension size = page.findMediaBox().createDimension();
        double across = rotated ? size.height : size.width, down = rotated ? size.width
                : size.height;
        setShowValues(across, down, new Vector(0,0), 1);
        int at = page.getParent().getKids().indexOf(page);
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
