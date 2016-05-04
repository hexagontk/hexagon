package hexagon.benchmark;

import java.io.IOException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * <p>Stress tests. Perform application tests in parallel several times.
 *
 * @author jamming
 */
@Test (enabled = false, threadPoolSize = 16, invocationCount = 75)
public final class ApplicationStressTest {
    private static final int WARM_UP = 10;

//    private final ApplicationTest tests = new ApplicationTest ();
//
//    @BeforeClass public void setup () throws IOException {
//        tests.setup ();
//
//        for (int ii = 0; ii < WARM_UP; ii++) {
//            tests.json ();
//            tests.plaintext ();
//            tests.no_query_parameter ();
//            tests.empty_query_parameter ();
//            tests.text_query_parameter ();
//            tests.zero_queries ();
//            tests.one_thousand_queries ();
//            tests.one_query ();
//            tests.ten_queries ();
//            tests.one_hundred_queries ();
//            tests.five_hundred_queries ();
//            tests.fortunes ();
//            tests.no_updates_parameter ();
//            tests.empty_updates_parameter ();
//            tests.text_updates_parameter ();
//            tests.zero_updates ();
//            tests.one_thousand_updates ();
//            tests.one_update ();
//            tests.ten_updates ();
//            tests.one_hundred_updates ();
//            tests.five_hundred_updates ();
//        }
//    }
//
//    @AfterClass public void close () {
//        tests.close ();
//    }
//
//    public void stress_json () throws IOException {
//        tests.json ();
//    }
//
//    public void stress_plaintext () throws IOException {
//        tests.plaintext ();
//    }
//
//    public void stress_no_query_parameter () throws IOException {
//        tests.no_query_parameter ();
//    }
//
//    public void stress_empty_query_parameter () throws IOException {
//        tests.empty_query_parameter ();
//    }
//
//    public void stress_text_query_parameter () throws IOException {
//        tests.text_query_parameter ();
//    }
//
//    public void stress_zero_queries () throws IOException {
//        tests.zero_queries ();
//    }
//
//    public void stress_one_thousand_queries () throws IOException {
//        tests.one_thousand_queries ();
//    }
//
//    public void stress_one_query () throws IOException {
//        tests.one_query ();
//    }
//
//    public void stress_ten_queries () throws IOException {
//        tests.ten_queries ();
//    }
//
//    public void stress_one_hundred_queries () throws IOException {
//        tests.one_hundred_queries ();
//    }
//
//    public void stress_five_hundred_queries () throws IOException {
//        tests.five_hundred_queries ();
//    }
//
//    public void stress_fortunes () throws IOException {
//        tests.fortunes ();
//    }
//
//    public void stress_no_updates_parameter () throws IOException {
//        tests.no_updates_parameter ();
//    }
//
//    public void stress_empty_updates_parameter () throws IOException {
//        tests.empty_updates_parameter ();
//    }
//
//    public void stress_text_updates_parameter () throws IOException {
//        tests.text_updates_parameter ();
//    }
//
//    public void stress_zero_updates () throws IOException {
//        tests.zero_updates ();
//    }
//
//    public void stress_one_thousand_updates () throws IOException {
//        tests.one_thousand_updates ();
//    }
//
//    public void stress_one_update () throws IOException {
//        tests.one_update ();
//    }
//
//    public void stress_ten_updates () throws IOException {
//        tests.ten_updates ();
//    }
//
//    public void stress_one_hundred_updates () throws IOException {
//        tests.one_hundred_updates ();
//    }
//
//    public void stress_five_hundred_updates () throws IOException {
//        tests.five_hundred_updates ();
//    }
}
