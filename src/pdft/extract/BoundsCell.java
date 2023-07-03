package pdft.extract;

import facets.core.app.avatar.AvatarContent;
import facets.util.Util;
import facets.util.geom.Line;
import org.apache.pdfbox.util.TextPosition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static java.lang.Double.NaN;

class BoundsCell implements AvatarContent {
    final Line bounds;
    BoundsCell(double[] vals) {
        bounds = new Line(vals);
    }
    BoundsCell(double left, double top, double right, double bottom) {
        this(new double[]{left, top, right, bottom});
    }
    public String getText(List<TextPosition> chars) {
        if (chars == null) return "";
        ListIterator<TextPosition> i = chars.listIterator();
        List<TextPosition> chars_ = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        while (i.hasNext()) {
            TextPosition next = i.next();
            float x = next.getX();
            float y = next.getY();
            if (x > bounds.from.x() && x < bounds.to.x()
                    && y > bounds.from.y() && y < bounds.to.y()) {
                i.remove();
                chars_.add(next);
            }
        }

        List<List<TextPosition>> lines = new ArrayList<>();
        List<TextPosition> line = null;
        float maxWidth=0;
        for (TextPosition c:chars_) {
            float width = c.getWidth();
            if (maxWidth<width)maxWidth=width;
        }
        i = chars_.listIterator();
        double thenY = NaN;
        double thenX = NaN;
        while (i.hasNext()) {
            TextPosition next = i.next();
            float getY = next.getY();
            float getX = next.getX();
            if (getY != thenY) {
                if (!lines.isEmpty())sb.append("<br>");
                lines.add(line = new ArrayList<>());
                thenY = getY;
            }
             if (false&&
                    getX-thenX> maxWidth)
                sb.append(false?
                        Util.sf(next.getWidth()):" ");
            thenX=getX;
            line.add(next);
            sb.append(next.getCharacter());
        }
        return true ? sb.toString() : chars.size()+":"+lines.size();
    }
}
