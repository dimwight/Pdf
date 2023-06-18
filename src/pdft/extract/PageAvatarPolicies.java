package pdft.extract;

import facets.core.app.SViewer;
import facets.core.app.avatar.*;
import facets.core.superficial.app.SSelection;

final class PageAvatarPolicies extends AvatarPolicies{
    public static final int MARGINS = -1;
    @Override
    public AvatarPolicy viewerPolicy(SViewer viewer, AvatarContent content, PainterSource p) {
        return new AvatarPolicy() {
            @Override
            public Painter[] newViewPainters(boolean selected, boolean active) {
                return null;
            }
            @Override
            public Painter[] newPickPainters(Object hit, boolean selected) {
                return null;
            }
        };
    }
}

