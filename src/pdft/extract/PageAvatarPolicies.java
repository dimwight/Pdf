package pdft.extract;

import facets.core.app.SViewer;
import facets.core.app.avatar.*;
import facets.util.shade.Shade;
import facets.util.shade.Shades;
import pdft.extract.PageRenderView.Coord;

import static facets.util.shade.Shades.*;
import static pdft.extract.PageAvatarPolicies.ShadeState.*;

final class PageAvatarPolicies extends AvatarPolicies{
    public static final boolean TEST = false;
    enum ShadeState {
        Plain (TEST ?gray:red.darker()),
        Selected (TEST ?lightGray:red),
        Picked (TEST ?yellow:red.darker().darker());
        final Shade shade;
        ShadeState(Shade shade) {
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
                trace(" hit = " + hit);
                return new Painter[]{
                        coordBar(coord, selected, true)
                };
            }
            private Painter coordBar(Coord coord, boolean selected, boolean picked) {
                ShadeState state = false? ShadeState.values()[coord.id]:
                        picked?Picked:selected?Selected:Plain;
                trace(" picked=" + picked+" coord=" + coord.id+" state=" + state);
                boolean pickable = !picked;
                int thickness =true?21//+coord.id
                        : 10;
                boolean vertical = coord.forX;
                Painter bar = p.bar(vertical ? coord.at : 0,
                        vertical ?0:coord.at,
                        vertical ? thickness : across,
                        vertical ? down : thickness,
                        state.shade,
                        pickable);
                return bar;
            }
        };
    }
}

