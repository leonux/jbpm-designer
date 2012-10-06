package org.jbpm.designer.test.web;

import static org.junit.Assert.assertEquals;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import javax.ws.rs.core.MediaType;

import org.drools.util.codec.Base64;
import org.jboss.arquillian.container.test.api.*;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.ResolutionStrategy;
import org.jboss.shrinkwrap.resolver.api.maven.*;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependency;
import org.jboss.shrinkwrap.resolver.api.maven.filter.MavenResolutionFilter;
import org.jboss.shrinkwrap.resolver.api.maven.filter.ScopeFilter;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.AcceptScopesStrategy;
import org.jboss.shrinkwrap.resolver.impl.maven.MavenWorkingSession;
import org.jboss.shrinkwrap.resolver.impl.maven.PomEquippedResolveStageBaseImpl;
import org.jbpm.designer.Base64EncodingUtil;
import org.jbpm.designer.test.web.guvnor.GuvnorTestUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@RunAsClient
public class GuvnorIntegrationTest {

    private static final String guvnorVersion = "5.5.0-SNAPSHOT";
    private static final String jbossAS7LaunchPropName = "jbossas-7";

    private static Properties launchProps;
    static {
        try {
            launchProps = new Properties();
            InputStream arquillianLaunchFile = GuvnorIntegrationTest.class.getResourceAsStream("/arquillian.launch");
            launchProps.load(arquillianLaunchFile);
            
            launchProps.list(System.out);
        } catch (IOException e) {
            // do nada
        }
    }

    @Deployment(name = "guvnor", order = 1)
    public static WebArchive getGuvnorWar() {
        String guvnorWarName = "drools-guvnor.war";
        System.out.println(" << creating " + guvnorWarName + " deployment");
        
        WebArchive war = null;
        if (launchProps.containsKey(jbossAS7LaunchPropName)) { 
            // dependencies
            File[] guvnorAs7WarFiles = Maven.resolver()
                    .resolve("org.drools:guvnor-distribution-wars:war:jboss-as-7.0:" + guvnorVersion)
                    .offline().withoutTransitivity()
                    .as(File.class);
            // Import/deploy the guvnor jboss AS 7 war
            war = ShrinkWrap.create(ZipImporter.class, guvnorWarName).importFrom(guvnorAs7WarFiles[0]).as(WebArchive.class);
        } else if (launchProps.containsKey("glassfish-embedded")) {
            throw new RuntimeException("Glassfish embpreprocessingUnit.preprocesimportRuntimeDependenciess(request, response, profile);edded test not yet implemented!");
        } else {
            launchProps.list(System.out);
            throw new RuntimeException("No property could be found specifying which arquillian container to start.");
        }

        System.out.println(" >> " + guvnorWarName + " deployment created");
        return war;
    }

    @Deployment(name = "designer", order = 2)
    public static WebArchive getDesignerWar() {
        String designerWarName = "designer.war";
        System.out.println(" << creating " + designerWarName + " deployment");
        
        File[] dependencyFiles = resolveRuntimeDependenciesOffLine("designer/guvnor-servlets-pom.xml");

        WebArchive war = null;
        if (launchProps.containsKey(jbossAS7LaunchPropName)) { 

            war = ShrinkWrap
                    .create(WebArchive.class, designerWarName)
                    // classes -- do not add "org.jbpm.designer.assets" yet
                    .addPackages(true, "org.jbpm.designer.bpmn2")
                    .addPackages(true, "org.jbpm.designer.server")
                    .addPackages(true, "org.jbpm.designer.stencilset")
                    .addPackages(true, "org.jbpm.designer.taskforms")
                    .addPackages(true, "org.jbpm.designer.web")
                    .addClass(Base64EncodingUtil.class)
                    // web config
                    .addAsWebInfResource("WEB-INF/web.xml", "designer/guvnor-servlets-web.xml")
                    .addAsWebInfResource("WEB-INF/jboss-web.xml", "jboss-web.xml")
                    // persistence
                    // .addAsResource("persistence.xml", "META-INF/persistence.xml")
                    // persistence: ds [jboss specific (because it's not embedded.. :( )]
                    // .addAsWebInfResource("jbossas-ds.xml")
                    .addAsLibraries(dependencyFiles);
        } else if (launchProps.containsKey("glassfish-embedded")) {
            throw new RuntimeException("Glassfish embedded test not yet implemented!");
        } else {
            launchProps.list(System.out);
            throw new RuntimeException("No property could be found specifying which arquillian container to start.");
        }
        
        System.out.println(" >> " + designerWarName + " deployment created");

        return war;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static File [] resolveRuntimeDependenciesOffLine(String pomXmlResourcePath) {

        PomEquippedResolveStageBaseImpl pers = (PomEquippedResolveStageBaseImpl) Maven.resolver().loadPomFromClassLoaderResource(pomXmlResourcePath);
        
        ScopeType[] runtmeScopes = new ScopeType[] { ScopeType.COMPILE, ScopeType.IMPORT, ScopeType.RUNTIME, ScopeType.SYSTEM };
        addRuntimeDependencies(pers, runtmeScopes);
        
        // 1. pers.resolve calls .createStrategyStage();
        // 2. .offline() fails unless we call addRuntimeDependencies(...) above
        MavenStrategyStageBase mssb = pers.resolve().offline(); 
        
        ResolutionStrategy runtimeStrategy = new AcceptScopesStrategy(runtmeScopes);
        File[] dependencyFiles = mssb.using(runtimeStrategy).as(File.class);

        return dependencyFiles;
    }

    @SuppressWarnings("rawtypes")
    private static void addRuntimeDependencies(PomEquippedResolveStageBaseImpl pers, ScopeType [] runtimeScopes) {
        // Get all declared dependencies
        final MavenWorkingSession session = pers.getMavenWorkingSession();
        final List<MavenDependency> dependencies = new ArrayList<MavenDependency>(session.getDeclaredDependencies());

        // Filter by scope
        final MavenResolutionFilter preResolutionFilter = new ScopeFilter(runtimeScopes);

        // For all declared dependencies which pass the filter, add 'em to the set of dependencies to be resolved for this request
        ArrayList<MavenDependency> EMPTY_LIST = new ArrayList<MavenDependency>(0);
        for (final MavenDependency candidate : dependencies) {
            if (preResolutionFilter.accepts(candidate, EMPTY_LIST)) {
                session.getDependenciesForResolution().add(candidate);
            }
        }
    }
    

    @Test
    @InSequence(0)
    @OperateOnDeployment("guvnor")
    public void fillGuvnor(@ArquillianResource URL guvnorUrl) throws Exception { 
        GuvnorTestUtil.instance()
            .createPackageViaAtom(guvnorUrl, "package1");
    }
    
//    @Test
//    @InSequence(1)
//    @OperateOnDeployment("designer")
    public void testJbpmPreprocessingUnitPreprocess(@ArquillianResource URL designerUrl) throws Exception {
       
        // TODO: retrieve the UUID of an asset from Guvnor
        String uuid = null;
        
        
        String editorUrlString = "/editor/?uuid=" + uuid + "&profile=jbpm";
        
        URL url = new URL(designerUrl, editorUrlString);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "Basic " + new Base64().encodeToString(("admin:admin".getBytes())));
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", MediaType.APPLICATION_XML);
        connection.connect();

        assertEquals(200, connection.getResponseCode());
        assertEquals(MediaType.APPLICATION_XML, connection.getContentType());
    }

  
}
