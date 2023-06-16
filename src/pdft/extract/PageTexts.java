package pdft.extract;

import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSurface;
import facets.util.HtmlBuilder;
import facets.util.Times;
import facets.util.Util;
import facets.util.app.ProvidingCache.ItemProvider;
import org.apache.pdfbox.cos.COSDocument;

import static facets.util.Regex.replaceAll;

final class PageTexts extends DocTexts {
	public enum TextStyle{Extracted,Stream;
		public String title(){
			return this==Extracted?"E&xtracted":"Stream";
		}
	}
	private final PagePainters emptyPainters=new PagePainters(this,0,null);
	private final FacetAppSurface app;
	PageTexts(COSDocument cosDoc, FacetAppSurface app){
		super(cosDoc);
		this.app=app;
	}
	PagePainters getPainters(final int pageAt){
		if(false)trace(".getPainters: pageAt=",pageAt);
		traceDebug(".getPainters:=",this);
		PagePainters painters = new PagePainters(PageTexts.this, pageAt, app);
		if(false)trace(".getPainters: painters=",painters);
		return painters!=null?painters:emptyPainters;
	}
	@Override
	protected void traceOutput(String msg){
		if(true)super.traceOutput(msg);
	}
	String newHtml(final int pageAt,TextStyle style){
		final boolean extracted=style== TextStyle.Extracted;
		if(false)Times.printElapsed("Texts..buildPageContent extracted="+extracted);
		final int basePts=FacetFactory.fontSizes[FacetFactory.fontSizeAt];
		String html=new ItemProvider<String>(app.ff.providingCache(),doc,
				PageTexts.class.getSimpleName()+".newHtml"){
			@Override
			protected String newItem(){
				return new HtmlBuilder(){
					final double unitPts=12;
					@Override
					protected String[]buildPageStyles(double points){
						return extracted?new String[]{
				"p{font-family:\"Times New Roman\",serif;font-size:"+usePts(14)+"pt}"
						}
						:new String[]{
				"p{font-family:\"Courier New\",Courier;font-size:"+usePts(12)+"pt;margin-bottom:" +
						usePts(3)+"pt;}",
				"i{color:gray}"
						};
					}
					private double usePts(int pt){
						return Util.sf(basePts*pt/unitPts);
					}
					@Override
					public String newPageContent(){
						return"<p>"+(extracted?newExtracted(pageAt).replace("\n","\n<p>")
								:replaceAll(getStreamText(pageAt),new String[]{
							"\n","\n<p>",
							"\\(([^\\)]+)\\)","(<i>$1</i>)"
						}));
					}
				}.buildPage();
			}
			@Override
			protected long buildByteCount(){
				return 0;
			}
			
			@Override
			protected long finalByteCount(String storeReady){
				return storeReady.getBytes().length;
			}
		}.getForValues(pageAt,extracted,basePts);
		if(false)Times.printElapsed("Texts..buildPageContent html="+html.length());
		return html==null?"[Operation not completed]":html;
	}
}
