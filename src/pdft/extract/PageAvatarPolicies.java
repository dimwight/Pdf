package pdft.extract;

import facets.core.app.SViewer;
import facets.core.app.avatar.*;
import facets.util.geom.Line;
import facets.util.shade.Shade;
import facets.util.shade.Shades;
import pdft.extract.PageRenderView.Coord;

import static facets.util.shade.Shades.*;
import static pdft.extract.PageAvatarPolicies.ShadeState.*;

final class PageAvatarPolicies extends AvatarPolicies{
    public static final boolean TEST =false;
    enum ShadeState {
        Plain (TEST ?red:red),
        Selected (TEST ?yellow:magenta.darker()),
        Picked (TEST ?blue:red.darker());
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
        double w = view.showWidth() - MARGINS;
        double h = view.showHeight() - MARGINS;
        Coord coord= (Coord) content;
        return new AvatarPolicy() {
            @Override
            public Painter[] newViewPainters(boolean selected, boolean active) {
                return new Painter[]{
                        coordLine(coord, selected, false),
                };
            }
            @Override
            public Painter[] newPickPainters(Object hit, boolean selected) {
                if (false) trace(" hit = " + hit);
                return new Painter[]{
                        coordLine(coord, selected, true)
                };
            }
            private Painter coordLine(Coord coord, boolean selected, boolean picked) {
                ShadeState state = false? ShadeState.values()[coord.id]:
                        picked?Picked:selected?Selected:Plain;
                if (false) trace(" picked=" + picked+" coord=" + coord.id+" state=" + state);
                boolean pickable = !picked;
                int thickness = 10;
                boolean forX = coord.forX;
                float at = coord.at;
                Painter bar = false?
                        p.bar(forX ? at : 0, forX ? 0 : at, forX ? thickness : w, forX ? h : thickness, state.shade, pickable):
                        p.line(new Line(new double[]{
                                forX ? at : 0,
                                forX ? 0 : at,
                                forX ? at : w,
                                forX ? h : at,
                        }),state.shade,0,pickable);
                return bar;
            }
        };
    }
}

