package test.com.popoaichuiniu.intentGen;

import com.popoaichuiniu.intentGen.Intent;
import com.popoaichuiniu.intentGen.IntentExtraKey;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertTrue;

/**
 * Intent Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Nov 12, 2018</pre>
 */

@RunWith(Parameterized.class)
public class IntentTest {

    private Intent intent1;
    private Intent intent2;
    private boolean expected;

    public IntentTest(Intent intent1, Intent intent2, boolean expected) {
        this.intent1 = intent1;
        this.intent2 = intent2;
        this.expected = expected;
    }

    @Before
    public void before() throws Exception {


    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: equals(Object o)
     */
    @Test
    public void testEquals() throws Exception {
//TODO: Test goes here...

        System.out.println("testEquals");
    }

    /**
     * java
     * <p>
     * Method: equivExtraTo(Set<IntentExtraKey> set1, Set<IntentExtraKey> set2)
     */
    @Test
    public void testEquivExtraTo() throws Exception {

        System.out.println("testEquivExtraTo");
//TODO: Test goes here... 
    }

    /**
     * Method: equivCategoryTo(Set<String> cateSet1, Set<String> cateSet2)
     */
    @Test
    public void testEquivCategoryTo() throws Exception {
//TODO: Test goes here...
        System.out.println("testEquivCategoryTo");
    }

    /**
     * Method: hashCode()
     */
    @Test
    public void testHashCode() throws Exception {
//TODO: Test goes here...
        System.out.println("testHashCode");
    }

    /**
     * Method: toString()
     */
    @Test
    public void testToString() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: initialIntentExtraValueSet()
     */
    @Test
    public void testInitialIntentExtraValueSet() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: contains(Intent intent)
     */
    @Test
    public void testContains() throws Exception {

        if (expected != intent1.contains(intent2)) {
            Assert.fail();
        }

        System.out.println("hello");


//TODO: Test goes here...
    }


    @Parameterized.Parameters
    public static Collection<Object[]> data() {

        Intent intent1 = new Intent();
        Intent intent2 = new Intent();
        boolean expected = true;

        Intent intent11 = new Intent();
        Intent intent22 = new Intent();
        intent11.action = "ttt";
        boolean expectedd = true;


        Intent intent111 = new Intent();
        Intent intent222 = new Intent();
        intent222.action = "zzz";
        boolean expecteddd = false;


        Intent intent4 = new Intent();
        Intent intent5 = new Intent();
        intent4.categories.add("sss");
        boolean expected4 = true;


        Intent intent44 = new Intent();
        Intent intent55 = new Intent();
        intent55.categories.add("sss");
        boolean expected44 = false;


        Intent intent66 = new Intent();
        Intent intent77 = new Intent();
        intent66.myExtras.add(new IntentExtraKey("xxx", "string", "ttt"));
        boolean expected66 = true;


        Intent intent666 = new Intent();
        Intent intent777 = new Intent();
        intent777.myExtras.add(new IntentExtraKey("xxx", "string", "ttt"));
        boolean expected666 = false;

        return Arrays.asList(new Object[][]{
                {intent1, intent2, expected},
                {intent11, intent22, expectedd},
                {intent111, intent222, expecteddd},

                {intent4, intent5, expected4},
                {intent44, intent55, expected44},

                {intent66, intent77, expected66},

                {intent666, intent777, expected666}


        });

    }
}
