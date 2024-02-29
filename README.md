## Account Cache Implementation

### Overview
This repository contains an implementation of an account cache (`AccountCacheImpl`) in Java, along with associated test cases (`AccountCacheImplTest`). The account cache is designed to store account information and provide methods for accessing and updating accounts.

### Account Cache Implementation (`AccountCacheImpl`)
The `AccountCacheImpl` class implements the `AccountCache` interface and provides the following functionality:
- Caching of account objects based on their IDs.
- Subscription mechanism for receiving updates about account changes.
- Retrieval of the top 3 accounts by balance.
- Tracking the number of times an account is accessed by its ID.

### How to Run Test Cases
To run the test cases for the `AccountCacheImpl` class, follow these steps:

1. Clone this repository to your local machine.
2. Open the project in your preferred Java development environment.
3. Navigate to the `src/test/java` directory.
4. Open the `AccountCacheImplTest.java` file.
5. Run the test cases using your IDE's test runner.

### Test Cases Description
The `AccountCacheImplTest` class contains test cases to ensure the correctness of the `AccountCacheImpl` implementation. These test cases cover various scenarios, including:
- Retrieving accounts by ID.
- Retrieving accounts by Absent ID.
- Retrieving top 3 accounts with element number is 0.
- Retrieving top 3 accounts with element number `less` than 3.
- Retrieving top 3 accounts with element number `more` than 3.
- Retrieving hit count with access after retrieving 1 times.
- Retrieving hit count with access after retrieving 0 times.
- The cache will be initiated with 2 accounts. Add 3rd account after `querying` 1st account, the cache will remove the 2nd account even it is added into cache after the 1st one.
- The cache will be initiated with 2 accounts. Add 3rd account after `updating` 1st account, the cache will remove the 2nd account even it is added into cache after the 1st one.
- Demonstrate that a read-lock `prevents` other threads from `writing`, and vice versa.
- Demonstrate that a read-lock `doesn't prevent` other threads from `reading`.
- Demonstrate that a write-lock `prevent` other threads from `writing`.

### Additional Notes
- Ensure that you have Java and Maven installed on your system to run the test cases.
- Review the test case results to verify the correctness of the `AccountCacheImpl` implementation.
- Modify or extend the test cases as needed to cover additional scenarios or edge cases.

This README provides an overview of the `AccountCacheImpl` implementation and instructions for running the associated test cases. For more detailed information, refer to the source code and comments in the `AccountCacheImpl` and `AccountCacheImplTest` classes.