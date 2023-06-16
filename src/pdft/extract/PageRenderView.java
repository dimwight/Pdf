package pdft.extract;

import facets.core.app.avatar.PlaneViewWorks;
import facets.util.geom.Vector;
import facets.util.shade.Shades;
import org.apache.pdfbox.pdmodel.PDPage;
import pdft.select.PageAvatarPolicies;

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
}
