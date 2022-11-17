package edu.usfca.cs272;

import java.io.IOException;
import java.net.HttpURLConnection;
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
 * @version Fall 2022
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

	/** How long to wait for individual tests to complete. */
	public static final Duration TIMEOUT = Duration.ofSeconds(45);

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
				"https://usf-cs272-fall2022.github.io/project-web/input/simple/no_extension",
				"https://usf-cs272-fall2022.github.io/project-web/input/simple/double_extension.html.txt",
				"https://usf-cs272-fall2022.github.io/project-web/input/guten/1661-h/images/cover.jpg"
		})
		@Order(1)
		public void testNotHtml(String link) throws IOException {
			URL url = new URL(link);
			HttpURLConnection.setFollowRedirects(false);

			Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
				Map<String, List<String>> headers = url.openConnection().getHeaderFields();
				Assertions.assertFalse(HtmlFetcher.isHtml(headers));
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
				"https://usf-cs272-fall2022.github.io/project-web/input/simple/",
				"https://usf-cs272-fall2022.github.io/project-web/input/simple/empty.html",
				"https://usf-cs272-fall2022.github.io/project-web/input/birds/falcon.html",
				"https://usf-cs272-fall2022.github.io/project-web/input/birds/falcon.html#file=hello.jpg",
				"https://www.cs.usfca.edu/~cs272/redirect/nowhere"
		})
		@Order(2)
		public void testIsHtml(String link) throws IOException {
			URL url = new URL(link);
			HttpURLConnection.setFollowRedirects(false);

			Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
				Map<String, List<String>> headers = url.openConnection().getHeaderFields();
				Assertions.assertTrue(HtmlFetcher.isHtml(headers));
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
				"https://usf-cs272-fall2022.github.io/project-web/input/simple/no_extension",
				"https://usf-cs272-fall2022.github.io/project-web/input/simple/double_extension.html.txt",
				"https://usf-cs272-fall2022.github.io/project-web/input/birds/yellowthroat.html"
		})
		@Order(1)
		public void test200(String link) throws IOException {
			testStatusCode(link, 200);
		}

		/**
		 * Tests that the status code is 404.
		 *
		 * @throws IOException from {@link #testStatusCode(String, int)}
		 * @see HtmlFetcher#getStatusCode(Map)
		 */
		@Test
		@Order(2)
		public void test404() throws IOException {
			String link = "https://www.cs.usfca.edu/~cs272/redirect/nowhere";
			testStatusCode(link, 404);
		}

		/**
		 * Tests that the status code is 410.
		 *
		 * @throws IOException from {@link #testStatusCode(String, int)}
		 * @see HtmlFetcher#getStatusCode(Map)
		 */
		@Test
		@Order(3)
		public void test410() throws IOException {
			String link = "https://www.cs.usfca.edu/~cs272/redirect/gone";
			testStatusCode(link, 410);
		}
	}

	/**
	 * Tests the redirect status code methods.
	 *
	 * @see HtmlFetcher#isRedirect(Map)
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
		 * @see HtmlFetcher#isRedirect(Map)
		 */
		@ParameterizedTest
		@ValueSource(strings = {
				"https://www.cs.usfca.edu/~cs272/redirect/loop1",
				"https://www.cs.usfca.edu/~cs272/redirect/loop2",
				"https://www.cs.usfca.edu/~cs272/redirect/one",
				"https://www.cs.usfca.edu/~cs272/redirect/two"
		})
		@Order(4)
		public void testRedirect(String link) throws IOException {
			URL url = new URL(link);
			HttpURLConnection.setFollowRedirects(false);

			Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
				Map<String, List<String>> headers = url.openConnection().getHeaderFields();
				Assertions.assertTrue(HtmlFetcher.isRedirect(headers));
			});
		}

		/**
		 * Tests that the status code is not a redirect.
		 *
		 * @param link the link to fetch
		 * @throws IOException from {@link #testStatusCode(String, int)}
		 *
		 * @see HtmlFetcher#isRedirect(Map)
		 */
		@ParameterizedTest
		@ValueSource(strings = {
				"https://usf-cs272-fall2022.github.io/project-web/input/simple/no_extension",
				"https://www.cs.usfca.edu/~cs272/redirect/nowhere",
				"https://www.cs.usfca.edu/~cs272/redirect/gone"
		})
		@Order(5)
		public void testNotRedirect(String link) throws IOException {
			URL url = new URL(link);
			HttpURLConnection.setFollowRedirects(false);

			Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
				Map<String, List<String>> headers = url.openConnection().getHeaderFields();
				Assertions.assertFalse(HtmlFetcher.isRedirect(headers));
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
		 */
		@ParameterizedTest
		@ValueSource(strings = {
				"https://usf-cs272-fall2022.github.io/project-web/input/simple/no_extension",
				"https://usf-cs272-fall2022.github.io/project-web/input/simple/double_extension.html.txt",
				"https://www.cs.usfca.edu/~cs272/redirect/nowhere"
		})
		@Order(1)
		public void testNotValidHtml(String link) {
			Assertions.assertTimeoutPreemptively(TIMEOUT,
					() -> { String html = HtmlFetcher.fetch(link); Assertions.assertNull(html); });
		}

		/**
		 * Tests the HTML returned for a valid page.
		 *
		 * @throws IOException if unable to read html file
		 */
		@Test
		@Order(2)
		public void testHtmlYellow() throws IOException {
			String link = "https://usf-cs272-fall2022.github.io/project-web/input/birds/yellowthroat.html";

			Path yellow = Path.of("src", "test", "resources", "yellowthroat.html");
			String expected = Files.readString(yellow, StandardCharsets.UTF_8);

			Assertions.assertTimeoutPreemptively(TIMEOUT,
					() -> {
						String html = HtmlFetcher.fetch(link);
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
			String link = "https://usf-cs272-fall2022.github.io/project-web/docs/api/allclasses-index.html";

			Path yellow = Path.of("src", "test", "resources", "allclasses-index.html");
			String expected = Files.readString(yellow, StandardCharsets.UTF_8);

			Assertions.assertTimeoutPreemptively(TIMEOUT,
					() -> {
						String html = HtmlFetcher.fetch(link);
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

			Assertions.assertTimeoutPreemptively(TIMEOUT,
					() -> { String html = HtmlFetcher.fetch(one, redirects); Assertions.assertNull(html); });
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
		URL url = new URL(link);
		HttpURLConnection.setFollowRedirects(false);

		Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
			Map<String, List<String>> headers = url.openConnection().getHeaderFields();
			int actual = HtmlFetcher.getStatusCode(headers);
			Assertions.assertEquals(code, actual);
		});
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
}
