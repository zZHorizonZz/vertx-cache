package io.vertx.cache.it;

import io.vertx.cache.common.Cache;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public abstract class AbstractCacheTest {

    protected Vertx vertx;
    protected Cache cache;

    protected abstract Cache cache(Vertx vertx);

    @Before
    public void setUp() {
        this.vertx = Vertx.vertx();
        this.cache = cache(vertx);
    }

    @After
    public void tearDown(TestContext should) {
        cache.clear().compose(v -> cache.close()).onComplete(should.asyncAssertSuccess());
    }
}
