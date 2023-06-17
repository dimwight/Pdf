package pdft.extract;

import facets.core.app.MenuFacets;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.ViewerTarget;
import facets.core.superficial.SFacet;
import facets.core.superficial.STargeter;
import facets.facet.AppFacetsBuilder;
import facets.facet.AreaFacets.PaneMenuStyle;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSurface;

import static pdft.extract.PdfPages.*;

final class PdfFeatures extends FacetFactory{
	private final FacetAppSurface app;
	private final SContentAreaTargeter area;
	STargeter pages[],
		goTo,
		lastPage,
		fonts,
		views;
	SFacet mainMenu,
		paneMenu,
		menus[],
		toolbar;
	PdfFeatures(FacetAppSurface app, SContentAreaTargeter area){
		super(app.ff);
		this.app=app;
		this.area=area;
		if (false) {
			pages=area.content().elements();
			goTo=pages[COS_GO_TO_PAGE];
			lastPage=pages[COS_PAGE_COUNT];
			fonts=pages[COS_FONTS];
			views=pages[COS_LAST+1];
		}
		mainMenu=menuRoot(new AppFacetsBuilder(this,area
				).newMenuFacets());
		paneMenu=menuRoot(areas().new PaneFacets("Pane",area){
			protected PaneMenuStyle style(){
				return PaneMenuStyle.Dialog;
			}
		});
		menus=new SFacet[]{mainMenu, paneMenu};
		toolbar=null;
	}
	@Override
	public SFacet[]header(){
		return menus;
	}
	@Override
	public SFacet extras(){
		return appExtras(app);
	}
	@Override
	public SFacet toolbar(){
		return toolbar;
	}
	@Override
	protected MenuFacets getServicesContextMenuFacets(){
		return new MenuFacets(area,""){
			public SFacet[]getContextFacets(ViewerTarget viewer,
					SFacet[]viewerFacets){
				return new SFacet[]{};
			}
		};
	}
	@Override
	public SFacet status(){
		return null;
	}
}