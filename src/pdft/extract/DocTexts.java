package pdft.extract;

import facets.facet.FacetFactory;
import facets.util.*;
import facets.util.app.ProvidingCache;
import facets.util.app.ProvidingCache.ItemProvider;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

import java.io.IOException;
import java.util.*;

import static facets.util.Regex.replaceAll;
import static pdft.extract.DocTexts.TextStyle.*;

class DocTexts extends Tracer{
	private final ItemProvider<String> extract;
	private final ItemProvider<List<TextPosition>> chars;
	private final Map<Integer, Coords> pageCoords;

	public enum TextStyle{Extract,Stream,Table}
	final static class PageChars extends Tracer{
		public final PDPage page;
		public final List<TextPosition>textChars;
		PageChars(PDPage page,List<TextPosition>textChars){
			this.page=page;
			this.textChars=textChars;
		}
	}
	public final PDDocument doc;
	private final PDFTextStripper stripper;
	private final List<TextPosition>stripChars=new ArrayList();
	final public PageChars getChars(int pageAt){
		setStripperPage(pageAt);
//		Times.printElapsed("getChars pageAt=" + pageAt);
		extract.getForValues(pageAt);
		PageChars pageChars = new PageChars(((List<PDPage>) (this.
				doc).getDocumentCatalog().getAllPages()).get(pageAt),
				chars.getForValues(pageAt)
		);
//		Times.printElapsed("getChars-");
		return pageChars;
	}
	protected DocTexts(PDDocument doc, Map<Integer, Coords> pageCoords, ProvidingCache cache){
		if((this.doc=doc) ==null)
			throw new IllegalArgumentException("Null doc in "+Debug.info(this));
		this.pageCoords = pageCoords;
		try{
			stripper=new PDFTextStripper(){
				public boolean getSortByPosition(){
					return true;
				}
				@Override
				public void processEncodedText(byte[]string)throws IOException{
					super.processEncodedText(string);
				}
				@Override
				protected void processTextPosition(TextPosition text){
					super.processTextPosition(text);
				}
				protected void writePage()throws IOException{
					stripChars.clear();
					super.writePage();
					stripChars.addAll(charactersByArticle.get(0));
				}
			};
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		extract =new ItemProvider<String>(cache,this,"Text") {
			@Override
			protected String newItem() {
				try {
					return stripper.getText(DocTexts.this.doc);
				}catch(Exception e){
					throw new RuntimeException(e);
				}
			}
		};
		chars=new ItemProvider<List<TextPosition>>(cache, this, "Chars") {
			@Override
			protected List<TextPosition> newItem() {
				final List<TextPosition> textChars = new ArrayList();
				Iterator<TextPosition> chars = stripChars.iterator();
				while (chars.hasNext()) textChars.add(chars.next());
				return textChars;
			}
		};
		Times.times=true;
	}
	private String getPageText(int pageAt, TextStyle style){
		PDPage page = (PDPage) doc.getDocumentCatalog().getAllPages().get(pageAt);
		if(style== Extract) try{
			setStripperPage(pageAt);
			Times.printElapsed("getPageText: style=" + style+ " pageAt=" + pageAt);
			String got = extract.getForValues(pageAt);
			Times.printElapsed("getPageText-");
			return got;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		else if (style==Table) {
			setStripperPage(pageAt);
			extract.getForValues(pageAt);
			Coords coords = pageCoords.get(pageAt);
			if (coords == null)return "[No coords]";
			else return coords.constructTable(chars.getForValues(pageAt));
		}
		else try{
			return page.getContents().getInputStreamAsString();
		}catch(IOException e){
			return e.getMessage();
		}
	}
	private void setStripperPage(int pageAt) {
		stripper.setStartPage(pageAt +1);
		stripper.setEndPage(pageAt +1);
	}
	String newHtml(final int pageAt, TextStyle style){
		return new HtmlBuilder() {
			final int basePts = FacetFactory.fontSizes[FacetFactory.fontSizeAt];
			final double unitPts = 12;
			@Override
			protected String[] buildPageStyles(double points) {
				return style != Stream ? new String[]{
						"p{font-family:\"Times New Roman\",serif;font-size:" + usePts(14) + "pt}"
				}
						: new String[]{
						"p{font-family:\"Courier New\",Courier;font-size:" + usePts(12) + "pt;margin-bottom:" +
								usePts(3) + "pt;}",
						"i{color:gray}"
				};
			}
			private double usePts(int pt) {
				return Util.sf(basePts * pt / unitPts);
			}
			@Override
			public String newPageContent() {
				String raw = getPageText(pageAt, style);
				return "<p>" + (style != Stream ? raw.replace("\n", "\n<p>")
						: replaceAll(raw, "\n", "\n<p>",
						"\\(([^\\)]+)\\)", "(<i>$1</i>)"));
			}
		}.buildPage();
	}
}

