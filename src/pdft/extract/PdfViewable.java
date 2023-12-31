package pdft.extract;

import facets.core.app.SViewer;
import facets.core.app.ViewableFrame;
import facets.core.app.avatar.AvatarView;
import facets.core.superficial.SNumeric;
import facets.core.superficial.STarget;
import facets.core.superficial.STextual;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SelectionView;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSurface;
import facets.util.NumberPolicy;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static facets.core.app.TextView.PAGE_NEXT;
import static facets.core.app.TextView.PAGE_PREVIOUS;

class PdfViewable extends ViewableFrame{
	static final int COS_GO_TO_PAGE=0;
	static final int COS_PAGE_COUNT=1;
	static final int COS_FONTS=2;
	static final int COS_LAST=COS_FONTS;
	private final List<COSDictionary>cosPages;
	private final int pageCount;
	private final SNumeric goToPage;
	final DocTexts texts;
	int viewPageAt=-1;
	PdfViewable(String title, PDDocument doc, Map<Integer, Coords> pageCoords, FacetAppSurface app){
		super(title,doc);
		texts=new DocTexts(doc,pageCoords, app.ff.providingCache());
		cosPages=new ArrayList();
		for(Object each:doc.getDocumentCatalog().getAllPages())
			cosPages.add(((PDPage)each).getCOSDictionary());
		pageCount=cosPages.size();
		int pageAt=0;
		defineSelection(cosPages.get(pageAt));
		goToPage=new SNumeric("Page",pageAt+1,new SNumeric.Coupler(){
			@Override
			public void valueSet(SNumeric n){
				COSDictionary pageNow=cosPages.get((int)n.value()-1);
				if(pageNow!=selection().single())defineSelection(pageNow);
			}
			@Override
			public NumberPolicy policy(SNumeric n){
				return new NumberPolicy(1,cosPages.size()){
					@Override
					public String[]incrementTitles(){
						return new String[]{PAGE_PREVIOUS,PAGE_NEXT};
					}
				};
			}
		});
	}
	@Override
	protected SSelection newViewerSelection(SViewer viewer){
		SelectionView view=(SelectionView)viewer.view();
		final int pageAt=(int)goToPage.value()-1;
		if(viewPageAt!=pageAt&&view instanceof AvatarView){
			view.updateStateStamp();
			viewPageAt=pageAt;
		}
		return view instanceof AvatarView?
				((PageRenderView)view).newViewerSelection()
			:view instanceof PageHtmlView ?
				((PageHtmlView)view).newViewerSelection(texts,pageAt)
			:view.newViewerSelection(viewer,selection());
	}
	@Override
	protected void viewerSelectionChanged(SViewer viewer,SSelection selection){
		defineSelection(selection.single());
	}
	@Override
	public SSelection defineSelection(Object definition){
		final COSDictionary cos=definition instanceof COSDictionary?
			(COSDictionary)definition:cosPages.get((int)goToPage.value()-1);
		SSelection set=setSelection(new SSelection(){
			@Override
			public Object content(){
				return framed;
			}
			@Override
			public Object single(){
				return cos;
			}
			@Override
			public Object[]multiple(){
				return new Object[]{cos};
			}
		});
		if(goToPage!=null)goToPage.setValue(cosPages.indexOf(cos)+1);
		return set;
	}
	@Override
	protected STarget[]lazyElements(){
		return new STarget[]{
				goToPage,
				new STextual("pageCount","/  "+pageCount,new STextual.Coupler()),
				FacetFactory.fontIndexing
			};
	}
	public void setFramedState(Object stateSpec,boolean interim){}
}
