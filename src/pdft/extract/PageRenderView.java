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
                return new AvatarContent[] {
                        newDummy()
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
    public static TextArt newDummy() {
        return new TextArt("Hi",
                20,
                20,
                0,
                Shades.red,
                "",
                200,
                false,
                false,
                "");
    }
}
