/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.internal.executor;

import com.dangdang.ddframe.job.api.config.JobConfiguration;
import com.dangdang.ddframe.job.api.config.JobConfigurationFactory;
import com.dangdang.ddframe.job.api.listener.AbstractDistributeOnceElasticJobListener;
import com.dangdang.ddframe.job.api.listener.fixture.ElasticJobListenerCaller;
import com.dangdang.ddframe.job.api.listener.fixture.TestDistributeOnceElasticJobListener;
import com.dangdang.ddframe.job.api.listener.fixture.TestElasticJobListener;
import com.dangdang.ddframe.job.fixture.TestJob;
import com.dangdang.ddframe.job.internal.schedule.SchedulerFacade;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.SchedulerException;
import org.unitils.util.ReflectionUtils;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

public final class JobExecutorTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private SchedulerFacade schedulerFacade;
    
    @Mock
    private ElasticJobListenerCaller caller;
    
    private JobConfiguration jobConfig = JobConfigurationFactory.createSimpleJobConfigurationBuilder("testJob", TestJob.class, 3, "0/1 * * * * ?").build();
    
    private JobExecutor jobExecutor = new JobExecutor(regCenter, jobConfig);
    
    @Before
    public void initMocks() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(jobExecutor, "regCenter", regCenter);
        ReflectionUtils.setFieldValue(jobExecutor, "schedulerFacade", schedulerFacade);
    }
    
    @Test
    public void testNew() throws NoSuchFieldException {
        TestDistributeOnceElasticJobListener testDistributeOnceElasticJobListener = new TestDistributeOnceElasticJobListener(caller);
        assertNull(ReflectionUtils.getFieldValue(testDistributeOnceElasticJobListener, ReflectionUtils.getFieldWithName(AbstractDistributeOnceElasticJobListener.class, "guaranteeService", false)));
        JobExecutor actualJobExecutor = new JobExecutor(null, jobConfig, new TestElasticJobListener(caller), testDistributeOnceElasticJobListener);
        assertNotNull(ReflectionUtils.getFieldValue(testDistributeOnceElasticJobListener, ReflectionUtils.getFieldWithName(AbstractDistributeOnceElasticJobListener.class, "guaranteeService", false)));
        assertThat(ReflectionUtils.getFieldValue(actualJobExecutor, ReflectionUtils.getFieldWithName(JobExecutor.class, "elasticJob", false)), instanceOf(jobConfig.getJobClass()));
    }
    
    @Test
    public void assertInit() throws NoSuchFieldException, SchedulerException {
        jobExecutor.init();
        verify(schedulerFacade).clearPreviousServerStatus();
        verify(regCenter).addCacheData("/testJob");
        verify(schedulerFacade).registerStartUpInfo();
    }
}
