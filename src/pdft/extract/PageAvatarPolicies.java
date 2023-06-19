package pdft.extract;

import facets.core.app.SViewer;
import facets.core.app.avatar.*;
import facets.util.geom.Point;
import facets.util.shade.Shade;
import facets.util.shade.Shades;

import static facets.util.shade.Shades.*;
import static pdft.extract.PageAvatarPolicies.ShadeState.*;

final class PageAvatarPolicies extends AvatarPolicies{
    public static final boolean TEST =false;
    enum ShadeState {
        Plain (TEST ?red:red),
        Selected (TEST ?yellow:magenta.darker()),
        Picked (TEST ?blue:red.darker()),
        Dragging (TEST ?blue:blue.darker());
        final Shade shade;
        ShadeState(Shade shade) {
            this.shade = shade;
        }
        static ShadeState chooseViewState(boolean selected, boolean picked) {
            return picked ? Picked : selected ? Selected : Plain;
        }
    }
    @Override
    public Painter getBackgroundPainter(SViewer viewer, PainterSource p) {
        PlaneView view = (PlaneView) viewer.view();
        return p.bar(0,0,view.showWidth(),view.showHeight(),
                Shades.white,false);
    }
    @Override
    public AvatarPolicy viewerPolicy(SViewer viewer, AvatarContent content, PainterSource p) {
        PlaneView view = (PlaneView) viewer.view();
        double w = view.showWidth() ;
        double h = view.showHeight() ;
        Coord coord= (Coord) content;
        return new AvatarPolicy() {
            @Override
            public Painter[] newViewPainters(boolean selected, boolean active) {
                return new Painter[]{
                        coordLine(coord, view, selected, false, p),
                };
            }
            @Override
            public Painter[] newPickPainters(Object hit, boolean selected) {
                return new Painter[]{
                        coordLine(coord, view, selected, true, p)
                };
            }
        };
    }
    static Painter coordLine(Coord c, PlaneView view, boolean selected, boolean picked, PainterSource p) {
        ShadeState state = chooseViewState(selected, picked);
        return p.line(c.newViewLine(view),state.shade,0, !picked);
    }
    @Override
    public DragPolicy dragPolicy(AvatarView view, AvatarContent[] content, Object hit, PainterSource p) {
        PlaneView page = (PlaneView) view;
        Coord then= (Coord) content[0];
        return new DragPolicy() {
            @Override
            public Painter[] newDragPainters(Point anchorAt, Point dragAt) {
                Coord now;
                now = newUpdate(anchorAt, dragAt);
                return new Painter[]{
                        p.line(now.newViewLine(page), Dragging.shade, 0, false)
                };
            }
            @Override
            public Object[] newDragDropEdits(Point anchorAt, Point dragAt) {
                return new Object[]{newUpdate(anchorAt, dragAt)};
            }
            private Coord newUpdate(Point anchorAt, Point dragAt) {
                boolean forX = then.forX;
                return new Coord(forX, then.at +
                        (forX ? dragAt.x() - anchorAt.x() : dragAt.y() - anchorAt.y()));
            }
        };
    }
}

