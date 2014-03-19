/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.quartz.legacy;

import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.scheduling.NexusTask;

import com.google.common.base.Throwables;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Legacy wrapper job. Not a singleton. It lookups legacy task, prepopulates it's parameters, executes it, and returns
 * the parameters into jobDataMap for persisting it, if needed.
 *
 * @since 3.0
 */
@Named
// TODO: I _think_ we have no task param changes, params are changed only by user
// @PersistJobDataAfterExecution
public class LegacyWrapperJob
    implements Job
{
  public static final String LEGACY_JOB_TYPE_KEY = "quartz.legacy.jobType";

  private final QuartzLegacyNexusScheduler quartzLegacyNexusScheduler;

  @Inject
  public LegacyWrapperJob(final QuartzLegacyNexusScheduler quartzLegacyNexusScheduler) {
    this.quartzLegacyNexusScheduler = checkNotNull(quartzLegacyNexusScheduler);
  }

  @Override
  public void execute(final JobExecutionContext context) throws JobExecutionException {
    final NexusTask<?> legacyJob = quartzLegacyNexusScheduler
        .createTaskInstance(context.getJobDetail().getJobDataMap().getString(LEGACY_JOB_TYPE_KEY));
    for (Entry<String, Object> entry : context.getJobDetail().getJobDataMap().entrySet()) {
      legacyJob.addParameter(entry.getKey(), String.valueOf(entry.getValue()));
    }
    try {
      legacyJob.call();
    }
    catch (Exception e) {
      Throwables.propagate(e);
    }
  }
}
