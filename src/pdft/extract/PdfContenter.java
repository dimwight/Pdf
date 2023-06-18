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
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import static facets.core.app.ActionViewerTarget.newViewerAreas;
import static facets.facet.AreaFacets.*;
import static facets.facet.FacetFactory.*;
import static pdft.extract.PageTexts.*;
import static pdft.extract.PdfViewable.COS_FONTS;
import static pdft.extract.PdfViewable.COS_LAST;

final class PdfContenter extends ViewerContenter{
	public static final String ARG_WRAP="wrapCode";
	private static int defaults=1;
	private final FacetAppSurface app;
	private PageRenderView pageView;
	PdfContenter(Object source, FacetAppSurface app){
		super(source);
		if((this.app=app)==null)throw new IllegalArgumentException(
				"Null app in "+Debug.info(this));
	}
	@Override
	protected ViewableFrame newContentViewable(final Object source){
		COSDocument cosDoc=(COSDocument)source;
		if(cosDoc==null)throw new AppSurface.ContentCreationException(
				"Content creation was interrupted for "+source+".");
		String title="Coords"+defaults++;
		return new PdfViewable(title,cosDoc,app){
			@Override
			public SSelection defineSelection(Object definition){
				SSelection selection=super.defineSelection(definition);
				if(pageView!=null)setPageViewToRotation(pageView);
				return selection;
			}
		};
	}
	@Override
	protected FacetedTarget[]newContentViewers(ViewableFrame viewable){
		SFrameTarget pages=new SFrameTarget(new CosTreeView(CosTreeView.TreeStyle.Pages));
		SFrameTarget document=new SFrameTarget(new CosTreeView(CosTreeView.TreeStyle.Document)),
				extracted=new PageTextView(TextStyle.Extracted,app.spec).newFramed(),
				stream=new PageTextView(TextStyle.Stream,app.spec).newFramed();;
		PageRenderView render=new PageRenderView(new PageAvatarPolicies());
		render.setToPageRotation(new PDPage((COSDictionary)viewable.selection().single()));
		SFrameTarget page = new SFrameTarget(render);
		((PdfViewable)viewable).setPageViewToRotation(pageView=(PageRenderView)page.framed);
		return newViewerAreas(viewable,
				new SFrameTarget[]{pages,
						/*
						document,
						extracted,
						stream,
						*/
						page,
		});
	}
	@Override
	protected void attachContentAreaFacets(AreaRoot area){
		ViewerAreaMaster master = new ViewerAreaMaster() {
			protected ViewerAreaMaster newChildMaster(SAreaTarget area1) {
				final SView view = ((ViewerTarget) area1.activeFaceted()).view();
				final boolean forPage = view instanceof PageRenderView,
						forTree = view instanceof CosTreeView,
						forStream = view instanceof PageTextView
								&& ((PageTextView) view).style == TextStyle.Stream;
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
						if (!forStream) return null;
						STargeter[] elements = targeter.elements();
						return app.ff.toolGroups(targeter, HINT_NONE,
								app.ff.textualField(elements[PageTextView.TARGET_COUNT], 5, HINT_USAGE_FORM),
								app.ff.togglingCheckboxes(elements[PageTextView.TARGET_WRAP], HINT_BARE),
								true ? null : app.ff.togglingButtons(elements[PageTextView.TARGET_WRAP], HINT_BARE),
								app.ff.spacerTall(30)
						);
					}
				};
			}
		};
		app.ff.areas().attachViewerAreaPanes(area, master, AreaFacets.PANE_SPLIT_VERTICAL);
	}

	@Override
	public STarget[]lazyContentAreaElements(SAreaTarget area){
		return app.ff.areas().panesGetTarget(area).elements();
	}
	@Override
	public void areaRetargeted(SContentAreaTargeter root){
		if(true)return;;
		Object activeView=((SFrameTarget)root.view().target()).framed;
		STargeter[]pane=root.elements(),paneShow=pane[PANE_SHOW].elements(),
			content=root.content().elements();
		boolean extractedOrCodeIsActive=activeView instanceof PageTextView,
			renderIsActive=activeView instanceof PageRenderView,
			extractedOrCodePaneSet=((SToggling)paneShow[4].target()).isSet()
				||((SToggling)paneShow[5].target()).isSet(),
			renderPaneSet=((SToggling)paneShow[3].target()).isSet(),
			noPaneMaximised=pane[PANE_ACTIVE].elements()[PANE_ACTIVE_MAXIMISE].
				target().isLive();
		content[COS_FONTS].target().setLive(
				extractedOrCodeIsActive||(extractedOrCodePaneSet&&noPaneMaximised));
		content[
				COS_LAST+1].target().setLive(
				renderIsActive||(renderPaneSet&&noPaneMaximised));
	}
	@Override
	public LayoutFeatures newContentFeatures(SContentAreaTargeter area){
		return new PdfFeatures(app,area);
	}
}
