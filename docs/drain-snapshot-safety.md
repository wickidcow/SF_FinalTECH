# Lock-drain snapshot safety

## Baseline and reproduced defect

Inspected published FinalTECH 3.0.14 at `6b828b394f38476efb0c1e4089465adf737df0e2`. The exact `ServerRunnableLockFactory.java` blob is `1eeacd55369d468e0291f922ab8856607c86301a`.

`waitAllTask()` iterated the live `HashMap.keySet()` while waiting for task completion. The factory's normal task-finally path removes keys from that same map. Removing the current or a later key, or adding a new key, can invalidate the active iterator and abort the remaining drain with `ConcurrentModificationException`. The separate-thread regression reproduces removal before predecessor completion; it does not rely on timing sleeps.

The JDK documents both external synchronization for concurrently modified HashMaps and fail-fast collection-view iteration: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html

## Scoped repair

- Copy the current key sequence under the ObjectMap monitor, then release that monitor before looking up and waiting for each future.
- Synchronize paired task/factory-map reads and mutations on that same existing ownership monitor; keep the existing outer multi-key reservation monitor.
- Continue looking up each future when its key is visited. Removed keys are skipped, replacement futures are observed, and newly added keys belong to the next drain invocation.
- Read task counts under the monitor without allocating a key snapshot.
- Retain the protected live `keySet()` bridge and its descriptor; callers using that historical view must hold the ObjectMap monitor. The internal drain no longer uses the view.

The five-second per-future timeout, diagnostic text, execution/interruption propagation, completed-future skipping, key equality, null-map behavior, future/factory identities, and class-keyed cache layout remain unchanged. No waits are performed while this patch holds the ObjectMap monitor. This is not a redesign of shutdown, cancellation, instance-cache publication, or arbitrary external use of the retained live-view bridge.

No item/research IDs, recipes, menus, energy costs, scheduling submissions/delays, migration policy, persistent keys or saved formats change. Candidate version: 3.0.15. Keep this separate from Legacy's bundle-revision-124 pin to already published FinalTECH 3.0.14.

## Permanent regression coverage

`ServerRunnableLockFactoryDrainTest` exercises the actual factory and ObjectMap in twelve cases: empty/completed drains, self-removal, removal of a later key, addition during draining, replacement at an existing key, a shared future under multiple keys, timeout/diagnostic continuation, execution failure, interruption, historical null-map keys, and worker removal before actual future completion. Tests observe existing key iteration order instead of assuming a HashMap sort order. They verify that waits happen outside the ObjectMap monitor and reject unexpected server scheduler/API calls.

## Actual local evidence and remaining validation

The original source and RunnableLockFactory interface copies were verified against their Git blob hashes. An isolated OpenJDK 21 harness compiled the exact original and patched factory plus the same twelve test method bodies with `--release 21 -Xlint:unchecked -Werror`.

- Original: 8 passed, 4 failed with ConcurrentModificationException (directly or wrapped by the waiting future).
- Patched: 12 passed, 0 failed.

External Bukkit/annotation/JUnit APIs were represented by minimal local doubles because the container has no Maven/dependency cache and cannot resolve GitHub. JDK futures, map implementations, threads, reflection and the factory source were real. No harness doubles are committed. These results are focused reproduction evidence, not full Maven/JUnit or real-server validation.

Require the normal repository English audit and build workflow, all existing preservation/API/unchecked-generics guards, real Maven/JUnit tests against Paper 1.21.11, 26.2 and the currently resolved 26.3 API, and universal Java 21 JAR verification before merge. Record the exact tested Legacy dependency source from CI. No old-world, cross-fork rollback or general Folia-safety certification follows from these tests.
