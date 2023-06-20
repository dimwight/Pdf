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
    public static final boolean TEST = true;
    private final Map<PDPage, Coords> pageCoords;
    PageRenderView(Map<PDPage, Coords> pageCoords, PageAvatarPolicies avatars) {
        super("Re&ndering", 0, 0, new Vector(0, 0), avatars);
        this.pageCoords = pageCoords;
    }
    @Override
    public Object backgroundStyle() {
        return Shades.gray;
    }

    Coords coords;
    void setToPageRotation(PDPage page) {
        coords = pageCoords.get(page);
        if(coords==null)coords=pageCoords.put(page, new Coords());
        boolean rotated = page.findRotation() != 0;
        Dimension size = page.findMediaBox().createDimension();
        double across = rotated ? size.height : size.width, down = rotated ? size.width
                : size.height;
        setShowValues(across, down, new Vector(0,0), 1);
    }

    public SSelection newViewerSelection() {
        return new SSelection() {
            @Override
            public Object content() {
                  return (TEST ?content_:coords.getAll())
                          .toArray(new AvatarContent[0]);
            }

            @Override
            public Object single() {
                return multiple()[0];
            }

            @Override
            public Object[] multiple() {
                return multipleNext;
            }
        };
    }

    private List<AvatarContent> content_ =
            Arrays.asList(
            false ?
            new AvatarContent[]{
                    new Coord(true, 10f),
                    new Coord(true, 200f),
                    new Coord(true, 400f),
            } : new AvatarContent[]{
            new Coord(true, 10f),
            new Coord(false, 10f),
            new Coord(true, 200f),
            new Coord(false, 100f),
    });
    Object[] multipleNext = {
            TEST ? content_.get(1) : coords.getAll().get(0)
            /*
           content[0],
           content[2],
           content[3],
  */};
}
