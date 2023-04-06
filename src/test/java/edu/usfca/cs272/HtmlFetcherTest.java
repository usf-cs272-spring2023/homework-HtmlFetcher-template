package edu.usfca.cs272;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

/**
 * Tests the {@link HtmlFetcher} class.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
@TestMethodOrder(MethodName.class)
public class HtmlFetcherTest {
	// ███████╗████████╗ ██████╗ ██████╗
	// ██╔════╝╚══██╔══╝██╔═══██╗██╔══██╗
	// ███████╗   ██║   ██║   ██║██████╔╝
	// ╚════██║   ██║   ██║   ██║██╔═══╝
	// ███████║   ██║   ╚██████╔╝██║
	// ╚══════╝   ╚═╝    ╚═════╝ ╚═╝
	/*
	 * ...and read this! Please do not spam our server by rapidly re-running all of
	 * these tests over and over again. You risk being blocked by our web server if
	 * you make making too many requests in too short of a time period!
	 *
	 * Focus on one test or one group of tests at a time instead. If you do that,
	 * you will not have anything to worry about!
	 */

	/**
	 * Tests the {@link HtmlFetcher#isHtml(Map)} method.
	 *
	 * @see HtmlFetcher#isHtml(Map)
	 */
	@Nested
	@TestMethodOrder(OrderAnnotation.class)
	public class A_HtmlTypeTests {
		/**
		 * Tests the {@link HtmlFetcher#isHtml(Map)} method for URLs that do not point
		 * to valid HTML webpages.
		 *
		 * @param link the link to test
		 * @throws IOException from {@link URL#openConnection()}
		 *
		 * @see HtmlFetcher#isHtml(Map)
		 */
		@ParameterizedTest
		@ValueSource(strings = {
			"input/simple/no_extension",
			"input/simple/double_extension.html.txt",
			"input/guten/1661-h/images/cover.jpg"
		})
		@Order(1)
		public void testNotHtml(String link) throws IOException {
			URL base = new URL(GITHUB);
			URL url = new URL(base, link);

			Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
				Map<String, List<String>> headers = getHeaders(url);
				Assertions.assertFalse(HtmlFetcher.isHtml(headers), () -> debug(url, headers));
			});
		}

		/**
		 * Tests the {@link HtmlFetcher#isHtml(Map)} method for URLs that do point to
		 * valid HTML webpages.
		 *
		 * @param link the link to test
		 * @throws IOException from {@link URL#openConnection()}
		 *
		 * @see HtmlFetcher#isHtml(Map)
		 */
		@ParameterizedTest
		@ValueSource(strings = {
				"input",
				"input/simple/",
				"input/simple/empty.html",
				"input/birds/falcon.html",
				"input/birds/falcon.html#file=hello.jpg",
				"https://www.cs.usfca.edu/~cs272/redirect/nowhere",
				"https://www.cs.usfca.edu/~cs272/recurse/"
		})
		@Order(2)
		public void testIsHtml(String link) throws IOException {
			URL base = new URL(GITHUB);
			URL url = new URL(base, link);

			Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
				Map<String, List<String>> headers = getHeaders(url);
				Assertions.assertTrue(HtmlFetcher.isHtml(headers), () -> debug(url, headers));
			});
		}
	}

	/**
	 * Tests the status code methods.
	 *
	 * @see HtmlFetcher#getStatusCode(Map)
	 */
	@Nested
	@TestMethodOrder(OrderAnnotation.class)
	public class B_StatusCodeTests {
		/**
		 * Tests that the status code is 200.
		 *
		 * @param link the link to fetch
		 * @throws IOException from {@link #testStatusCode(String, int)}
		 * @see HtmlFetcher#getStatusCode(Map)
		 */
		@ParameterizedTest
		@ValueSource(strings = {
				"input/simple/no_extension",
				"input/simple/double_extension.html.txt",
				"input/birds/yellowthroat.html"
		})
		@Order(1)
		public void test200(String link) throws IOException {
			testStatusCode(link, 200);
		}

		/**
		 * Tests the status code for other codes.
		 *
		 * @param code the expected code
		 * @param link the link to fetch
		 * @throws IOException from {@link #testStatusCode(String, int)}
		 * @see HtmlFetcher#getStatusCode(Map)
		 */
		@ParameterizedTest
		@CsvSource({
				"301, input",
				"302, http://www.cs.usfca.edu/~cs272/",
				"301, https://www.cs.usfca.edu/~cs272/redirect/loop1",
				"404, https://www.cs.usfca.edu/~cs272/redirect/nowhere",
				"410, https://www.cs.usfca.edu/~cs272/redirect/gone"
		})
		@Order(2)
		public void testOther(int code, String link) throws IOException {
			testStatusCode(link, code);
		}
	}

	/**
	 * Tests the redirect status code methods.
	 *
	 * @see HtmlFetcher#getRedirect(Map)
	 */
	@Nested
	@TestMethodOrder(OrderAnnotation.class)
	public class C_RedirectCodeTests {
		/**
		 * Tests that the status code is a redirect.
		 *
		 * @param link the link to fetch
		 * @throws IOException from {@link #testStatusCode(String, int)}
		 *
		 * @see HtmlFetcher#getRedirect(Map)
		 */
		@ParameterizedTest
		@ValueSource(strings = {
				"https://www.cs.usfca.edu/~cs272/redirect/loop1",
				"https://www.cs.usfca.edu/~cs272/redirect/loop2",
				"https://www.cs.usfca.edu/~cs272/redirect/one",
				"https://www.cs.usfca.edu/~cs272/redirect/two"
		})
		@Order(1)
		public void testRedirect(String link) throws IOException {
			URL url = new URL(link);

			Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
				Map<String, List<String>> headers = getHeaders(url);
				Assertions.assertNotNull(HtmlFetcher.getRedirect(headers), () -> debug(url, headers));
			});
		}

		/**
		 * Tests that the status code is not a redirect.
		 *
		 * @param link the link to fetch
		 * @throws IOException from {@link #testStatusCode(String, int)}
		 *
		 * @see HtmlFetcher#getRedirect(Map)
		 */
		@ParameterizedTest
		@ValueSource(strings = {
				"input/simple/no_extension",
				"https://www.cs.usfca.edu/~cs272/redirect/nowhere",
				"https://www.cs.usfca.edu/~cs272/redirect/gone"
		})
		@Order(2)
		public void testNotRedirect(String link) throws IOException {
			URL base = new URL(GITHUB);
			URL url = new URL(base, link);

			Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
				Map<String, List<String>> headers = getHeaders(url);
				Assertions.assertNull(HtmlFetcher.getRedirect(headers), () -> debug(url, headers));
			});
		}
	}

	/**
	 * Tests fetching HTML for troublesome links.
	 *
	 * @see HtmlFetcher#fetch(String)
	 * @see HtmlFetcher#fetch(URL)
	 */
	@Nested
	@TestMethodOrder(OrderAnnotation.class)
	public class D_FetchHtmlTests {
		/**
		 * Test that attempting to fetch pages that do not have valid HTML results in a
		 * null value.
		 *
		 * @param link the link to fetch
		 * @throws MalformedURLException from {@link URL#URL(String)}
		 */
		@ParameterizedTest
		@ValueSource(strings = {
				"input/simple/no_extension",
				"input/simple/double_extension.html.txt",
				"https://www.cs.usfca.edu/~cs272/redirect/nowhere"
		})
		@Order(1)
		public void testNotValidHtml(String link) throws MalformedURLException {
			URL base = new URL(GITHUB);
			URL url = new URL(base, link);

			Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
				String html = HtmlFetcher.fetch(url);
				Assertions.assertNull(html, url.toString());
			});
		}

		/**
		 * Tests the HTML returned for a valid page.
		 *
		 * @throws IOException if unable to read html file
		 */
		@Test
		@Order(2)
		public void testHtmlYellow() throws IOException {
			String link = "input/birds/yellowthroat.html";
			URL base = new URL(GITHUB);
			URL url = new URL(base, link);

			Path file = Path.of("src", "test", "resources", "yellowthroat.html");
			String expected = Files.readString(file, StandardCharsets.UTF_8);

			Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
				String html = HtmlFetcher.fetch(url);
				compareText(expected, html);
			});
		}

		/**
		 * Tests the HTML returned for a valid page.
		 *
		 * @throws IOException if unable to read html file
		 */
		@Test
		@Order(3)
		public void testHtmlJava() throws IOException {
			String link = "docs/api/allclasses-index.html";
			URL base = new URL(GITHUB);
			URL url = new URL(base, link);

			Path file = Path.of("src", "test", "resources", "allclasses-index.html");
			String expected = Files.readString(file, StandardCharsets.UTF_8);

			Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
				String html = HtmlFetcher.fetch(url);
				compareText(expected, html);
			});
		}
	}

	/**
	 * Tests fetching HTML for redirects.
	 *
	 * @see HtmlFetcher#fetch(String, int)
	 * @see HtmlFetcher#fetch(URL, int)
	 */
	@Nested
	@TestMethodOrder(OrderAnnotation.class)
	public class E_FetchRedirectTests {
		/**
		 * Tests that null is returned when a link does not resolve within a specific
		 * number of redirects.
		 *
		 * @param redirects the number of redirects to try
		 */
		@ParameterizedTest
		@ValueSource(ints = {
				-1, 0, 1, 2
		})
		@Order(1)
		public void testUnsuccessfulRedirect(int redirects) {
			String one = "https://www.cs.usfca.edu/~cs272/redirect/one";

			Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
				String html = HtmlFetcher.fetch(one, redirects);
				Assertions.assertNull(html);
			});
		}

		/**
		 * Tests that proper HTML is returned when a link DOES resolve within a specific
		 * number of redirects.
		 *
		 * @param redirects the number of redirects to try
		 * @throws IOException if unable to read html file
		 */
		@ParameterizedTest
		@ValueSource(ints = {
				3, 4
		})
		@Order(2)
		public void testSuccessfulRedirect(int redirects) throws IOException {
			String one = "https://www.cs.usfca.edu/~cs272/redirect/one";

			Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
				String html = HtmlFetcher.fetch(one, redirects);
				Assertions.assertNotNull(html);

				Path hello = Path.of("src", "test", "resources", "hello.html");
				String expected = Files.readString(hello, StandardCharsets.UTF_8);
				compareText(expected, html);
			});
		}
	}

	/**
	 * Tests that certain classes or packages do not appear in the implementation
	 * code. Attempts to fool this test will be considered cheating.
	 */
	@Tag("approach")
	@Nested
	public class F_ApproachTests {
		/**
		 * Tests that certain classes or packages do not appear in the implementation
		 * code. Attempts to fool this test will be considered cheating.
		 *
		 * @throws IOException if unable to read source code
		 */
		@Test
		public void testClasses() throws IOException {
			String name = HtmlFetcher.class.getSimpleName() + ".java";
			Path base = Path.of("src", "main", "java");
			Path pack = Path.of("edu", "usfca", "cs272");
			Path path = base.resolve(pack).resolve(name);

			String source = Files.readString(path, StandardCharsets.UTF_8);

			Assertions.assertAll(
					() -> Assertions.assertFalse(source.contains("import java.net.*;"),
							"Modify your code to use more specific import statements."),
					() -> Assertions.assertFalse(source.contains("import java.net.URLConnection;"),
							"You may not use the URLConnection class."),
					() -> Assertions.assertFalse(source.contains("import java.net.HttpURLConnection;"),
							"You may not use the HttpURLConnection class."));
		}

		/**
		 * Causes this group of tests to fail if the other non-approach tests are not
		 * yet passing.
		 */
		@Test
		public void testOthersPassing() {
			var request = LauncherDiscoveryRequestBuilder.request()
					.selectors(DiscoverySelectors.selectClass(HtmlFetcherTest.class))
					.filters(TagFilter.excludeTags("approach"))
					.build();

			var launcher = LauncherFactory.create();
			var listener = new SummaryGeneratingListener();

			Logger logger = Logger.getLogger("org.junit.platform.launcher");
			logger.setLevel(Level.SEVERE);

			launcher.registerTestExecutionListeners(listener);
			launcher.execute(request);

			Assertions.assertEquals(0, listener.getSummary().getTotalFailureCount(),
					"Must pass other tests to earn credit for approach group!");
		}
	}

	/**
	 * Tests if the status code returned is as expected.
	 *
	 * @param link the URL to fetch
	 * @param code the expected status code
	 * @throws IOException from {@link URL#openConnection()}
	 *
	 * @see HtmlFetcher#getStatusCode(Map)
	 */
	public static void testStatusCode(String link, int code) throws IOException {
		URL base = new URL(GITHUB);
		URL url = new URL(base, link);

		Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
			Map<String, List<String>> headers = getHeaders(url);
			int actual = HtmlFetcher.getStatusCode(headers);
			Assertions.assertEquals(code, actual, () -> debug(url, headers));
		});
	}

	/**
	 * Use built-in Java URL connection to get headers for debugging.
	 *
	 * @param url the url to fetch
	 * @return the headers
	 * @throws IOException if unable to connect
	 */
	public static Map<String, List<String>> getHeaders(URL url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setInstanceFollowRedirects(false);
		connection.setRequestProperty("Connection", "close");
		connection.connect();

		try {
			return connection.getHeaderFields();
		}
		finally {
			connection.disconnect();
		}
	}

	/**
	 * Cleans whitespace and then compares the actual text to the expected.
	 *
	 * @param expected the expected text
	 * @param actual the actual text
	 */
	public static void compareText(String expected, String actual) {
		Assertions.assertEquals(cleanWhitespace(expected), cleanWhitespace(actual));
	}

	/**
	 * Cleans up whitespace for comparison.
	 *
	 * @param text the text to clean
	 * @return the cleaned text
	 */
	public static String cleanWhitespace(String text) {
		return text.strip().replaceAll("\r\n?", "\n");
	}

	/**
	 * Produces output for debugging.
	 *
	 * @param url the url
	 * @param headers the headers
	 * @return the output
	 */
	public static final String debug(URL url, Map<String, List<String>> headers) {
		StringBuilder output = new StringBuilder();
		output.append("\nURL:\n");
		output.append(url.toString());
		output.append("\n\nHeaders:\n");

		for (var entry : headers.entrySet()) {
			output.append(entry.getKey());
			output.append(" -> ");
			output.append(entry.getValue());
			output.append("\n");
		}

		output.append("\n");
		return output.toString();
	}

	/** Base URL for the GitHub test website. */
	public static final String GITHUB = "https://usf-cs272-spring2023.github.io/project-web/";

	/** How long to wait for individual tests to complete. */
	public static final Duration TIMEOUT = Duration.ofSeconds(45);
}
