package pdft.extract;

import facets.util.Debug;
import facets.util.Times;
import facets.util.Tracer;
import facets.util.app.ProvidingCache;
import facets.util.app.ProvidingCache.ItemProvider;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;
import pdft.extract.HtmlTexts.TextStyle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static pdft.extract.HtmlTexts.TextStyle.Extract;

class DocTexts extends Tracer{
	private final ItemProvider<String> text;
	private final ItemProvider<List<TextPosition>> chars;
	final static class PageChars extends Tracer{
		public final PDPage page;
		public final List<TextPosition>textChars;
		PageChars(PDPage page,List<TextPosition>textChars){
			this.page=page;
			this.textChars=textChars;
		}
	}
	public final PDDocument doc;
	public final List<PDPage>pages;
	private final PDFTextStripper stripper;
	private final List<TextPosition>stripChars=new ArrayList();
	protected DocTexts(COSDocument cosDoc, ProvidingCache cache){
		if((doc=new PDDocument(cosDoc))==null)
			throw new IllegalArgumentException("Null cos in "+Debug.info(this));
		pages=new PDDocument(cosDoc).getDocumentCatalog().getAllPages();
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
		text=new ItemProvider<String>(cache,this,"Text") {
			@Override
			protected String newItem() {
				try {
					return stripper.getText(doc);
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
	final public PageChars getChars(int pageAt){
		setStripperPage(pageAt);
//		Times.printElapsed("getChars pageAt=" + pageAt);
		text.getForValues(pageAt);
		PageChars pageChars = new PageChars(pages.get(pageAt), Collections.unmodifiableList(
				chars.getForValues(pageAt)
		));
//		Times.printElapsed("getChars-");
		return pageChars;
	}
	protected String getPageText(int pageAt, TextStyle style){
		if(style== Extract) try{
			setStripperPage(pageAt);
			Times.printElapsed("getPageText pageAt=" + pageAt);
			String got = text.getForValues(pageAt);
			Times.printElapsed("getPageText-");
			return got;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		else try{
			return pages.get(pageAt).getContents().getInputStreamAsString();
		}catch(IOException e){
			return e.getMessage();
		}
	}
	private void setStripperPage(int pageAt) {
		stripper.setStartPage(pageAt +1);
		stripper.setEndPage(pageAt +1);
	}
}

