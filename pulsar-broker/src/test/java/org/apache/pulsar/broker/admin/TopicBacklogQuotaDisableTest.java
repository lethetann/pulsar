/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pulsar.broker.admin;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.broker.auth.MockedPulsarServiceBaseTest;
import org.apache.pulsar.broker.service.BacklogQuotaManager;
import org.apache.pulsar.client.admin.PulsarAdminException;
import org.apache.pulsar.common.naming.TopicName;
import org.apache.pulsar.common.policies.data.BacklogQuota;
import org.apache.pulsar.common.policies.data.ClusterData;
import org.apache.pulsar.common.policies.data.RetentionPolicies;
import org.apache.pulsar.common.policies.data.TenantInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Slf4j
public class TopicBacklogQuotaDisableTest extends MockedPulsarServiceBaseTest {

    private static final Logger LOG = LoggerFactory.getLogger(TopicBacklogQuotaDisableTest.class);

    private final String testTenant = "my-tenant";

    private final String testNamespace = "my-namespace";

    private final String myNamespace = testTenant + "/" + testNamespace;

    private final String backlogQuotaTopic = "persistent://" + myNamespace + "/test-set-backlog-quota";

    @BeforeMethod
    @Override
    protected void setup() throws Exception {
        this.conf.setSystemTopicEnabled(true);
        this.conf.setTopicLevelPoliciesEnabled(false);
        super.internalSetup();

        admin.clusters().createCluster("test", new ClusterData(pulsar.getWebServiceAddress()));
        TenantInfo tenantInfo = new TenantInfo(Sets.newHashSet("role1", "role2"), Sets.newHashSet("test"));
        admin.tenants().createTenant(this.testTenant, tenantInfo);
        admin.namespaces().createNamespace(testTenant + "/" + testNamespace, Sets.newHashSet("test"));
        admin.topics().createPartitionedTopic(backlogQuotaTopic, 2);
    }

    @AfterMethod
    @Override
    public void cleanup() throws Exception {
        super.internalCleanup();
    }

    @Test
    public void testBacklogQuotaDisabled() throws Exception {
        BacklogQuota backlogQuota = new BacklogQuota(1024, BacklogQuota.RetentionPolicy.consumer_backlog_eviction);
        log.info("Backlog quota: {} will set to the topic: {}", backlogQuota, backlogQuotaTopic);

        try {
            admin.topics().setBacklogQuota(backlogQuotaTopic, backlogQuota);
            Assert.fail();
        } catch (PulsarAdminException e) {
            Assert.assertEquals(e.getStatusCode(), 405);
        }

        try {
            admin.topics().removeBacklogQuota(backlogQuotaTopic);
            Assert.fail();
        } catch (PulsarAdminException e) {
            Assert.assertEquals(e.getStatusCode(), 405);
        }

        try {
            admin.topics().getBacklogQuotaMap(backlogQuotaTopic);
            Assert.fail();
        } catch (PulsarAdminException e) {
            Assert.assertEquals(e.getStatusCode(), 405);
        }
    }
}
