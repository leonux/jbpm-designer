package org.jbpm.designer.test.web.guvnor;

import static org.junit.Assert.*;

import java.net.*;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElement;
import org.apache.abdera.protocol.Response.ResponseType;
import org.apache.abdera.protocol.client.*;
import org.jboss.arquillian.test.api.ArquillianResource;

public class GuvnorTestUtil {

    private Abdera abdera = new Abdera();

    public static GuvnorTestUtil _instance = new GuvnorTestUtil();
    
    private GuvnorTestUtil() { 
        // private constructor 
    }
    
    public static GuvnorTestUtil instance() { 
        return _instance;
    }
    
    public GuvnorTestUtil createPackageViaAtom(URL baseURL, String packageName) 
            throws URISyntaxException, MalformedURLException {
        
        // create package entry
        Entry entry = abdera.newEntry();
        entry.setTitle(packageName);
        entry.setSummary(packageName + " created by " + this.getClass().getSimpleName());

        // Invoke Guvnor REST API 
        AbderaClient client = createAuthorizedClient(baseURL);
        ClientResponse resp = client.post(new URL(baseURL, "rest/packages").toExternalForm(), entry);
        assertEquals(ResponseType.SUCCESS, resp.getType());
        
        return _instance;
    }

    public GuvnorTestUtil testCreateAssetAtom(URL baseURL, String assetName, String packageName, String processName, String categoryName ) throws Exception {
        
        // create process/asset entry
        Entry processEntry = this.createProcessEntry( assetName,
                                                      assetName + " process created by " + this.getClass().getSimpleName(),
                                                      categoryName);

        // Invoke Guvnor REST API
        AbderaClient client = createAuthorizedClient(baseURL);
        RequestOptions options = client.getDefaultRequestOptions();
        options.setContentType( "application/atom+xml" );

        ClientResponse resp = client.post( 
                new URL( baseURL, "rest/packages/" + packageName + "/assets" ).toExternalForm(),
                processEntry,
                options );

        assertTrue("Error creating process asset: " + resp.getStatusText(), resp.getType() != ResponseType.SUCCESS );

        return _instance;
    }
    
    private AbderaClient createAuthorizedClient(URL baseURL) throws URISyntaxException { 
        AbderaClient client = new AbderaClient( abdera );
        client.addCredentials( baseURL.toExternalForm(),
                               null,
                               null,
                               new org.apache.commons.httpclient.UsernamePasswordCredentials( "admin",
                                                                                              "admin" ) );
        return client;
    }

    private Entry createProcessEntry(String title, String summary, String categoryName) {
        Entry processEntry = abdera.newEntry();
        processEntry.setTitle(title);
        processEntry.setSummary(summary);

        // create metadata element
        ExtensibleElement metadataExtension = processEntry.addExtension(new QName("", "metadata"));

        // add format element to metadata
        ExtensibleElement formatExtension = metadataExtension.addExtension(new QName("", "format"));
        formatExtension.addSimpleExtension(new QName("", "value"), "bpmn2");

        // add categories element to metadata
        ExtensibleElement categoriesExtension = metadataExtension.addExtension(new QName("", "categories"));
        categoriesExtension.addSimpleExtension(new QName("", "value"), categoryName);

        return processEntry;
    }

}
