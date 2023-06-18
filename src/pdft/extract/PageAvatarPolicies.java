package pdft.extract;

import facets.core.app.SViewer;
import facets.core.app.avatar.*;
import facets.util.shade.Shade;
import facets.util.shade.Shades;
import pdft.extract.PageRenderView.Coord;

import static facets.util.shade.Shades.red;

final class PageAvatarPolicies extends AvatarPolicies{
    enum ShadeStates{
        Selected (red),
        Plain (red.darker()),
        Picked (red.darker().darker()),
        ;
        final Shade shade;
        ShadeStates(Shade shade) {
            this.shade = shade;
        }
    }
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
        Coord coord= (Coord) content;
        return new AvatarPolicy() {
            @Override
            public Painter[] newViewPainters(boolean selected, boolean active) {
                return new Painter[]{
                        coordBar(coord, selected, false),
                };
            }
            @Override
            public Painter[] newPickPainters(Object hit, boolean selected) {
                return new Painter[]{
                        coordBar(coord, selected, true)
                };
            }
            private Painter coordBar(Coord coord, boolean selected, boolean picked) {
                Shade shade = false?ShadeStates.values()[coord.id].shade:
                        picked?red.darker().darker():selected?red:red.darker();
                boolean pickable = !picked;
                int thickness =true?19+coord.id: 10;
                boolean vertical = coord.forX;
                Painter bar = p.bar(vertical ? coord.at : 0,
                        vertical ?0:coord.at,
                        vertical ? thickness : across,
                        vertical ? down : thickness,
                        shade,
                        pickable);
                return bar;
            }
        };
    }
}

