import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.junit.Assert;
import org.junit.Test;

public class MyTest {
	@Test
	public void test() {
		//Assert.assertEquals(false, true);
		Assert.assertEquals(true, true);
	}

	@Test
	public void test2() {
		//Assert.assertEquals(false, true);
		Assert.assertEquals(false, false);
	}
}
