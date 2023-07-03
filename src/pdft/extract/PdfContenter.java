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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import pdft.PdfCore;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static facets.core.app.ActionViewerTarget.newViewerAreas;
import static facets.facet.AreaFacets.*;
import static facets.facet.FacetFactory.*;
import static pdft.extract.PdfViewable.COS_FONTS;
import static pdft.extract.PdfViewable.COS_LAST;

final class PdfContenter extends ViewerContenter{
	public static final String ARG_WRAP="wrapCode";
	private final FacetAppSurface app;
	private PageRenderView renderView;
	private final Map<Integer,Coords>pageCoords=new HashMap();
	private File coordData;
	PdfContenter(Object source, FacetAppSurface app){
		super(source);
		if((this.app=app)==null)throw new IllegalArgumentException(
				"Null app in "+Debug.info(this));
	}
	@Override
	public void wasRemoved() {
		for (Integer c:pageCoords.keySet())
			if(pageCoords.get(c).isEmpty())pageCoords.remove(c);
		try {
			new ObjectOutputStream(new FileOutputStream(coordData))
					.writeObject(pageCoords);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	protected ViewableFrame newContentViewable(final Object source){
		File file= (File) source;
		PDDocument doc;
		try{
			doc = new PdfCore(file).document;
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		String title=file.getName().replace(".pdf","");
		coordData = new File( title + ".dat");
		if (coordData.exists())	try {
			Object read=new ObjectInputStream(new FileInputStream(coordData))
					.readObject();
			pageCoords.putAll((Map<Integer,Coords>) read);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		return new PdfViewable(title, doc,pageCoords, app){
			@Override
			public SSelection defineSelection(Object definition){
				if(definition instanceof Coord){
					renderView.defineSelection((Coord) definition);
					return selection();
				}
				SSelection selection=super.defineSelection(definition);
				if(renderView !=null)setPageViewToRotation(renderView);
				return selection;
			}
		};
	}
	@Override
	protected FacetedTarget[]newContentViewers(ViewableFrame viewable){
		SFrameTarget pages=new SFrameTarget(new CosTreeView(CosTreeView.TreeStyle.Pages));
		AppSpecifier values = app.spec;
		SFrameTarget document=new SFrameTarget(new CosTreeView(CosTreeView.TreeStyle.Document)),
				extract=new PageHtmlView(DocTexts.TextStyle.Extract, values).newFramed(),
				table=new PageHtmlView(DocTexts.TextStyle.Table, values).newFramed(),
				stream=new PageHtmlView(DocTexts.TextStyle.Stream, values).newFramed();
		(renderView =new PageRenderView(pageCoords, new PageAvatarPolicies(app)))
			.setToPage(new PDPage((COSDictionary)viewable.selection().single()));
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
	protected void attachContentAreaFacets(AreaRoot area){
		ViewerAreaMaster master = new ViewerAreaMaster() {
			protected ViewerAreaMaster newChildMaster(SAreaTarget area1) {
				final SView view = ((ViewerTarget) area1.activeFaceted()).view();
				final boolean forPage = view instanceof PageRenderView,
						forTree = view instanceof CosTreeView,
						forStream = view instanceof PageHtmlView
								&& ((PageHtmlView) view).style == DocTexts.TextStyle.Stream;
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
								app.ff.textualField(elements[PageHtmlView.TARGET_COUNT], 5, HINT_USAGE_FORM),
								app.ff.togglingCheckboxes(elements[PageHtmlView.TARGET_WRAP], HINT_BARE),
								true ? null : app.ff.togglingButtons(elements[PageHtmlView.TARGET_WRAP], HINT_BARE),
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
		boolean extractedOrCodeIsActive=activeView instanceof PageHtmlView,
			renderIsActive=activeView instanceof PageRenderView,
			extractedOrCodePaneSet=((SToggling)paneShow[4].target()).isSet()
				||((SToggling)paneShow[5].target()).isSet(),
			renderPaneSet=((SToggling)paneShow[3].target()).isSet(),
			noPaneMaximised=pane[PANE_ACTIVE].elements()[PANE_ACTIVE_MAXIMISE].
				target().isLive();
		content[COS_FONTS].target().setLive(
				extractedOrCodeIsActive||(extractedOrCodePaneSet&&noPaneMaximised));
		content[COS_LAST+1].target().setLive(
				renderIsActive||(renderPaneSet&&noPaneMaximised));
	}
	@Override
	public LayoutFeatures newContentFeatures(SContentAreaTargeter area){
		return new PdfFeatures(app,area);
	}
}
