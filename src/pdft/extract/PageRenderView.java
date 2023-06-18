package pdft.extract;

import applicable.TextAvatar;
import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PlaneViewWorks;
import facets.core.superficial.app.SSelection;
import facets.util.geom.Point;
import facets.util.geom.Vector;
import facets.util.shade.Shades;
import org.apache.pdfbox.pdmodel.PDPage;

import java.awt.*;

final class PageRenderView extends PlaneViewWorks {

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

    private TextAvatar dummy = new TextAvatar() {
        @Override
        public String getText() {
            return "Hi";
        }
        @Override
        public BoundsBox getBounds() {
            return new BoundsBox(0, 0, 0, 0);
        }
        @Override
        public Painter newViewPainter(boolean selected) {
            return null;
        }
        @Override
        public Painter[] newPickPainters(boolean selected) {
            return new Painter[0];
        }
        @Override
        public Object checkCanvasHit(Point canvasAt, double hitGap) {
            return null;
        }
    };
    public SSelection newViewerSelection() {
        return new SSelection() {
            @Override
            public Object content() {
                return new AvatarContent[]{
                        dummy
                };
            }

            @Override
            public Object single() {
                return null;
            }

            @Override
            public Object[] multiple() {
                return new Object[0];
            }
        };
    }
}
