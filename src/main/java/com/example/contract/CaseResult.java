package com.example.contract;

/**
 * Outcome of running one contract case against one implementation.
 * Records the actual output or the actual exception so failures and
 * cross-implementation inconsistencies can be reported precisely.
 */
public final class CaseResult {

    public enum Status { PASSED, FAILED }

    private final String caseId;
    private final Status status;
    private final String expectation;
    private final Object actualOutput;
    private final Class<? extends Throwable> actualExceptionType;
    private final String actualExceptionMessage;

    private CaseResult(String caseId, Status status, String expectation,
                       Object actualOutput, Throwable actualException) {
        this.caseId = caseId;
        this.status = status;
        this.expectation = expectation;
        this.actualOutput = actualOutput;
        this.actualExceptionType = actualException == null ? null : actualException.getClass();
        this.actualExceptionMessage = actualException == null ? null : actualException.getMessage();
    }

    public static CaseResult passed(String caseId, String expectation,
                                    Object actualOutput, Throwable actualException) {
        return new CaseResult(caseId, Status.PASSED, expectation, actualOutput, actualException);
    }

    public static CaseResult failed(String caseId, String expectation,
                                    Object actualOutput, Throwable actualException) {
        return new CaseResult(caseId, Status.FAILED, expectation, actualOutput, actualException);
    }

    public String caseId() {
        return caseId;
    }

    public Status status() {
        return status;
    }

    public boolean isPassed() {
        return status == Status.PASSED;
    }

    public String expectation() {
        return expectation;
    }

    public Object actualOutput() {
        return actualOutput;
    }

    public Class<? extends Throwable> actualExceptionType() {
        return actualExceptionType;
    }

    public String actualDescription() {
        if (actualExceptionType != null) {
            String description = "exception " + actualExceptionType.getName();
            return actualExceptionMessage == null ? description : description + ": " + actualExceptionMessage;
        }
        return "output " + String.valueOf(actualOutput);
    }

    /** Key used to compare the outcomes of two implementations on the same case. */
    String consistencyKey() {
        if (actualExceptionType != null) {
            return "exception:" + actualExceptionType.getName();
        }
        return "output:" + String.valueOf(actualOutput);
    }

    public String describe() {
        return "case '" + caseId + "' expected " + expectation + " but got " + actualDescription();
    }
}
