package io.vertx.cache.it.operation;

import com.redis.testcontainers.RedisContainer;
import io.vertx.cache.common.Cache;
import io.vertx.cache.distributed.DistributedCache;
import io.vertx.cache.distributed.DistributedCacheOptions;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.redis.client.RedisOptions;
import org.junit.After;
import org.junit.Before;

public class DistributedOperationCacheTest extends OperationCacheTest {

    private final RedisContainer container = new RedisContainer(RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG));

    @Before
    @Override
    public void setUp() {
        this.vertx = Vertx.vertx();
        this.container.start();
        this.cache = cache(vertx);
    }

    @Override
    protected Cache cache(Vertx vertx) {
        RedisOptions redisOptions = new RedisOptions().setConnectionString(container.getRedisURI());

        DistributedCacheOptions options = new DistributedCacheOptions()
                .setKeyPrefix("test:" + System.currentTimeMillis() + ":")
                .setRedisOptions(redisOptions);

        return DistributedCache.create(vertx, options);
    }

    @After
    @Override
    public void tearDown(TestContext should) {
        super.tearDown(should);
    }
}
