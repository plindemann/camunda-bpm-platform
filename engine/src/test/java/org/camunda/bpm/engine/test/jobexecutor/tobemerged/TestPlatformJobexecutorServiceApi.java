package org.camunda.bpm.engine.test.jobexecutor.tobemerged;

import java.util.UUID;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.impl.DefaultPlatformJobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.impl.acquisition.JobAcquisition;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.impl.util.JobAcquisitionConfigurationBean;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.spi.JobAcquisitionStrategy;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * 
 * @author Daniel Meyer
 */
public class TestPlatformJobexecutorServiceApi {
  
  protected DefaultPlatformJobExecutor platformJobExecutor;

  @Before
  public void initPlaformJobExecutor() {
    
    platformJobExecutor = new DefaultPlatformJobExecutor();
    
    JobAcquisitionConfigurationBean acquisitionConfiguration = new JobAcquisitionConfigurationBean();
    acquisitionConfiguration.setAcquisitionName("default");
    acquisitionConfiguration.setJobAcquisitionStrategy(JobAcquisitionStrategy.SEQUENTIAL);
    acquisitionConfiguration.setLockOwner(UUID.randomUUID().toString());
    acquisitionConfiguration.setLockTimeInMillis(5*60*1000);
    acquisitionConfiguration.setMaxJobsPerAcquisition(3);
    acquisitionConfiguration.setWaitTimeInMillis(5*1000);
    
    // start the platform job executor
    platformJobExecutor.start();
    platformJobExecutor.startJobAcquisition(acquisitionConfiguration);
  }
  
  @After
  public void stopPlatformJobexecutor() {
    platformJobExecutor.stop();
  }
  
  @Test
  public void testRegisterSingleProcessEngine() {
    // configure and build a process engine
    StandaloneProcessEngineConfiguration standaloneProcessEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    standaloneProcessEngineConfiguration.setProcessEngineName(getClass().getName()+"-enine");
    standaloneProcessEngineConfiguration.setJdbcUrl("jdbc:h2:mem:jobexecutor-test-engine");
    standaloneProcessEngineConfiguration.setJobExecutorActivate(false);
    ProcessEngine engine = standaloneProcessEngineConfiguration.buildProcessEngine();
    
    // register the process engine with the platform job executor
    JobExecutor jobExecutor = platformJobExecutor.registerProcessEngine(standaloneProcessEngineConfiguration, "default");
    standaloneProcessEngineConfiguration.setJobExecutor(jobExecutor);
        
    // assert that we obtain an active job executor for the registered process engine.
    Assert.assertTrue(jobExecutor.isActive());
    
    // if we unregister the last process engine, the acquisition is stopped:
    platformJobExecutor.unregisterProcessEngine(standaloneProcessEngineConfiguration, "default");
    Assert.assertFalse(jobExecutor.isActive());
    
    engine.close();
  }
  
  @Test
  public void testUnregisterForUnexistingAcquisitionFails() {
    // configure and build a process engine
    StandaloneProcessEngineConfiguration standaloneProcessEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    standaloneProcessEngineConfiguration.setProcessEngineName(getClass().getName()+"-enine");
    standaloneProcessEngineConfiguration.setJdbcUrl("jdbc:h2:mem:jobexecutor-test-engine");
    standaloneProcessEngineConfiguration.setJobExecutorActivate(false);
    ProcessEngine engine = standaloneProcessEngineConfiguration.buildProcessEngine();
    try {
      platformJobExecutor.unregisterProcessEngine(standaloneProcessEngineConfiguration, "unexisting");
      // expected
    } catch (ProcessEngineException e) {
      Assert.fail();
    }
    engine.close();
  }
  
  @Test
  public void testRegisterForUnexistingAcquisitionFails() {
    // configure and build a process engine
    StandaloneProcessEngineConfiguration standaloneProcessEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    standaloneProcessEngineConfiguration.setProcessEngineName(getClass().getName()+"-enine");
    standaloneProcessEngineConfiguration.setJdbcUrl("jdbc:h2:mem:jobexecutor-test-engine");
    standaloneProcessEngineConfiguration.setJobExecutorActivate(false);
    ProcessEngine engine = standaloneProcessEngineConfiguration.buildProcessEngine();
    try {
      platformJobExecutor.registerProcessEngine(standaloneProcessEngineConfiguration, "unexisting");
      Assert.fail();
    } catch (ProcessEngineException e) {
      // expected
    }
    engine.close();
  }
  
  @Test
  public void testRegisterTwiceFails() {
    // configure and build a process engine
    StandaloneProcessEngineConfiguration standaloneProcessEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    standaloneProcessEngineConfiguration.setProcessEngineName(getClass().getName()+"-enine");
    standaloneProcessEngineConfiguration.setJdbcUrl("jdbc:h2:mem:jobexecutor-test-engine");
    standaloneProcessEngineConfiguration.setJobExecutorActivate(false);
    ProcessEngine engine = standaloneProcessEngineConfiguration.buildProcessEngine();
    
    platformJobExecutor.registerProcessEngine(standaloneProcessEngineConfiguration, "default");
    
    try {
      platformJobExecutor.registerProcessEngine(standaloneProcessEngineConfiguration, "default");
      Assert.fail();
    }catch (ProcessEngineException e) {
      // expected
    }    
    
    platformJobExecutor.unregisterProcessEngine(standaloneProcessEngineConfiguration, "default");
    engine.close();
  }
  

  @Test
  public void testUnRegisterUnexistingFails() {
    // configure and build a process engine
    StandaloneProcessEngineConfiguration standaloneProcessEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    standaloneProcessEngineConfiguration.setProcessEngineName(getClass().getName()+"-enine");
    standaloneProcessEngineConfiguration.setJdbcUrl("jdbc:h2:mem:jobexecutor-test-engine");
    standaloneProcessEngineConfiguration.setJobExecutorActivate(false);
    ProcessEngine engine = standaloneProcessEngineConfiguration.buildProcessEngine();
    
    try {
      platformJobExecutor.unregisterProcessEngine(standaloneProcessEngineConfiguration, "default");
      Assert.fail();
    }catch (ProcessEngineException e) {
      // expected
    }    
    
    engine.close();
  }
  
  @Test
  public void testRegisterTwoEngines() {
    // configure and build a process engine
    StandaloneProcessEngineConfiguration engineConfiguration1 = new StandaloneInMemProcessEngineConfiguration();
    engineConfiguration1.setProcessEngineName(getClass().getName()+"engine1");    
    engineConfiguration1.setJdbcUrl("jdbc:h2:mem:activiti1");
    engineConfiguration1.setJobExecutorActivate(false);
    ProcessEngine engine1 = engineConfiguration1.buildProcessEngine();
    
    // and a second one
    StandaloneProcessEngineConfiguration engineConfiguration2 = new StandaloneInMemProcessEngineConfiguration();
    engineConfiguration2.setProcessEngineName("engine2");
    engineConfiguration2.setJdbcUrl("jdbc:h2:mem:activiti2");
    engineConfiguration2.setJobExecutorActivate(false);
    ProcessEngine engine2 = engineConfiguration2.buildProcessEngine();
    
    // register the first process engine with the platform job executor
    JobExecutor jobExecutor1 = platformJobExecutor.registerProcessEngine(engineConfiguration1, "default");
    engineConfiguration1.setJobExecutor(jobExecutor1);
        
    // assert that we obtain an active job executor for the registered process engine.
    Assert.assertTrue(jobExecutor1.isActive());
    
    // register the second process engine with the platform job executor
    JobExecutor jobExecutor2 = platformJobExecutor.registerProcessEngine(engineConfiguration2, "default");
    engineConfiguration2.setJobExecutor(jobExecutor2);
        
    // jobexecutor active
    Assert.assertTrue(jobExecutor1.isActive());
    Assert.assertTrue(jobExecutor2.isActive());
    
    // if we unregister the first process engine, the acquisition is not stopped:
    platformJobExecutor.unregisterProcessEngine(engineConfiguration2, "default");
    Assert.assertTrue(jobExecutor2.isActive());
    Assert.assertTrue(jobExecutor1.isActive());
    
    // if we unregister the last process engine, the acquisition is stopped:
    platformJobExecutor.unregisterProcessEngine(engineConfiguration1, "default");
    Assert.assertFalse(jobExecutor2.isActive());
    Assert.assertFalse(jobExecutor1.isActive());
    
    engine1.close();
    engine2.close();
  }
  
  @Test
  public void testRemoveUnexistingJobAcquisitionFailsSilently() {
    try {
      platformJobExecutor.stopJobAcquisition("unexisting");  
      // expected
    }catch (ProcessEngineException e) {
      Assert.fail();   
    }       
  }
  
  @Test
  public void testAddRemoveJobAcquisition() {
    JobAcquisitionConfigurationBean acquisitionConfiguration = new JobAcquisitionConfigurationBean();
    acquisitionConfiguration.setAcquisitionName("acquisition2");
    acquisitionConfiguration.setJobAcquisitionStrategy(JobAcquisitionStrategy.SEQUENTIAL);
    acquisitionConfiguration.setLockOwner(UUID.randomUUID().toString());
    acquisitionConfiguration.setLockTimeInMillis(5*60*1000);
    acquisitionConfiguration.setMaxJobsPerAcquisition(3);
    acquisitionConfiguration.setWaitTimeInMillis(5*1000);
    
    Assert.assertEquals(1, platformJobExecutor.getJobAcquisitions().size());
    
    platformJobExecutor.startJobAcquisition(acquisitionConfiguration);
    
    Assert.assertEquals(2, platformJobExecutor.getJobAcquisitions().size());
    JobAcquisition jobAcquisition = platformJobExecutor.getJobAcquisitionByName("acquisition2");
    Assert.assertNotNull(jobAcquisition);
    
    platformJobExecutor.stopJobAcquisition("acquisition2");
    Assert.assertEquals(1, platformJobExecutor.getJobAcquisitions().size());
    
    try {
      platformJobExecutor.getJobAcquisitionByName("acquisition2");  
      Assert.fail();      
    }catch (ProcessEngineException e) {
      // expected
    }
  }
    
  @Test
  public void testAddJobAcquisitionWithExistingNameFails() {
    JobAcquisitionConfigurationBean acquisitionConfiguration = new JobAcquisitionConfigurationBean();
    acquisitionConfiguration.setAcquisitionName("default");
    acquisitionConfiguration.setJobAcquisitionStrategy(JobAcquisitionStrategy.SEQUENTIAL);
    acquisitionConfiguration.setLockOwner(UUID.randomUUID().toString());
    acquisitionConfiguration.setLockTimeInMillis(5*60*1000);
    acquisitionConfiguration.setMaxJobsPerAcquisition(3);
    acquisitionConfiguration.setWaitTimeInMillis(5*1000);
    
    Assert.assertEquals(1, platformJobExecutor.getJobAcquisitions().size());
    
    try{
      platformJobExecutor.startJobAcquisition(acquisitionConfiguration);    
      Assert.fail();      
    }catch (ProcessEngineException e) {
      // expected
    }
  }

}
