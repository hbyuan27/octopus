package com.octopus;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.octopus.Profiles;

/**
 * This class is designed for helping <b>DEVELOPERS</b>.<br>
 * <li>It provides an integration environment for testing functionalities across modules/classes.<br> <li>The <b>Spring
 * ApplicationContext</b> will be loaded for tests.<br> <li><b>DEV PROFILE</b> is activated by default.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Profiles.TEST)
public abstract class AbstractIntegrationTest {

  @Autowired
  protected TestRestTemplate restTemplate;

}
