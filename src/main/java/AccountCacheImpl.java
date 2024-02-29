import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public class AccountCacheImpl implements AccountCache {
    private static Logger LOGGER = LoggerFactory.getLogger(AccountCacheImpl.class);
    private LRUCache<Long, Account> lruCache;

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    public AccountCacheImpl(int capacity) {
        this.lruCache = new LRUCache<>(capacity);
    }

    public LRUCache<Long, Account> getLruCache() {
        return lruCache;
    }

    @Override
    public Account getAccountById(long id) {
        lock.readLock().lock();
        try {
            return lruCache.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void subscribeForAccountUpdates(Consumer<Account> listener) {
        listener.accept(null);
    }

    @Override
    public List<Account> getTop3AccountsByBalance() {
        LOGGER.info("{} - Enter getTop3AccountsByBalance!, read lock", Thread.currentThread().getName());
        lock.readLock().lock();
        try {
            LOGGER.info("{} - Write lock is released, Start getTop3AccountsByBalance!", Thread.currentThread().getName());
            Thread.sleep(5000);
            return lruCache.values()
                    .stream()
                    .sorted()
                    .limit(3)
                    .collect(Collectors.toList());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            LOGGER.info("{} - getTop3AccountsByBalance successfully!, release read lock", Thread.currentThread().getName());
            lock.readLock().unlock();
        }
    }

    @Override
    public int getAccountByIdHitCount() {
        lock.readLock().lock();
        try {
            return lruCache.getCountGetByIdHit();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void putAccount(Account account) {
        LOGGER.info("{} - Enter putAccount!, write lock", Thread.currentThread().getName());
        lock.writeLock().lock();
        try {
            LOGGER.info("{} - Read lock is released, Start putAccount!", Thread.currentThread().getName());
            Thread.sleep(5000);
            lruCache.put(account.getId(), account);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            LOGGER.info("{} - putAccount successfully!, release write lock", Thread.currentThread().getName());
            lock.writeLock().unlock();
        }
    }
}
