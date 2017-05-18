package org.metaborg.core.testing;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Stack;

/**
 * Used to report the status of tests.
 *
 * This base class performs some additional checks to ensure it is used correctly.
 */
public abstract class TestReporterServiceBase implements ITestReporterService {

    private boolean started = false;
    private final Stack<String> testSuites = new Stack<>();
    @Nullable private String testCase = null;

    /**
     * A test session has started.
     */
    protected abstract void onSessionStarted();
    /**
     * The test session has finished.
     */
    protected abstract void onSessionFinished();
    /**
     * A test suite has started. Test suites can be nested.
     *
     * @param name The name of the test suite.
     */
    protected abstract void onTestSuiteStarted(String name);
    /**
     * The test suite has finished.
     *
     * @param name The name of the test suite.
     */
    protected abstract void onTestSuiteFinished(String name);
    /**
     * A test has started.
     *
     * @param name The name of the test.
     */
    protected abstract void onTestStarted(String name);
    /**
     * The test has finished and failed.
     *
     * @param name The name of the test.
     * @param reason The reason the test failed; or null.
     * @param details The details about the failed test, such as additional messages and the stack trace; or null.
     */
    protected abstract void onTestFailed(String name, @Nullable String reason, @Nullable String details);
    /**
     * The test has finished and passed.
     *
     * @param name The name of the test.
     */
    protected abstract void onTestPassed(String name);
    /**
     * The test has finished and was ignored.
     *
     * @param name The name of the test.
     * @param reason The reason the test was ignored; or null.
     */
    protected abstract void onTestIgnored(String name, @Nullable String reason);

    @Override
    public final void sessionStarted() {
        assertState(!this.started);

        onSessionStarted();

        this.started = true;
    }


    @Override
    public final void sessionFinished() {
        assertState(this.started);

        onSessionFinished();

        this.started = false;
    }

    @Override
    public final void testSuiteStarted(String name) {
        if (name == null)
            throw new IllegalArgumentException("Argument name may not be null.");
        assertState(this.started);
        testSuites.push(name);

        onTestSuiteStarted(name);
    }

    @Override
    public final void testSuiteFinished(String name) {
        if (name == null)
            throw new IllegalArgumentException("Argument name may not be null.");
        assertState(this.started);
        assertState(this.testSuites.peek().equals(name));

        onTestSuiteFinished(name);

        testSuites.pop();
    }

    @Override
    public final void testStarted(String name) {
        if (name == null)
            throw new IllegalArgumentException("Argument name may not be null.");
        assertState(this.started);
        assertState(this.testCase == null);
        this.testCase = name;

        onTestStarted(name);
    }

    @Override
    public final void testFailed(String name, @Nullable String reason, @Nullable String details) {
        if (name == null)
            throw new IllegalArgumentException("Argument name may not be null.");
        assertState(this.started);
        assertState(Objects.equals(this.testCase, name));

        onTestFailed(name, reason, details);

        this.testCase = null;
    }

    @Override
    public final void testPassed(String name) {
        if (name == null)
            throw new IllegalArgumentException("Argument name may not be null.");
        assertState(this.started);
        assertState(Objects.equals(this.testCase, name));

        onTestPassed(name);

        this.testCase = null;
    }

    @Override
    public final void testIgnored(String name, @Nullable String reason) {
        if (name == null)
            throw new IllegalArgumentException("Argument name may not be null.");
        assertState(this.started);
        assertState(Objects.equals(this.testCase, name));

        onTestIgnored(name, reason);

        this.testCase = null;
    }

    /**
     * Asserts that the user is not abusing the reporter, otherwise throws an exception.
     *
     * @param condition The condition to check.
     * @throws IllegalStateException The reporter is being abused.
     */
    private void assertState(boolean condition) {
        if (!condition)
            throw new IllegalStateException("Call not allowed in this state.");
    }
}
