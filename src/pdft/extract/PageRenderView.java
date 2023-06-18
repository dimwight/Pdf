package pdft.extract;

import applicable.textart.TextArt;
import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.PlaneViewWorks;
import facets.core.superficial.app.SSelection;
import facets.util.geom.Vector;
import facets.util.shade.Shades;
import org.apache.pdfbox.pdmodel.PDPage;

import java.awt.*;

import static facets.facet.kit.avatar.SwingPainterSource.newDummy;

final class PageRenderView extends PlaneViewWorks {
    private Coord selected;
    private AvatarContent[] contents = new AvatarContent[]{
        new Coord(true, 0f),
                selected =
                        new Coord(false, 0f),
                new Coord(true, 200f),
                new Coord(false, 100f),
    };
    PageRenderView(PageAvatarPolicies avatars) {
        super("Re&ndering", 0, 0, new Vector(0, 0), avatars);
    }
    @Override
    public Object backgroundStyle() {
        return Shades.gray;
    }

    void setToPageRotation(PDPage page) {
        boolean rotated = page.findRotation() != 0;
        Dimension size = page.findMediaBox().createDimension();
        double across = rotated ? size.height : size.width, down = rotated ? size.width
                : size.height;
        setShowValues(across + PageAvatarPolicies.MARGINS, down + PageAvatarPolicies.MARGINS, new Vector(PageAvatarPolicies.MARGINS, PageAvatarPolicies.MARGINS).scaled(0.5), 1);
    }

    public SSelection newViewerSelection() {
        return new SSelection() {
            @Override
            public Object content() {
                  return contents;
            }

            @Override
            public Object single() {
                return selected;
            }

            @Override
            public Object[] multiple() {
                return new Object[]{selected};
            }
        };
    }

    static class Coord implements AvatarContent {
        final boolean forX;
        private static int ids;
        private final int id;
        float at;
        Coord(boolean forX, float at) {
            this.forX = forX;
            this.at = at;
            id = ids++;
        }
    }
}
