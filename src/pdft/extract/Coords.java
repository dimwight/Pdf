package pdft.extract;

import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.PlaneView;
import facets.util.StatefulCore;
import facets.util.TextLines;
import facets.util.Util;
import facets.util.app.AppValues;
import org.apache.pdfbox.util.TextPosition;

import java.io.File;
import java.util.*;

final class Coords extends StatefulCore {
    private final List<Coord> forX = new ArrayList();
    private final List<Coord> forY = new ArrayList();
    private transient StringBuilder sb;
    Coords(PlaneView view) {
        super("Coords-" + view.title());
        add(true, view);
        add(false, view);
    }
    void add(boolean forX, PlaneView view) {
        double pageSize = forX ? view.showWidth(): view.showHeight() ;
        (forX ? this.forX : this.forY).add(0, new Coord(forX, pageSize));
        updateStateStamp();
        traceForX("add");
    }
    public void sortAll() {
        Collections.sort(forX);
        Collections.sort(forY);
        traceForX("sortAll");
    }
    void remove(Coord coord) {
        (coord.forX ? forX : forY).remove(coord);
        traceForX("remove");
        updateStateStamp();
    }
    private void traceForX(String from) {
        if (false)
        trace("."+ from + ": forX=", forX.toArray(new Coord[0]));
    }
    AvatarContent[] getAll() {
        while (forX.size()>3&&!forX.get(3).isLive())forX.remove(3);
        while (forY.size()>3&&!forY.get(3).isLive())forY.remove(3);
        ArrayList<AvatarContent> all = new ArrayList(forX);
        all.addAll(forY);
        if (false)all.addAll(newBoundsCells(null));
        return all.toArray(new AvatarContent[0]);
    }
    private Collection<AvatarContent> newBoundsCells(List<TextPosition> chars) {
        sortAll();
        List<Coord> useX = jumpZeroes(forX);
        List<Coord> useY = jumpZeroes(forY);
        List<AvatarContent> lines = new ArrayList();
        if (useX.size()<2||useY.size()<2) {
            lines.add(new BoundsCell(new double[]{0,0,20,20}));
            return lines;
        }
        ListIterator<Coord> forY_ = useY.listIterator();
        sb = new StringBuilder("<html><head></head><body><table>");
        while (forY_.hasNext()) {
            sb.append("<tr>");
            Coord top = forY_.next();
            Coord bottom = forY_.next();
            if (forY_.hasNext()) forY_.previous();
            ListIterator<Coord> forX_ = useX.listIterator();
            while (forX_.hasNext()) {
                sb.append("<td>");
                Coord left = forX_.next();
                Coord right = forX_.next();
                if (forX_.hasNext())forX_.previous();
                BoundsCell cell = new BoundsCell(left.getAt(), top.getAt(),
                        right.getAt(), bottom.getAt());
                lines.add(cell);
                sb.append(//left+"" +top+ "<br>" +right+ "" +bottom+ 
                        cell.getText(chars)+ "</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table></body></html>");
        return lines;
    }
    String constructTable(List<TextPosition> chars) {
        sb=new StringBuilder("No table");
        newBoundsCells(chars);
        String page = sb.toString();
        if (false)
        try{
            File file=new File(AppValues.userDir(),"export.html");
            new TextLines(file).writeLines(page.split("\n"));
            String path=file.getCanonicalPath();
            Util.windowsOpenUrl("file://" +path);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
        return page;
    }
    private List<Coord> jumpZeroes(List<Coord> zeroed) {
        ArrayList<Coord> jumped = new ArrayList<>();
        Iterator<Coord> i = zeroed.iterator();
        while (i.hasNext()) {
            Coord next = i.next();
            if (next.isLive())
                jumped.add(next);
        }
        return jumped;
    }
    boolean isEmpty() {
        boolean found=false;
        for (AvatarContent c:getAll()){
            if(!(c instanceof Coord))continue;
            Coord check= (Coord) c;
            found|=check.isLive();
        }
        return !found;
    }
}
