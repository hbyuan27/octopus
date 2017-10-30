package com.octopus;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.octopus.Profiles;

/**
 * This class is designed for helping <b>DEVELOPERS</b>.<br>
 * <li>It provides an integration environment for testing functionalities across modules/classes.<br> <li>The <b>Spring
 * ApplicationContext</b> will be loaded for tests.<br> <li><b>DEV PROFILE</b> is activated by default.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
// @DataJpaTest -> If enable, regular @Component beans will not be loaded into the ApplicationContext
// add this annotation since @DataJpaTest is disabled
@AutoConfigureTestEntityManager
@Transactional
// @ActiveProfiles activates the identified Spring Profiles and put those beans into test ApplicationContext
@ActiveProfiles(Profiles.TEST)
public abstract class AbstractDataJpaTest {

  @Autowired
  protected TestEntityManager entityManager;

}
