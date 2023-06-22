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
import facets.util.app.ProvidingCache;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.util.ArrayList;
import java.util.List;

import static facets.core.app.TextView.PAGE_NEXT;
import static facets.core.app.TextView.PAGE_PREVIOUS;

class PdfViewable extends ViewableFrame{
	static final int COS_GO_TO_PAGE=0;
	static final int COS_PAGE_COUNT=1;
	static final int COS_FONTS=2;
	static final int COS_LAST=COS_FONTS;
	private final ProvidingCache appCache;
	private final List<COSDictionary>cosPages;
	private final int pageCount;
	private final SNumeric goToPage;
	final DocTexts texts;
	int viewPageAt=-1;
	PdfViewable(String title, COSDocument cosDoc, FacetAppSurface app){
		super(title,cosDoc);
		appCache=app.ff.providingCache();
		texts=new HtmlTexts(cosDoc,app);
		cosPages=new ArrayList();
		for(Object each:new PDDocument(cosDoc).getDocumentCatalog().getAllPages())
			cosPages.add(((PDPage)each).getCOSDictionary());
		pageCount=cosPages.size();
		int pageAt=true||!title.startsWith("Default")?1
				:Integer.valueOf(title.replaceAll("[^\\d]+",""));
		defineSelection(cosPages.get(pageAt-1));
		goToPage=new SNumeric("Page",pageAt,new SNumeric.Coupler(){
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
				((PageHtmlView)view).newViewerSelection((HtmlTexts) texts,pageAt)
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
	void setPageViewToRotation(PageRenderView view){
		view.setToPage(new PDPage((COSDictionary)selection().single()));
	}
}
