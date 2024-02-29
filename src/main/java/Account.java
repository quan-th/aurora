public class Account implements Comparable<Account> {


    public long id;
    public long balance;

    public Account(int id, int balance) {
        this.id = id;
        this.balance = balance;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    @Override
    public int compareTo(Account o) {
        return Long.compare(o.getBalance(), this.getBalance());
    }
}