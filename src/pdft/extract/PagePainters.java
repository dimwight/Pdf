package pdft.extract;

import facets.core.app.avatar.Painter;
import facets.facet.app.FacetAppSurface;
import facets.facet.kit.avatar.ImageProviderAwt;
import facets.util.Debug;
import facets.util.SizeEstimable;
import facets.util.Tracer;
import facets.util.app.WatchableOperation;
import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.TextPosition;
import org.apache.pdfbox.util.TextPositionComparator;
import pdft.extract.DocTexts.PageChars;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.*;

final class PagePainters extends Tracer implements SizeEstimable{
		@Override
	protected void traceOutput(String msg){
		if(true)super.traceOutput(msg);
	}
	private final List<PageChar>plainChars=new ArrayList();
	private final List<PageChar>fullChars=new ArrayList();
	private List<PageChar>paintChars;
	private final FacetAppSurface app;
	private final PDPage page;
	private final int pageAt;
	private final int rotation;
	private final Dimension pageSize;
	PagePainters(DocTexts texts, int pageAt, FacetAppSurface app){
		this.app=app;
		PageChars chars=texts.getChars(this.pageAt=pageAt);
		page=chars.page;
		rotation=page.findRotation();
		pageSize = page.findMediaBox().createDimension();
		for(TextPosition textChar:chars.textChars)
			if(textChar!=null)plainChars.add(new PageChar(textChar,rotation,0,false));
	}
	@Override
	public String toString(){
		return Debug.info(this)+": Page #"+pageAt;
	}
	Painter newPainter(int viewsAt, boolean drawPage){
		final boolean flip=rotation!=0;
		final int paintWidth=!flip?pageSize.width:pageSize.height,
				paintHeight=!flip?pageSize.height:pageSize.width;
		return app==null? graphics -> {}
				:new Painter(){
			final double scale=1;
			int width=(int)(paintWidth*scale),height=(int)(paintHeight*scale);
			final Image image=!drawPage ?null:new ImageProviderAwt(
					app.ff.providingCache(),
					PagePainters.this,PagePainters.class.getSimpleName()+".newTextPainter",
					width,height){
				@Override
				protected BufferedImage newPaintedImage(int width,int height){
					BufferedImage paintable=newPaintableImage(width,height,Color.white);
					drawPageGraphics((Graphics2D)paintable.getGraphics(),scale);
					return paintable;
				}
				@Override
				protected long buildByteCount(){
					return width*height*3;
				}
			}.getImageForValues(pageAt,viewsAt);
			public void paintInGraphics(Object graphics){
				Graphics2D paint=(Graphics2D)((Graphics)graphics).create();
				if(drawPage){
					if(image==null)drawPageGraphics((Graphics2D)paint.create(),scale);
					else paint.drawImage(image.getScaledInstance(paintWidth,-1,
							Image.SCALE_SMOOTH),0,0,null);
					paintChars=fullChars;
				}
				else paintChars=plainChars;
				for(PageChar c:paintChars)c.paintInGraphics(paint.create());
			}
			private void drawPageGraphics(final Graphics2D g2,final double scale){
			WatchableOperation op=new WatchableOperation("PagePainters.drawPageGraphics"){
			@Override
			public void doSimpleOperation(){
				final Map<TextPosition,Color>colors=new HashMap();
				final List<TextPosition>texts=new ArrayList();
				g2.scale(scale,scale);
				if(flip){
					g2.translate(pageSize.height,0);
					g2.rotate(rotation/360f*2*Math.PI);
				}
				try{
					new PageDrawer(){
						@Override
						protected void processTextPosition(TextPosition text){
							if(false)super.processTextPosition(text);
							else try{
								colors.put(text,getGraphicsState().getNonStrokingColor().getJavaColor());
								texts.add(text);
							}catch(IOException e){
								throw new RuntimeException(e);
							}
						}
					}.drawPage(g2,page,pageSize);
				}catch(IOException e){
					throw new RuntimeException(e);
				}
				if(!fullChars.isEmpty())return;
				Collections.sort(texts,new TextPositionComparator());
				for(TextPosition t:texts)
					fullChars.add(new PageChar(t,rotation,colors.get(t).getRGB(),false));
			}};
				if(false)app.runWatched(op);
				else op.doOperations();
			}
			@Override
			public int hashCode(){
				return Arrays.hashCode(new Object[]{pageSize,pageAt,viewsAt});
			}
		};
	}
	@Override
	public long estimateSize() {
		return 0;
	}
}