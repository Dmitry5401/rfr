package io.student.rangiffler.config;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.function.IntPredicate;

public final class HttpService implements Service {
  private final String name;
  private final String hint;
  private final HttpRequest request;
  private final IntPredicate accepted;

  public HttpService(String name, String hint, URI uri) {
    this(name, hint, HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(5)).build(), status -> status / 100 == 2);
  }

  public HttpService(String name, String hint, HttpRequest request, IntPredicate accepted) {
    this.name = name;
    this.hint = hint;
    this.request = request;
    this.accepted = accepted;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String hint() {
    return hint;
  }

  @Override
  public boolean responds() {
    try {
      return Arrays.stream(InetAddress.getAllByName(request.uri().getHost())).anyMatch(this::responds);
    } catch (UnknownHostException failure) {
      return false;
    }
  }

  private boolean responds(InetAddress address) {
    try (HttpClient client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(5))
        .build()) {
      return accepted.test(
          client.send(
                  HttpRequest.newBuilder(request, (header, value) -> true).uri(at(address)).build(),
                  HttpResponse.BodyHandlers.discarding())
              .statusCode());
    } catch (IOException failure) {
      return false;
    } catch (InterruptedException failure) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Waiting for " + name + " at " + request.uri() + " was interrupted", failure);
    }
  }

  private URI at(InetAddress address) {
    URI uri = request.uri();
    try {
      return new URI(uri.getScheme(), null, address.getHostAddress(), uri.getPort(), uri.getPath(), uri.getQuery(), null);
    } catch (URISyntaxException failure) {
      throw new IllegalStateException("Address " + address + " of " + name + " does not form a valid URI from " + uri, failure);
    }
  }
}
