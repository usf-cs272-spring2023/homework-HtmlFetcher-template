HtmlFetcher
=================================================

![Points](../../blob/badges/points.svg)

For this homework, you will use the `HttpsFetcher` code from lecture to create a `HtmlFetcher` class for downloading HTML content from web servers.

## Hints ##

Below are some hints that may help with this homework assignment:

  - It will help to have a HTTP reference. The [MDN Web Docs](https://developer.mozilla.org/en-US/) have nice [HTTP reference](https://developer.mozilla.org/en-US/docs/Web/HTTP) references, including references for [HTTP headers](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers) and [HTTP status codes](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status).

  - **Do not fetch the entire page unless necessary!** For the most efficient solution, do not directly use `HttpsFetcher.fetchURL(URL url)` in your implementation. Instead, setup the sockets and get the headers in the same way. Then, based on those headers, decide how to proceed.

  - Some of these methods can be done using regular expressions, but it is not required.

These hints are *optional*. There may be multiple approaches to solving this homework.

## Instructions ##

Use the "Tasks" view in Eclipse to find the `TODO` comments for what need to be implemented and the "Javadoc" view to see additional details.

The tests are provided in the `src/test/` directory; do not modify any of the files in that directory. Check the run details on GitHub Actions for how many points each test group is worth. 

See the [Homework Guides](https://usf-cs272-spring2023.github.io/guides/homework/) for additional details on homework requirements and submission.