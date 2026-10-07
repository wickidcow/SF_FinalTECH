# Bounded lock-waiter failure recovery

## Inspected baseline and defect

This batch starts from published FinalTECH 3.0.13, source `005ed7a6009421ae10366cfcf704507e9acf533c`. The inspected `ServerRunnableLockFactory.java` blob is `2773dcc3ac4cc78a170d972ba0b65e211d34cd3e`.

`waitFor(int, T...)` used a do/while traversal and recursively resumed at `++i` after any caught exception. If the final predecessor was cancelled or completed exceptionally after the `isDone()` observation, its `get()` could throw. Recovery then entered with `index == objects.length`. The do/while accessed beyond the array and recursively retried ever-larger indices, eventually ending in `StackOverflowError` rather than returning to the caller. A cancelled or failed future that was already done before the initial check was skipped and did not trigger this path.

The JDK documents `CancellationException` and `ExecutionException` as normal failure results of FutureTask.get(): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/FutureTask.html#get()

## Scoped change

Replace only the private wait traversal with a bounded for loop and per-key exception handling. Each remaining key is visited in the same order; the original diagnostic messages, synchronized get, skipped-completed-task behavior and existing continuation policy are retained. Waiting does not remove another task's lock-map entry or replace its future/factory identity.

No public signatures, generic suppression boundaries, scheduler submissions, delays, item IDs, recipes, menus, energy costs, persistent keys, saved-data formats or configuration files change. Shutdown, interruption policy and cache synchronization are not redesigned in this batch. The candidate version is 3.0.14; it is separate from Legacy's published-3.0.13 bundle pin.

## Permanent regression tests

`ServerRunnableLockFactoryWaitTest` calls the actual factory and its real ObjectMap. Eleven cases cover missing/completed predecessors, cancellation/failure at the final key, cancellation before a later key, failure in the middle, consecutive failures, duplicate keys, the existing empty-input diagnostic, and cancellation/failure after a separate waiting thread enters get(). Tests preserve and compare future/factory identities. Fixture keys are unique and cleaned up; no global production state is reset. API proxies provide only plugin/server/logger/scheduler access and reject unexpected server API calls.

The deterministic race fixture completes a real JDK FutureTask between the observed isDone result and get, rather than inventing exception behavior. Separate-thread cases use CountDownLatch handshakes and bounded waits, not timing sleeps.

## Validation evidence and limits

The original source copy was verified against its Git blob hash. An isolated Java 21 harness compiled that exact source and then the patched source with `--release 21 -Xlint:unchecked -Werror`. It invoked the same eleven test method bodies. The original passed six and failed five with StackOverflowError, directly or wrapped by the waiting future. The patched source passed all eleven.

That local harness used minimal external API/annotation/assertion doubles because the container could not resolve GitHub and had no Maven project dependency cache. Real JDK futures, threads, reflection and the actual factory implementation were used. This is targeted reproduction evidence, not a full Maven/JUnit or Paper-server result. None of the local doubles or runner files are committed.

Before merging, require the normal English audit and release validation, including all existing preservation/API/unchecked-generics guards, the real Maven/JUnit suite against Paper 1.21.11, 26.2 and the currently resolved 26.3 API, and universal Java 21 JAR verification. Record the actual tested addon and Legacy API commits. Compilation and unit tests do not certify old-world upgrades, Folia region safety or cross-fork rollback.
