package pdft.extract;

import applicable.textart.TextArt;
import facets.core.app.SView;
import facets.core.app.SViewer;
import facets.core.app.avatar.*;
import facets.util.shade.Shade;
import facets.util.shade.Shades;

import static facets.util.shade.Shades.red;

final class PageAvatarPolicies extends AvatarPolicies{
    public static final int MARGINS = -1;
    @Override
    public Painter getBackgroundPainter(SViewer viewer, PainterSource p) {
        PageRenderView view = (PageRenderView) viewer.view();
        return p.bar(0,0,view.showWidth()-MARGINS,view.showHeight()-MARGINS,
                Shades.white,false);
    }
    @Override
    public AvatarPolicy viewerPolicy(SViewer viewer, AvatarContent content, PainterSource p) {
        PageRenderView view = (PageRenderView) viewer.view();
        double across = view.showWidth() - MARGINS;
        double down = view.showHeight() - MARGINS;
        return new AvatarPolicy() {
            @Override
            public Painter[] newViewPainters(boolean selected, boolean active) {
                boolean picked = true;
                return new Painter[]{
                        coordBar(selected, picked)
                };
            }
            private Painter coordBar(boolean selected, boolean picked) {
                Shade shade = selected ?red.brighter(): picked ?red.darker(): red;
                boolean forX = true;
                boolean pickable = !picked;
                int thickness  = 10;
                Painter bar = p.bar(0,
                        0,
                        forX ? thickness : across,
                        forX ? thickness : down,
                        shade,
                        pickable);
                return bar;
            }
            @Override
            public Painter[] newPickPainters(Object hit, boolean selected) {
                return null;
            }
        };
    }
}

