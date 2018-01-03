package com.checkmarx.engine;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles({"test","aws"})
@SpringBootTest
public abstract class SpringUnitTest {

	public static boolean runIntegrationTests() {
		return false;
	}

}
