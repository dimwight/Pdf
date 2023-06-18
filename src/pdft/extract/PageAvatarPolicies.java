package pdft.extract;

import applicable.textart.TextArt;
import facets.core.app.SViewer;
import facets.core.app.avatar.*;

final class PageAvatarPolicies extends AvatarPolicies{
    public static final int MARGINS = -1;
    @Override
    public AvatarPolicy viewerPolicy(SViewer viewer,
                                     AvatarContent content,
                                     PainterSource p) {
        TextArt line=(TextArt)content;
        return new AvatarPolicy() {
            @Override
            public Painter[] newViewPainters(boolean selected, boolean active) {
                return new Painter[]{
                        p.textOutline(line.text(),
                                line.fontFace(),
                                line.fontSize(),
                                line.fontIsBold(),
                                line.fontIsItalic(),
                                line.shade(),
                                null)};
            }
            @Override
            public Painter[] newPickPainters(Object hit, boolean selected) {
                return null;
            }
        };
    }
}

