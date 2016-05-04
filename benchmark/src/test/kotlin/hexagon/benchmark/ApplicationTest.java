package hexagon.benchmark;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * <p>TODO
 * Write article about stress test with TestNG (scenarios, combine different tests in scenarios,
 * adding random pauses...)
 */
@Test (enabled = false, threadPoolSize = 16, invocationCount = 75)
public final class ApplicationTest {
    private static final int WARM_UP = 10;
    private static final String ENDPOINT = "http://localhost:5050";
    private static final ObjectMapper GSON = new ObjectMapper ();

    private static Application application;

    @BeforeClass public void setup () throws IOException {
        application = new Application ();
    }

//    @BeforeClass public void setup () throws IOException {
//        tests.setup ();
//
//        for (int ii = 0; ii < WARM_UP; ii++) {
//            json ();
//            plaintext ();
//            no_query_parameter ();
//            empty_query_parameter ();
//            text_query_parameter ();
//            zero_queries ();
//            one_thousand_queries ();
//            one_query ();
//            ten_queries ();
//            one_hundred_queries ();
//            five_hundred_queries ();
//            fortunes ();
//            no_updates_parameter ();
//            empty_updates_parameter ();
//            text_updates_parameter ();
//            zero_updates ();
//            one_thousand_updates ();
//            one_update ();
//            ten_updates ();
//            one_hundred_updates ();
//            five_hundred_updates ();
//        }
//    }
//
//    @AfterClass public void close () {
//        application.stop ();
//    }
//
//    public void json () throws IOException {
//        HttpResponse response = get (ENDPOINT + "/json");
//        String content = getContent (response);
//
//        checkResponse (response, content, "application/json");
//        assert "Hello, World!".equals (GSON.fromJson (content, Map.class).get ("message"));
//    }
//
//    public void plaintext () throws IOException {
//        HttpResponse response = get (ENDPOINT + "/plaintext");
//        String content = getContent (response);
//
//        checkResponse (response, content, "text/plain");
//        assert "Hello, World!".equals (content);
//    }
//
//    public void no_query_parameter () throws IOException {
//        HttpResponse response = get (ENDPOINT + "/db");
//        String content = getContent (response);
//
//        checkResponse (response, content, "application/json");
//        Map<?, ?> resultsMap = GSON.fromJson (content, Map.class);
//        assert resultsMap.containsKey ("id") && resultsMap.containsKey ("randomNumber");
//    }
//
//    public void empty_query_parameter () throws IOException {
//        checkDbRequest ("/query?queries", 1);
//    }
//
//    public void text_query_parameter () throws IOException {
//        checkDbRequest ("/query?queries=text", 1);
//    }
//
//    public void zero_queries () throws IOException {
//        checkDbRequest ("/query?queries=0", 1);
//    }
//
//    public void one_thousand_queries () throws IOException {
//        checkDbRequest ("/query?queries=1000", 500);
//    }
//
//    public void one_query () throws IOException {
//        checkDbRequest ("/query?queries=1", 1);
//    }
//
//    public void ten_queries () throws IOException {
//        checkDbRequest ("/query?queries=10", 10);
//    }
//
//    public void one_hundred_queries () throws IOException {
//        checkDbRequest ("/query?queries=100", 100);
//    }
//
//    public void five_hundred_queries () throws IOException {
//        checkDbRequest ("/query?queries=500", 500);
//    }
//
//    public void fortunes () throws IOException {
//        HttpResponse response = get (ENDPOINT + "/fortune");
//        String content = getContent (response);
//        String contentType = response.getEntity ().getContentType ().getValue ();
//
//        assert response.getFirstHeader ("Server") != null;
//        assert response.getFirstHeader ("Date") != null;
//        assert content.contains ("&lt;script&gt;alert(&quot;This should not be displayed");
//        assert content.contains ("フレームワークのベンチマーク");
//        assert "text/html; charset=utf-8".equals (contentType.toLowerCase ());
//    }
//
//    public void no_updates_parameter () throws IOException {
//        HttpResponse response = get (ENDPOINT + "/update");
//        String content = getContent (response);
//
//        checkResponse (response, content, "application/json");
//        Map<?, ?> resultsMap = GSON.fromJson (content, Map.class);
//        assert resultsMap.containsKey ("id") && resultsMap.containsKey ("randomNumber");
//    }
//
//    public void empty_updates_parameter () throws IOException {
//        checkDbRequest ("/update?queries", 1);
//    }
//
//    public void text_updates_parameter () throws IOException {
//        checkDbRequest ("/update?queries=text", 1);
//    }
//
//    public void zero_updates () throws IOException {
//        checkDbRequest ("/update?queries=0", 1);
//    }
//
//    public void one_thousand_updates () throws IOException {
//        checkDbRequest ("/update?queries=1000", 500);
//    }
//
//    public void one_update () throws IOException {
//        checkDbRequest ("/update?queries=1", 1);
//    }
//
//    public void ten_updates () throws IOException {
//        checkDbRequest ("/update?queries=10", 10);
//    }
//
//    public void one_hundred_updates () throws IOException {
//        checkDbRequest ("/update?queries=100", 100);
//    }
//
//    public void five_hundred_updates () throws IOException {
//        checkDbRequest ("/update?queries=500", 500);
//    }
//
//    private void checkDbRequest (String path, int itemsCount) throws IOException {
//        HttpResponse response = get (ENDPOINT + path);
//        String content = getContent (response);
//
//        checkResponse (response, content, "application/json");
//        checkResultItems (content, itemsCount);
//    }
//
//    private HttpResponse get (String uri) throws IOException {
//        return Get (uri).execute ().returnResponse ();
//    }
//
//    private String getContent (HttpResponse response) throws IOException {
//        InputStream in = response.getEntity ().getContent ();
//        return new Scanner (in).useDelimiter ("\\A").next ();
//    }
//
//    private void checkResponse (HttpResponse res, String content, String contentType) {
//        assert res.getFirstHeader ("Server") != null;
//        assert res.getFirstHeader ("Date") != null;
//        assert content.length () == res.getEntity ().getContentLength ();
//        assert res.getEntity ().getContentType ().getValue ().contains (contentType);
//    }
//
//    private void checkResultItems (String result, int size) {
//        List<?> resultsList = GSON.fromJson (result, List.class);
//        assert size == resultsList.size ();
//
//        for (int ii = 0; ii < size; ii++) {
//            Map<?, ?> r = (Map)resultsList.get (ii);
//            assert r.containsKey ("id") && r.containsKey ("randomNumber");
//        }
//    }
}
