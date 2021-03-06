package org.camunda.bpm.container.impl.jobexecutor.ra.inflow;


import javax.resource.ResourceException;
import javax.resource.spi.endpoint.MessageEndpointFactory;

import org.camunda.bpm.container.impl.jobexecutor.ra.PlatformJobExecutorConnector;

/**
 * Represents the activation of a {@link JobExecutionHandler}
 * 
 * @author Daniel Meyer
 * 
 */
public class JobExecutionHandlerActivation {

  protected PlatformJobExecutorConnector ra;

  protected JobExecutionHandlerActivationSpec spec;

  protected MessageEndpointFactory endpointFactory;

  public JobExecutionHandlerActivation() throws ResourceException {
    this(null, null, null);
  }

  public JobExecutionHandlerActivation(PlatformJobExecutorConnector ra, MessageEndpointFactory endpointFactory, JobExecutionHandlerActivationSpec spec) throws ResourceException {
    this.ra = ra;
    this.endpointFactory = endpointFactory;
    this.spec = spec;
  }

  public JobExecutionHandlerActivationSpec getActivationSpec() {
    return spec;
  }

  public MessageEndpointFactory getMessageEndpointFactory() {
    return endpointFactory;
  }

  public void start() throws ResourceException {
    // nothing to do here
  }

  public void stop() {
    // nothing to do here
  }

}
