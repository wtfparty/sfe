package io.bold.sfe.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.simplethingsllc.store.client.EntityStore;
import com.simplethingsllc.store.client.EntityStoreAsync;
import com.simplethingsllc.store.client.EntityStoreClient;
import com.simplethingsllc.store.client.EntityStoreFactory;
import com.simplethingsllc.store.client.config.ClientConfig;
import com.simplethingsllc.store.client.config.CompositeIndexDef;
import com.simplethingsllc.store.server.EntitiesStorage;
import io.bold.sfe.concurrent.BackgroundThreadPool;
import io.bold.sfe.environment.ClassLoaders;
import io.bold.sfe.inject.LazySingleton;
import io.bold.sfe.json.Json;
import org.apache.commons.io.IOUtils;
import org.skife.jdbi.v2.DBI;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class EntityStoreModule extends AbstractModule {

  private final String indexFilePath;
  private final Set<Class<?>> entityClasses;
  private final boolean skipMigrations;

  public EntityStoreModule(String indexFilePath, Set<Class<?>> entityClasses, boolean skipMigrations) {
    this.indexFilePath = indexFilePath;
    this.entityClasses = entityClasses;
    this.skipMigrations = skipMigrations;
  }

  @Override protected void configure() {
    install(new StorageModule.DbModule<>(EntitiesStorage.class));
  }

  @Provides
  @LazySingleton
  EntityStoreClient client(@ForWrites DataSource dataSource,
                           @ForWrites DBI dbi,
                           ObjectMapper objectMapper,
                           @BackgroundThreadPool ListeningExecutorService executorService) throws IOException {
    ClientConfig clientConfig = new ClientConfig();
    clientConfig.dataSource = dataSource;
    clientConfig.dbi = dbi;
    clientConfig.executorService = executorService;
    clientConfig.objectMapper = objectMapper;
    clientConfig.entityTypes = entityClasses;
    clientConfig.indexes = EntityStoreModule.loadIndexesFromJson(indexFilePath);
    clientConfig.skipMigrations = skipMigrations;
    return EntityStoreFactory.createClient(clientConfig);
  }

  @Provides EntityStore entityStore(EntityStoreClient client) {
    return client.getStore();
  }

  @Provides EntityStoreAsync entityStoreAsync(EntityStoreClient client) {
    return client.getAsyncStore();
  }

  public static class IndexesDef {
    List<CompositeIndexDef> indexes = new ArrayList<>();
  }

  public static List<CompositeIndexDef> loadIndexesFromJson(String indexFilePath) throws IOException {
    InputStream stream = ClassLoaders.open(indexFilePath);
    if (stream == null) {
      return new ArrayList<>();
    }
    String json = IOUtils.toString(stream, Charsets.UTF_8);
    return Json.readValue(json, IndexesDef.class).indexes;
  }
}
