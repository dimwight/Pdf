package pdft.extract;

import facets.core.app.avatar.AvatarPolicies;
import facets.core.superficial.app.SSelection;

final class PageAvatarPolicies extends AvatarPolicies{

    public static final int MARGINS = -1;

    public SSelection newViewerSelection() {
        return new SSelection() {
            @Override
            public Object content() {
                return null;
            }

            @Override
            public Object single() {
                return null;
            }

            @Override
            public Object[] multiple() {
                return new Object[0];
            }
        };
    }
}

