package org.jbpm.designer.test.web;

import static junit.framework.Assert.*;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.http.HttpEntity;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jbpm.designer.Base64EncodingUtil;
import org.jbpm.designer.web.server.JbpmServiceRepositoryServlet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

@RunWith(Arquillian.class)
@RunAsClient
public class ServletTest {

    private static HashMap<String, String> servletMappings = getServletMappings();
    
    @Deployment(testable = false)
    public static WebArchive getTestArchive() {
        Properties properties = new Properties();
        try {
            InputStream arquillianLaunchFile = ServletTest.class.getResourceAsStream("/arquillian.launch");
            properties.load(arquillianLaunchFile);
        } catch (IOException e) {
            // do nada
        }

        // dependencies
//        MavenDependencyResolver resolver = DependencyResolvers.use(MavenDependencyResolver.class);
//        MavenResolutionFilter resolutionFilter = new ScopeFilter("compile", "runtime");
//        File [] dependencyFiles = resolver.includeDependenciesFromPom("pom.xml").resolveAsFiles(resolutionFilter);
        File [] dependencyFiles = null;
        
        WebArchive war = null;
        if (properties.containsKey("jbossas-7")) {
            
            war = ShrinkWrap.create(WebArchive.class)
                    // classes -- do not add "org.jbpm.designer.assets" yet
                    .addPackages(true, "org.jbpm.designer.bpmn2")
                    .addPackages(true, "org.jbpm.designer.epn")
                    .addPackages(true, "org.jbpm.designer.server")
                    .addPackages(true, "org.jbpm.designer.stencilset")
                    .addPackages(true, "org.jbpm.designer.taskforms")
                    .addPackages(true, "org.jbpm.designer.web")
                    .addClass(Base64EncodingUtil.class)
                    // web config
                    .addAsWebInfResource("WEB-INF/web.xml", "web.xml")
                    // persistence
//                  .addAsResource("persistence.xml", "META-INF/persistence.xml")
                    // persistence: ds [jboss specific (because it's not embedded.. :( )]
//                  .addAsWebInfResource("jbossas-ds.xml")
                    .addAsLibraries(dependencyFiles);
        } else {
            properties.list(System.out);
            throw new RuntimeException("No property could be found specifying which arquillian container to start.");
        }

        return war;
    }

    @ArquillianResource
    URL deploymentUrl;

    @Test
    public void testCallServlet() throws Exception {
        String urlPattern = servletMappings.get(JbpmServiceRepositoryServlet.class.getSimpleName());
        post(urlPattern);
    }

    private void post(String urlPattern) throws IllegalStateException, IOException { 
//        HttpClient client = new DefaultHttpClient();
//        HttpPost post = new HttpPost(deploymentUrl + urlPattern );
//
//        HttpEntity responseEntity = client.execute(post).getEntity();
//        assertNotNull("Response entity is null.", responseEntity);
    }
    
    public static String convertToStringAndClose(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            int read;
            while ((read = is.read()) != -1) {
                out.write(read);
            }
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                // do nothing
            }
        }
        return out.toString();
    }
    
    protected static HashMap<String, String> getServletMappings() { 
        String webXmlPath = "/WEB-INF/web.xml";
        InputStream webXmlIn = ServletTest.class.getResourceAsStream(webXmlPath);
        assertNotNull("Could not load " + webXmlPath, webXmlIn);

        HashMap<String, String> servletMappings = new HashMap<String, String>();

        Document document = null;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(webXmlIn);
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
            fail( "Parser not configured correctly when parsing " + webXmlIn + ":" + pce.getMessage());
        } catch (SAXException saxe) {
            saxe.printStackTrace();
            fail( "Unable to parse " + webXmlIn + ":" + saxe.getMessage());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail( "Unable to read " + webXmlIn + ":" + ioe.getMessage());
        }
        
        NodeList nodeList = document.getElementsByTagName("servlet-mapping");

        document.getDocumentElement().normalize();

        for (int index = 0; index < nodeList.getLength(); index++) {
            Node node = nodeList.item(index);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                /**
                 * Example: 
                 * 
                 * <servlet-mapping>
                 *   <servlet-name>FileStoreServlet</servlet-name>
                 *   <url-pattern>/filestore</url-pattern>
                 * </servlet-mapping>
                 */

                if (element.getTagName().equals("servlet-mapping")) {
                    NodeList elemNodeList = element.getChildNodes();
                    String servletName = null, urLPattern = null;
                    for (int i = 0; i < elemNodeList.getLength(); ++i) {
                        Node elemNode = elemNodeList.item(i);
                        if (elemNode.getNodeName().equals("servlet-name")) {
                            servletName = elemNode.getTextContent();
                        } else if (elemNode.getNodeName().equals("url-pattern")) {
                            urLPattern = elemNode.getTextContent().replace("*", "");
                        }
                    }
                    assertNotNull("Could not find servlet-name value.", servletName);
                    assertNotNull("Could not find url-pattern value.", urLPattern);
                    servletMappings.put(servletName, urLPattern);
                }
            }
        }

        return servletMappings;
    }
}
