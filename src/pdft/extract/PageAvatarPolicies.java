package pdft.extract;

import facets.core.app.SViewer;
import facets.core.app.avatar.*;
import facets.facet.app.FacetAppSurface;
import facets.util.geom.Point;
import facets.util.shade.Shade;
import facets.util.shade.Shades;
import pdft.extract.Coords.BoundsCell;

import static facets.util.shade.Shades.*;
import static pdft.extract.PageAvatarPolicies.ShadeState.*;

final class PageAvatarPolicies extends AvatarPolicies{
    private static final boolean TEST =false;
    private final FacetAppSurface app;
    PageAvatarPolicies(FacetAppSurface app) {
        this.app = app;
    }
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
        PdfViewable viewable = (PdfViewable) app.findActiveContent().contentFrame();
        DocTexts texts=viewable.texts;
        int pageAt= viewable.viewPageAt;
        return graphics -> {
            p.bar(0, 0, view.showWidth(), view.showHeight(), Shades.white, false)
                    .paintInGraphics(graphics);
            new PagePainters(texts, pageAt, app).newPainter(2, true)
                    .paintInGraphics(graphics);
        };
    }
    @Override
    public AvatarPolicy viewerPolicy(SViewer viewer, AvatarContent content, PainterSource p) {
        PlaneView view = (PlaneView) viewer.view();
        double w = view.showWidth() ;
        double h = view.showHeight() ;
        return new AvatarPolicy() {
            @Override
            public Painter[] newViewPainters(boolean selected, boolean active) {
                return content instanceof BoundsCell ?new Painter[]{
                        p.line(((BoundsCell)content).bounds, blue,0, false),
                }
                : new Painter[]{
                        coordLine((Coord) content, view, selected, false, p),
                };
            }
            @Override
            public Painter[] newPickPainters(Object hit, boolean selected) {
                return new Painter[]{
                        coordLine((Coord) content, view, selected, true, p)
                };
            }
        };
    }
    static Painter coordLine(Coord c, PlaneView view, boolean selected, boolean picked, PainterSource p) {
        ShadeState state = chooseViewState(selected, picked);
        return p.line(c.newViewLine(view),c.isLive()? state.shade:blue,0, !picked);
    }
    @Override
    public DragPolicy dragPolicy(AvatarView view, AvatarContent[] content, Object hit, PainterSource p) {
        PageRenderView page = (PageRenderView) view;
        Coord then= (Coord) content[0];
        return new DragPolicy() {
            @Override
            public Painter[] newDragPainters(Point anchorAt, Point dragAt) {
                Coord now = newUpdate(anchorAt, dragAt);
                return new Painter[]{
                        p.line(now.newViewLine(page), Dragging.shade, 0, false)
                };
            }
            @Override
            public Object[] newDragDropEdits(Point anchorAt, Point dragAt) {
                then.setAt(newUpdate(anchorAt, dragAt).getAt());
                return new Object[]{then};
            }
            private Coord newUpdate(Point anchorAt, Point dragAt) {
                boolean forX = then.forX;
                return new Coord(forX, then.getAt() +
                        (forX ? dragAt.x() - anchorAt.x() : dragAt.y() - anchorAt.y()));
            }
        };
    }
}

