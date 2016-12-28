package com.davidbyttow.sfe.async;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.davidbyttow.sfe.concurrent.BackgroundThreadPool;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.IOException;

public class AsyncHttpClient {

  private final HttpClient httpClient;
  private final ListeningExecutorService executor;

  @Inject AsyncHttpClient(HttpClient httpClient, @BackgroundThreadPool ListeningExecutorService executor) {
    this.httpClient = httpClient;
    this.executor = executor;
  }

  public ListenableFuture<HttpResponse> executeAsync(HttpUriRequest request) throws IOException {
    return executor.submit(() -> httpClient.execute(request));
  }
}
