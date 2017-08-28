package com.checkmarx.engine.aws;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.amazonaws.services.ec2.model.Instance;
import com.checkmarx.engine.manager.EngineProvisioner;
import com.checkmarx.engine.manager.EngineProvisioner.CxServerRole;
import com.google.common.collect.Lists;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AwsEc2ClientTests {
	
	private static final Logger log = LoggerFactory.getLogger(AwsEc2ClientTests.class);
	
	private final boolean runTest = false;  //uncomment next line to run this test
	//private final boolean runTest = true;
	
	@Autowired
	private AwsComputeClient ec2Client;
	
	@Autowired
	private AwsEngineConfig config;

	private final List<String> instances = Lists.newArrayList();
		
	@Before
	public void setUp() throws Exception {
		log.trace("setup()");
	
		assertThat(ec2Client, is(notNullValue()));
		assertThat(config, is(notNullValue()));
	}
	
	@After
	public void tearDown() throws Exception {
		for (String instance : instances) {
			ec2Client.terminate(instance);
		}
	}

	@Test
	public void testLaunch() {
		log.trace("testLaunch()");
		
		Assume.assumeTrue(runTest);

		final String name = "cx-test1";
		final String instanceType = "t2.small";
		//final String instanceType = "m4.large";
		final String version = "8.5.0-RC3";
		final CxServerRole role = CxServerRole.ENGINE;
		
		final Map<String, String> tags = AwsEngines.createCxTags(role, version);
		
		final Instance instance = ec2Client.launch(name, instanceType, tags);
		
		assertNotNull(instance);
		assertThat(Ec2.getName(instance), is(name));
		assertThat(Ec2.getTag(instance, EngineProvisioner.CX_ROLE_TAG), is(role.toString()));
		assertThat(Ec2.getTag(instance, EngineProvisioner.CX_VERSION_TAG), is(version));
		assertEquals(instanceType, instance.getInstanceType());
		assertEquals(config.getKeyName(), instance.getKeyName());
		assertEquals(config.getImageId(), instance.getImageId());
		assertEquals(config.getSubnetId(), instance.getSubnetId());
		assertEquals(config.getSecurityGroup(), instance.getSecurityGroups().get(0).getGroupId());
	}
	
	@Test
	public void testTerminate() {
		log.trace("testTerminate()");
		
		Assume.assumeTrue(runTest);

		final String instanceId = "i-05bb6ec6c7aba753c";
		ec2Client.terminate(instanceId);
	}

	@Test
	public void testDescribe() {
		log.trace("testDescribe()");

		Assume.assumeTrue(runTest);

		final String instanceId = "i-0e34e0f752b8ac04f";
		
		final Instance instance = ec2Client.describe(instanceId);
		assertThat(instance, notNullValue());
		//log.debug("{}", AwsUtils.printInstanceDetails(instance, "CxVersion", "CxRole"));
		assertThat(instance.getInstanceId(), is(instanceId));
		assertThat(Ec2.getName(instance), is(notNullValue()));
	}
	
	@Test
	public void testListAllInstances() {
		log.trace("testListAllInstances()");

		List<Instance> instances = ec2Client.find(null, new String[0]);
		assertThat(instances, is(notNullValue()));
		assertThat(instances.isEmpty(), is(false));
		
		int i = 0;
		for(Instance instance : instances) {
			log.debug("{} : name={}; {}", ++i, Ec2.getName(instance), Ec2.print(instance));
		}
	}

	@Test
	public void testListInstances() {
		log.trace("testListInstances()");

		List<Instance> instances = ec2Client.find("cx-role", "MANAGER");
		assertThat(instances, is(notNullValue()));
		assertThat(instances.isEmpty(), is(false));
		
		int i = 0;
		for(Instance instance : instances) {
			log.debug("{} : name={}; {}", i++, Ec2.getName(instance), Ec2.print(instance));
		}
	}

}
