package pdft.extract;

import facets.util.Debug;
import facets.util.Tracer;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static pdft.extract.HtmlTexts.TextStyle.Extracted;

class DocTexts extends Tracer{
	final class PageChars extends Tracer{
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
	protected DocTexts(COSDocument cosDoc){
		if(cosDoc==null)
			throw new IllegalArgumentException("Null cos in "+Debug.info(this));
		doc=new PDDocument(cosDoc);
		try{
			stripper=new PDFTextStripper(){
				private final boolean test=false;
				private int testCharAt=-1;
				public boolean getSortByPosition(){
					return!test;
				}
				@Override
				public void processEncodedText(byte[]string)throws IOException{
					if(false||test)trace(".processEncodedText: ",new String(string));
					super.processEncodedText(string);
				}
				@Override
				protected void processTextPosition(TextPosition text){
					if(false||test){
						trace(".processTextPosition: ",text.getCharacter());
						testCharAt++;
						stripChars.add(text);
					}
					super.processTextPosition(text);
				}
				protected void writePage()throws IOException{
					if(!test)stripChars.clear();
					else trace(".writePage: charsRaw=",stripChars.size());
					super.writePage();
					if(!test)stripChars.addAll(charactersByArticle.get(0));
					else trace(".writePage: charsRaw="+stripChars.size()+" charAt="+testCharAt);
				}
			};
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		pages=new PDDocument(cosDoc).getDocumentCatalog().getAllPages();
	}
	final public PageChars getChars(int pageAt){
		setStripperPage(pageAt);
		try{
			String text=stripper.getText(doc);
			final List<TextPosition>textChars=new ArrayList();
			Iterator<TextPosition>chars=stripChars.iterator();
			boolean debug=false;
			if(debug)trace(": text=\n",text);
			for(int charAt=0;charAt<text.length();charAt++){
				String textNext=text.substring(charAt,charAt+1);
				if(textNext.matches("\\s+")){
					textChars.add(null);
					if(debug)trace(": space ");
					continue;
				}else{
					if(!chars.hasNext())break;
					TextPosition match=chars.next();
					String matchChar=match.getCharacter();
					int matchCount=matchChar.length();
					if(matchChar.matches("\\s+")){
						charAt--;
						continue;
					}
					else if(matchCount>1){
						textNext=text.substring(charAt,charAt+matchCount);
						charAt+=matchCount-1;
					}
					String msg="matchChar="+matchChar+" textNext="+textNext;
					if(!matchChar.equals(textNext))throw new IllegalStateException(
							"No match "+msg);
					else if(debug)trace(": matched ",msg);
					textChars.add(match);
				}
			}
			return new PageChars(pages.get(pageAt),Collections.unmodifiableList(textChars));
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	protected String newPageText(int pageAt, HtmlTexts.TextStyle style){
		if(style== Extracted) try{
			setStripperPage(pageAt);
			return stripper.getText(doc);
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

