package org.jbpm.designer.assets.services;

import static junit.framework.Assert.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.jbpm.designer.assets.persistence.AssetEntity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class DefaultPersistenceTest {

    private EntityManagerFactory emf;
   
    @Rule
    public TestName testName = new TestName();
    
    @Before
    public void before() { 
        if( ! "testPersistenceUnit".equals(testName.getMethodName()) ) { 
            emf = Persistence.createEntityManagerFactory("org.jbpm.designer");
        }
    }
    
    @Test
    public void testPersistenceUnit() { 
        emf = Persistence.createEntityManagerFactory("org.jbpm.designer");
        EntityManager em = emf.createEntityManager();
        em.close();
        emf.close();
    }
    
    @Test
    public void assetEntityTest() { 
       
        EntityManager em = emf.createEntityManager();
        
        AssetEntity assetEntity = new AssetEntity();
        em.getTransaction().begin();
        em.persist(assetEntity);
        em.getTransaction().commit();
        
        assertTrue(assetEntity.getId() > 0);
        
    }
}
