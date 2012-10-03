package org.jbpm.designer.assets.domain;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@XmlRootElement(name = "assets")
public class AssetsXml
{
   protected Collection<AssetXml> assets = new ArrayList<AssetXml>();
   protected List<LinkXml> links;

   @XmlElementRef
   public Collection<AssetXml> getAssets() {
      return assets;
   }

   public void setAssets(Collection<AssetXml> assets) {
      this.assets = assets;
   }

   @XmlElementRef
   public List<LinkXml> getLinks() {
      return links;
   }

   public void setLinks(List<LinkXml> links) {
      this.links = links;
   }

   @XmlTransient
   public String getNext() {
      if (links == null) return null;
      for (LinkXml link : links)
      {
         if ("next".equals(link.getRelationship())) return link.getHref();
      }
      return null;
   }

   @XmlTransient
   public String getPrevious() {
      if (links == null) return null;
      for (LinkXml link : links)
      {
         if ("previous".equals(link.getRelationship())) return link.getHref();
      }
      return null;
   }

}
