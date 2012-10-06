package org.jbpm.designer.web.persistence;

import org.jbpm.designer.web.profile.IDiagramProfile;
import org.jbpm.designer.web.profile.impl.ExternalInfo;

public class PersistenceUtil {

    public static String createURL(IDiagramProfile profile, String pkgName) { 
        String url = ExternalInfo.getExternalProtocol(profile)
                + "://"
                + ExternalInfo.getExternalHost(profile)
                + "/"
                + profile.getExternalLoadURLSubdomain().substring(0,
                        profile.getExternalLoadURLSubdomain().indexOf("/"))
                + "/rest/packages/"
                + pkgName 
                + "/assets/";
        return url;
    }
    
}
