package dev.langchain4j.store.embedding.redis;

import redis.clients.jedis.JedisPooled;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static dev.langchain4j.internal.Utils.getOrDefault;

/**
 * 使用 {@link URI}{@code redis://:password@host:port/db} 创建 {@link JedisPooled}（单参数 AUTH），
 * 避免 LangChain4j Builder 使用 {@code AUTH default &lt;password&gt;} 在仅支持 {@code requirepass} 的 Redis 上报错：
 * {@code ERR wrong number of arguments for 'auth' command}。
 */
public final class RedisEmbeddingStoreBootstrap {

    private RedisEmbeddingStoreBootstrap() {}

    public static RedisEmbeddingStore createWithUri(
            String host,
            int port,
            int database,
            String password,
            String indexName,
            String prefix,
            int dimension,
            Collection<String> metadataKeys) {
        JedisPooled client = new JedisPooled(buildRedisUri(host, port, database, password));

        RedisSchema schema = RedisSchema.builder()
                .indexName(getOrDefault(indexName, "embedding-index"))
                .prefix(getOrDefault(prefix, "embedding:"))
                .dimension(dimension)
                .metadataKeys(metadataKeys)
                .build();

        try {
            RedisEmbeddingStore store = allocateStore();
            Field clientField = RedisEmbeddingStore.class.getDeclaredField("client");
            clientField.setAccessible(true);
            clientField.set(store, client);
            Field schemaField = RedisEmbeddingStore.class.getDeclaredField("schema");
            schemaField.setAccessible(true);
            schemaField.set(store, schema);

            Method isIndexExist = RedisEmbeddingStore.class.getDeclaredMethod("isIndexExist", String.class);
            isIndexExist.setAccessible(true);
            String idx = schema.indexName();
            if (!(boolean) isIndexExist.invoke(store, idx)) {
                Method createIndex = RedisEmbeddingStore.class.getDeclaredMethod("createIndex", String.class);
                createIndex.setAccessible(true);
                createIndex.invoke(store, idx);
            }
            return store;
        } catch (ReflectiveOperationException e) {
            try {
                client.close();
            } catch (Exception ignored) {
                // ignore
            }
            throw new IllegalStateException("Failed to initialize RedisEmbeddingStore", e);
        }
    }

    @SuppressWarnings("restriction")
    private static RedisEmbeddingStore allocateStore() throws ReflectiveOperationException {
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) f.get(null);
        return (RedisEmbeddingStore) unsafe.allocateInstance(RedisEmbeddingStore.class);
    }

    /**
     * Jedis 5：{@code redis://[:password]@host:port/database}，无用户名时仅密码在 {@code :} 之后。
     */
    static URI buildRedisUri(String host, int port, int database, String password) {
        String auth;
        if (password == null || password.isEmpty()) {
            auth = "";
        } else {
            String enc = URLEncoder.encode(password, StandardCharsets.UTF_8).replace("+", "%20");
            auth = ":" + enc + "@";
        }
        String uriStr = String.format("redis://%s%s:%d/%d", auth, host, port, database);
        return URI.create(uriStr);
    }
}
