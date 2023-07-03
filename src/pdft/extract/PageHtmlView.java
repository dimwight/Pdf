package pdft.extract;

import facets.core.app.HtmlView;
import facets.core.superficial.*;
import facets.core.superficial.STextual.Coupler;
import facets.core.superficial.app.SSelection;
import facets.util.Debug;
import facets.util.TextLines;
import facets.util.Times;
import facets.util.Util;
import facets.util.app.AppValues;
import facets.util.tree.ValueNode;
import pdft.extract.DocTexts.TextStyle;

import java.io.File;

import static pdft.extract.DocTexts.TextStyle.Stream;
import static pdft.extract.DocTexts.TextStyle.Table;
import static pdft.extract.PdfContenter.ARG_WRAP;

final class PageHtmlView extends HtmlView.SmartView
{
	static final int TARGET_COUNT=0,TARGET_WRAP=1,TARGET_EXPORT=0;
	private final STarget codeCount=new STextual("Lines",new Coupler(){
			@Override
			protected String getText(STextual t){
				t.setLive(false);
				return""+lineCount;
			}
		});
	private final STarget export=new STrigger("Export", new STrigger.Coupler() {
		@Override
		public void fired(STrigger t) {
			try{
				File file=new File(AppValues.userDir(),"export.html");
				new TextLines(file).writeLines(rawHtml.split("\n"));
				String path=file.getCanonicalPath();
				Util.windowsOpenUrl("file://" +path);
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}
	});
	private final STarget wrap;
	final TextStyle style;
	int lineCount;
	private String rawHtml;
	PageHtmlView(TextStyle style, AppValues values){
		super(style.name());
		this.style=style;
		final ValueNode nature=values.nature();
		wrap=new SToggling("Wrap &Lines",
				nature.getOrPutBoolean(ARG_WRAP,false),
				new SToggling.Coupler(){
			@Override
			public void stateSet(SToggling t){
				nature.put(ARG_WRAP,t.isSet());
			}
		});
	}
  @Override
	protected void traceOutput(String msg){
		if(false)Times.printElapsed(Debug.info(this)+msg);
		else if(false)super.traceOutput(msg);
	}
	SSelection newViewerSelection(DocTexts texts, int pageAt){
		rawHtml = texts.newHtml(pageAt,style);
		final String lines[]= rawHtml.split("\\s*<p>");
		lineCount=lines.length;
		return new SSelection(){
			public Object content(){
				return false&& quickLineHeight()>0?lines: rawHtml;
			}
			public Object[]multiple(){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
			public Object single(){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
		};
	}
  //@Override
  public int quickLineHeight(){
  	return style != Stream ? -1 : 17;
  }
	@Override
	public boolean showSource() {
		return false;
	}
	@Override
  public boolean wrapLines(){
		return style!= Stream ||((SToggling)wrap).isSet();
  }
	SFrameTarget newFramed(){
		return new SFrameTarget(this){
			@Override
			protected STarget[]lazyElements(){
				return style == Stream?new STarget[]{
						codeCount,
						wrap
				} : style == Table ? new STarget[]{
						export
				} : super.lazyElements();
			}
		};
	}
}