import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.LoggerFactory;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountCacheImplTest {
    private AccountCacheImpl accountCache;

    ListAppender<ILoggingEvent> iLoggingEventListAppender = new ListAppender<>();

    @BeforeEach
    public void setUp() {
        accountCache = new AccountCacheImpl(5);
        Logger logger = (Logger) LoggerFactory.getLogger(AccountCacheImpl.class);
        iLoggingEventListAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(Level.DEBUG);
        logger.addAppender(iLoggingEventListAppender);
        iLoggingEventListAppender.start();
    }

    @Test
    public void testGetAccountById() {
        // setup
        Account account = new Account(1, 1000);
        accountCache.putAccount(account);
        // verify
        assertEquals(account, accountCache.getAccountById(1));
    }

    @Test
    public void testGetNonExistentAccountById() {
        assertNull(accountCache.getAccountById(999));
    }

    @Test
    public void testGetTop3AccountsByBalanceLessThan3Accounts() {
        // setup
        Account account1 = new Account(1, 1000);
        Account account2 = new Account(2, 2000);
        accountCache.putAccount(account1);
        accountCache.putAccount(account2);

        // verify
        assertEquals(2, accountCache.getTop3AccountsByBalance().size());
    }

    @Test
    public void testGetTop3AccountsByBalanceMoreThan3Accounts() {
        // setup
        Account account1 = new Account(1, 1000);
        Account account2 = new Account(2, 2000);
        Account account3 = new Account(3, 3000);
        Account account4 = new Account(4, 4000);
        Account account5 = new Account(5, 5000);
        accountCache.putAccount(account1);
        accountCache.putAccount(account2);
        accountCache.putAccount(account3);
        accountCache.putAccount(account4);
        accountCache.putAccount(account5);

        // exercise
        List<Account> accountList = accountCache.getTop3AccountsByBalance();

        // verify
        assertEquals(3, accountList.size());
        assertEquals(5000, accountCache.getTop3AccountsByBalance().get(0).getBalance());
        assertEquals(4000, accountCache.getTop3AccountsByBalance().get(1).getBalance());
        assertEquals(3000, accountCache.getTop3AccountsByBalance().get(2).getBalance());
    }

    @Test
    public void testLRU_Queries() {
        // setup
        accountCache = new AccountCacheImpl(2);
        Account account1 = new Account(1, 1000);
        Account account2 = new Account(2, 2000);
        Account account3 = new Account(3, 3000);
        accountCache.putAccount(account1);
        accountCache.putAccount(account2);
        accountCache.putAccount(account3);

        // exercise
        List<Account> accountList = accountCache.getTop3AccountsByBalance();

        // verify
        assertEquals(2, accountList.size());
        assertEquals(3000, accountCache.getTop3AccountsByBalance().get(0).getBalance());
        assertEquals(2000, accountCache.getTop3AccountsByBalance().get(1).getBalance());

        // Query account 2
        accountCache.getAccountById(2);

        accountCache.putAccount(new Account(4, 4000));

        // exercise
        accountList = accountCache.getTop3AccountsByBalance();
        assertEquals(2, accountList.size());
        assertEquals(4000, accountCache.getTop3AccountsByBalance().get(0).getBalance());
        assertEquals(2000, accountCache.getTop3AccountsByBalance().get(1).getBalance());
    }

    @Test
    public void testLRU_Update() {
        // setup
        accountCache = new AccountCacheImpl(2);
        Account account1 = new Account(1, 1000);
        Account account2 = new Account(2, 2000);
        Account account3 = new Account(3, 3000);
        accountCache.putAccount(account1);
        accountCache.putAccount(account2);
        accountCache.putAccount(account3);

        // exercise
        List<Account> accountList = accountCache.getTop3AccountsByBalance();

        // verify
        assertEquals(2, accountList.size());
        assertEquals(3000, accountCache.getTop3AccountsByBalance().get(0).getBalance());
        assertEquals(2000, accountCache.getTop3AccountsByBalance().get(1).getBalance());

        // Update account 2
        accountCache.putAccount(new Account(2, 2500));


        // verify
        accountList = accountCache.getTop3AccountsByBalance();
        assertEquals(2, accountList.size());
        assertEquals(3000, accountCache.getTop3AccountsByBalance().get(0).getBalance());
        assertEquals(2500, accountCache.getTop3AccountsByBalance().get(1).getBalance());

        // Add new account 4

        accountCache.putAccount(new Account(4, 4000));
        accountList = accountCache.getTop3AccountsByBalance();
        assertEquals(2, accountList.size());
        assertEquals(4000, accountCache.getTop3AccountsByBalance().get(0).getBalance());
        assertEquals(2500, accountCache.getTop3AccountsByBalance().get(1).getBalance());
    }

    @Test
    public void testGetTop3AccountsByBalanceWithNoAccounts() {
        assertEquals(0, accountCache.getTop3AccountsByBalance().size());
    }

    @Test
    public void testGetAccountByIdHitCountWithAccess() {
        // setup
        Account account = new Account(1, 1000);
        accountCache.putAccount(account);

        assertEquals(0, accountCache.getAccountByIdHitCount());
        // exercise
        accountCache.getAccountById(1);

        // verify
        assertEquals(1, accountCache.getAccountByIdHitCount());
    }

    @Test
    public void testGetAccountByIdHitCountWithNoAccess() {
        // setup
        Account account = new Account(1, 1000);
        accountCache.putAccount(account);

        // verify
        assertEquals(0, accountCache.getAccountByIdHitCount());
    }

    /**
     * Demonstrate that a read lock prevents other threads from writing, and vice versa.
     *
     * @throws InterruptedException
     */
    @Test
    public void testAccessWithMultiThread1() throws InterruptedException {
        // setup
        Thread thread1 = new Thread(() -> accountCache.getTop3AccountsByBalance());
        Thread thread2 = new Thread(() -> accountCache.putAccount(new Account(1, 1000)));

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        List<ILoggingEvent> iLoggingEventList = iLoggingEventListAppender.list;
        if (iLoggingEventList.stream().findFirst().toString().contains("Enter getTop3AccountsByBalance")) {
            long timeStampEnterGetTop3AccountsByBalance = iLoggingEventList.stream()
                    .filter(iLoggingEvent -> iLoggingEvent.toString().contains("Enter getTop3AccountsByBalance"))
                    .findFirst()
                    .get()
                    .getTimeStamp();
            long timeStampStartGetTop3AccountsByBalance = iLoggingEventList.stream()
                    .filter(iLoggingEvent -> iLoggingEvent.toString().contains("Start getTop3AccountsByBalance!"))
                    .findFirst()
                    .get()
                    .getTimeStamp();
            long duration = timeStampStartGetTop3AccountsByBalance - timeStampEnterGetTop3AccountsByBalance;
            assertTrue(duration < 1000);

            long timeStampEnterPutAccount = iLoggingEventList.stream()
                    .filter(iLoggingEvent -> iLoggingEvent.toString().contains("Enter putAccount!"))
                    .findFirst()
                    .get()
                    .getTimeStamp();
            long timeStampStartPutAccount = iLoggingEventList.stream()
                    .filter(iLoggingEvent -> iLoggingEvent.toString().contains("Start putAccount!"))
                    .findFirst()
                    .get()
                    .getTimeStamp();
            duration = timeStampStartPutAccount - timeStampEnterPutAccount;
            assertTrue(duration > 5000 && duration < 6000);
        } else {
            long timeStampEnterPutAccount = iLoggingEventList.stream()
                    .filter(iLoggingEvent -> iLoggingEvent.toString().contains("Enter putAccount!"))
                    .findFirst()
                    .get()
                    .getTimeStamp();
            long timeStampStartPutAccount = iLoggingEventList.stream()
                    .filter(iLoggingEvent -> iLoggingEvent.toString().contains("Start putAccount!"))
                    .findFirst()
                    .get()
                    .getTimeStamp();
            long duration = timeStampStartPutAccount - timeStampEnterPutAccount;
            assertTrue(duration < 1000);

            long timeStampEnterGetTop3AccountsByBalance = iLoggingEventList.stream()
                    .filter(iLoggingEvent -> iLoggingEvent.toString().contains("Enter getTop3AccountsByBalance"))
                    .findFirst()
                    .get()
                    .getTimeStamp();
            long timeStampStartGetTop3AccountsByBalance = iLoggingEventList.stream()
                    .filter(iLoggingEvent -> iLoggingEvent.toString().contains("Start getTop3AccountsByBalance!"))
                    .findFirst()
                    .get()
                    .getTimeStamp();
            duration = timeStampStartGetTop3AccountsByBalance - timeStampEnterGetTop3AccountsByBalance;
            assertTrue(duration > 5000 && duration < 6000);
        }
    }

    /**
     * Demonstrate that a read lock doesn't prevent other threads from reading.
     *
     * @throws InterruptedException
     */
    @Test
    public void testAccessWithMultiThread2() throws InterruptedException {
        // setup
        Thread thread1 = new Thread(() -> accountCache.getTop3AccountsByBalance());
        Thread thread2 = new Thread(() -> accountCache.getTop3AccountsByBalance());

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        List<ILoggingEvent> iLoggingEventList = iLoggingEventListAppender.list;
        List<Long> timeStampStartGetTop3AccountsByBalanceList = iLoggingEventList.stream()
                .filter(iLoggingEvent -> iLoggingEvent.toString().contains("Start getTop3AccountsByBalance"))
                .map(ILoggingEvent::getTimeStamp)
                .sorted()
                .collect(Collectors.toList());

        // duration between 2 times logging "Start getTop3AccountsByBalance" < 100
        // which means threads can read from resource at the same time
        long duration = timeStampStartGetTop3AccountsByBalanceList.get(1)
                - timeStampStartGetTop3AccountsByBalanceList.get(0);
        assertTrue(duration < 100);

        long timeStampEnterGetTop3AccountsByBalance = iLoggingEventList.stream()
                .filter(iLoggingEvent -> iLoggingEvent.toString().contains("Enter getTop3AccountsByBalance"))
                .findFirst()
                .get()
                .getTimeStamp();

        long timeStampStartGetTop3AccountsByBalance = iLoggingEventList.stream()
                .filter(iLoggingEvent -> iLoggingEvent.toString().contains("Start getTop3AccountsByBalance!"))
                .findFirst()
                .get()
                .getTimeStamp();

        duration = timeStampStartGetTop3AccountsByBalance - timeStampEnterGetTop3AccountsByBalance;
        assertTrue(duration < 1000);
    }
}

