package pdft.extract;

import facets.core.app.AppSurface.ContentStyle;
import facets.core.app.FeatureHost;
import facets.core.app.SContenter;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FacetAppSurface;

import java.io.File;

import static facets.core.app.AppSurface.ContentStyle.SINGLE;

public final class pdfExtract extends FacetAppSpecifier{
	pdfExtract(){
		super(pdfExtract.class);
	}

	@Override
	public boolean isFileApp() {
		return false;
	}

	@Override
	public boolean canCreateContent(){
		return false;
	}
	@Override
	public ContentStyle contentStyle(){
		return SINGLE;
	}
	@Override
	protected FacetAppSurface newApp(FacetFactory ff,FeatureHost host){
		return new FacetAppSurface(this,ff){
			@Override
			public Object getInternalContentSource(){
				return new File("PdfApp.pdf");
			}
			@Override
			protected SContenter newContenter(final Object source){
				return new PdfContenter(source,this);
			}
		};
	}
	public static void main(String[]args){
		new pdfExtract().buildAndLaunchApp(args);
	}
}