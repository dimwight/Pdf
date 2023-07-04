package pdft.extract;

import facets.core.app.*;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.superficial.*;
import facets.core.superficial.app.FacetedTarget;
import facets.core.superficial.app.SSelection;
import facets.facet.AreaFacets;
import facets.facet.FacetMaster.Viewer;
import facets.facet.ViewerAreaMaster;
import facets.facet.app.FacetAppSurface;
import facets.util.Debug;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import pdft.PdfCore;
import pdft.extract.DocTexts.TextStyle;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static facets.core.app.ActionViewerTarget.newViewerAreas;
import static facets.facet.AreaFacets.*;
import static facets.facet.FacetFactory.*;
import static pdft.extract.DocTexts.TextStyle.Stream;
import static pdft.extract.DocTexts.TextStyle.Table;
import static pdft.extract.PageHtmlView.*;
import static pdft.extract.PdfViewable.COS_FONTS;
import static pdft.extract.PdfViewable.COS_LAST;

final class PdfContenter extends ViewerContenter {
    public static final String ARG_WRAP = "wrapCode";
    private final FacetAppSurface app;
    private final Map<Integer, Coords> pageCoords = new HashMap();
    private final PageRenderView renderView;
    private File coordData;
    PdfContenter(Object source, FacetAppSurface app) {
        super(source);
        if ((this.app = app) == null) throw new IllegalArgumentException(
                "Null app in " + Debug.info(this));
        renderView = new PageRenderView(pageCoords, new PageAvatarPolicies(app));
    }
    @Override
    protected ViewableFrame newContentViewable(final Object source) {
        File file = (File) source;
        PDDocument doc;
        try {
            doc = new PdfCore(file).document;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String title = file.getName().replace(".pdf", "");
        coordData = new File(title + ".dat");
        if (coordData.exists()) try {
            Object read = new ObjectInputStream(new FileInputStream(coordData))
                    .readObject();
            pageCoords.putAll((Map<Integer, Coords>) read);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return new PdfViewable(title, doc, pageCoords, app) {
            @Override
            public SSelection defineSelection(Object definition) {
                if (definition instanceof Coord) {
                    renderView.defineSelection((Coord) definition);
                    return selection();
                }
                SSelection selection = super.defineSelection(definition);
                List pages = doc.getDocumentCatalog().getAllPages();
                for (Object o : pages) {
                    PDPage page = (PDPage) o;
                    if (page.getCOSDictionary() == definition) {
                        renderView.setToPage(page, pages.indexOf(page));
                        break;
                    }
                }
                return selection;
            }
        };
    }
    @Override
    protected FacetedTarget[] newContentViewers(ViewableFrame viewable) {
        SFrameTarget pages = new SFrameTarget(new CosTreeView(CosTreeView.TreeStyle.Pages));
        AppSpecifier values = app.spec;
        SFrameTarget document = new SFrameTarget(new CosTreeView(CosTreeView.TreeStyle.Document)),
                extract = new PageHtmlView(TextStyle.Extract, values).newFramed(),
                table = new PageHtmlView(Table, values).newFramed(),
                stream = new PageHtmlView(Stream, values).newFramed();
        ;
        SFrameTarget render = new SFrameTarget(renderView);
        return newViewerAreas(viewable,
                new SFrameTarget[]{pages,
                        /*
                        document,
                        stream,
                        */
                        render,
//						extract,
//						stream,
                        table,
                });
    }
    @Override
    protected void attachContentAreaFacets(AreaRoot area) {
        ViewerAreaMaster master = new ViewerAreaMaster() {
            protected ViewerAreaMaster newChildMaster(SAreaTarget area1) {
                final SView view = ((ViewerTarget) area1.activeFaceted()).view();
                final boolean forPage = view instanceof PageRenderView,
                        forTree = view instanceof CosTreeView,
                        forStream = view instanceof PageHtmlView
                                && ((PageHtmlView) view).style == Stream,
                        forTable = view instanceof PageHtmlView
                                && ((PageHtmlView) view).style == Table;
                return new ViewerAreaMaster() {
                    @Override
                    public Viewer viewerMaster() {
                        return forTree ? new CosTreeMaster() : null;
                    }

                    @Override
                    protected String hintString() {
                        return forPage ? HINT_BARE : forStream ? HINT_PANEL_ABOVE : HINT_NONE;
                    }

                    @Override
                    protected SFacet newViewTools(STargeter targeter) {
                        if (!forStream && !forTable) return null;
                        STargeter[] elements = targeter.elements();
                        return forStream ?
                                app.ff.toolGroups(targeter, HINT_NONE,
                                        app.ff.textualField(elements[TARGET_COUNT], 5, HINT_USAGE_FORM),
                                        app.ff.togglingCheckboxes(elements[TARGET_WRAP], HINT_BARE),
                                        true ? null : app.ff.togglingButtons(elements[TARGET_WRAP], HINT_BARE),
                                        app.ff.spacerTall(30)
                                ) : app.ff.toolGroups(targeter, HINT_NONE,
                                app.ff.triggerButtons(elements[TARGET_EXPORT], HINT_BARE)
                        );
                    }
                };
            }
        };
        app.ff.areas().attachViewerAreaPanes(area, master, AreaFacets.PANE_SPLIT_VERTICAL);
    }

    @Override
    public STarget[] lazyContentAreaElements(SAreaTarget area) {
        return app.ff.areas().panesGetTarget(area).elements();
    }
    @Override
    public void areaRetargeted(SContentAreaTargeter root) {
        if (true) return;
        Object activeView = ((SFrameTarget) root.view().target()).framed;
        STargeter[] pane = root.elements(), paneShow = pane[PANE_SHOW].elements(),
                content = root.content().elements();
        boolean extractedOrCodeIsActive = activeView instanceof PageHtmlView,
                renderIsActive = activeView instanceof PageRenderView,
                extractedOrCodePaneSet = ((SToggling) paneShow[4].target()).isSet()
                        || ((SToggling) paneShow[5].target()).isSet(),
                renderPaneSet = ((SToggling) paneShow[3].target()).isSet(),
                noPaneMaximised = pane[PANE_ACTIVE].elements()[PANE_ACTIVE_MAXIMISE].
                        target().isLive();
        content[COS_FONTS].target().setLive(
                extractedOrCodeIsActive || (extractedOrCodePaneSet && noPaneMaximised));
        content[COS_LAST + 1].target().setLive(
                renderIsActive || (renderPaneSet && noPaneMaximised));
    }
    @Override
    public LayoutFeatures newContentFeatures(SContentAreaTargeter area) {
        return new PdfFeatures(app, area);
    }
    @Override
    public void wasRemoved() {
        ArrayList<Integer> empties = new ArrayList<>();
        for (Integer c : pageCoords.keySet())
            if (pageCoords.get(c).isEmpty()) empties.add(c);
        for (Integer c : empties) pageCoords.remove(c);
        try {
            new ObjectOutputStream(new FileOutputStream(coordData)).writeObject(pageCoords);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
