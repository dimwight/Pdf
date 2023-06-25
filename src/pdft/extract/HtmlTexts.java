package pdft.extract;

import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSurface;
import facets.util.HtmlBuilder;
import facets.util.Util;
import org.apache.pdfbox.cos.COSDocument;

import static facets.util.Regex.replaceAll;
import static pdft.extract.HtmlTexts.TextStyle.Stream;

final class HtmlTexts extends DocTexts {
	public enum TextStyle{Extract,Stream,Table}
	HtmlTexts(COSDocument cosDoc, FacetAppSurface app){
		super(cosDoc,app.ff.providingCache());
	}
	@Override
	protected void traceOutput(String msg){
		if(true)super.traceOutput(msg);
	}
	String newHtml(final int pageAt,TextStyle style){
		return new HtmlBuilder(){
			final int basePts=FacetFactory.fontSizes[FacetFactory.fontSizeAt];
			final double unitPts=12;
			@Override
			protected String[]buildPageStyles(double points){
				return style != Stream ?new String[]{
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
				String raw = getPageText(pageAt, style);
				return"<p>"+(style != Stream ? raw.replace("\n","\n<p>")
						:replaceAll(raw, "\n","\n<p>",
						"\\(([^\\)]+)\\)","(<i>$1</i>)"));
			}
		}.buildPage();
	}
}
