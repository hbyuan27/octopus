package com.octopus;

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Used for unit tests only. The Spring ApplicationContext won't be loaded for tests.<br>
 * <li><b>Disadvantage:</b> Must mock test object and its dependencies manually. <li><b>Advantage:</b> The tests are
 * running much more faster.
 */
@RunWith(SpringRunner.class)
public abstract class AbstractUnitTest {

}
