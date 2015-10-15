/*
 *EXHIBIT A - Sun Industry Standards Source License
 *
 *"The contents of this file are subject to the Sun Industry
 *Standards Source License Version 1.2 (the "License");
 *You may not use this file except in compliance with the
 *License. You may obtain a copy of the
 *License at http://wbemservices.sourceforge.net/license.html
 *
 *Software distributed under the License is distributed on
 *an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either
 *express or implied. See the License for the specific
 *language governing rights and limitations under the License.
 *
 *The Original Code is WBEM Services.
 *
 *The Initial Developer of the Original Code is:
 *WBEM Solutions, Inc.
 *
 *Portions created by: Sun Microsystems, Inc.
 *are Copyright (c) 2001 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): 
 *
 *
 */
package javax.wbem.client.adapter.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DtdResolver implements EntityResolver
{
   Hashtable idHash = new Hashtable();
   /* (non-Javadoc)
    * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
    */
   public InputSource resolveEntity(String publicId, String systemId)
         throws SAXException, IOException
   {
      InputSource in = null;
      DtdClReference ref = (DtdClReference)idHash.get(publicId);
      if (ref != null)
      {
         String filename = ref.getDtdFilename();
         ClassLoader cl = ref.getClassLoader();
         InputStream is = cl.getResourceAsStream(filename);
         if (is != null)
         {
            in = new InputSource(is);
         }
      }
      return in;
   }

   /**
    * Allows you to specify a classloader and DTD filename to be loaded
    * when the given publicID is encountered in an XML document.
    * @param publicid
    * @param dtdName 
    * @param loader
    */
   public void registerCatalogEntry(String publicID, String dtdName, 
         ClassLoader loader)
   {
      DtdClReference ref = new DtdClReference();
      ref.setDtdFilename(dtdName);
      ref.setClassLoader(loader);
      idHash.put(publicID, ref);
   }
   
   
   
   private class DtdClReference
   {
      public String dtdFilename = null;
      public ClassLoader classLoader = null;
      /**
       * @return Returns the dtdFilename.
       */
      public String getDtdFilename()
      {
         return dtdFilename;
      }
      /**
       * @param dtdFilename The dtdFilename to set.
       */
      public void setDtdFilename(String dtdFilename)
      {
         this.dtdFilename = dtdFilename;
      }
      /**
       * @return Returns the classLoader.
       */
      public ClassLoader getClassLoader()
      {
         return classLoader;
      }
      /**
       * @param classLoader The classLoader to set.
       */
      public void setClassLoader(ClassLoader classLoader)
      {
         this.classLoader = classLoader;
      }
   }
}
