package com.example.contract;

/**
 * 单个用例对单个实现的执行结果。失败时携带定位信息（用例名、期望、实际值/实际异常）。
 */
public final class CaseResult {

    public enum Status {
        PASSED,
        FAILED
    }

    private final String caseName;
    private final Status status;
    private final String expectationDescription;
    private final Object actualValue;
    private final Class<? extends Throwable> actualExceptionType;
    private final String actualExceptionMessage;
    private final String failureDetail;

    private CaseResult(
            String caseName,
            Status status,
            String expectationDescription,
            Object actualValue,
            Class<? extends Throwable> actualExceptionType,
            String actualExceptionMessage,
            String failureDetail) {
        this.caseName = caseName;
        this.status = status;
        this.expectationDescription = expectationDescription;
        this.actualValue = actualValue;
        this.actualExceptionType = actualExceptionType;
        this.actualExceptionMessage = actualExceptionMessage;
        this.failureDetail = failureDetail;
    }

    static CaseResult passed(String caseName, String expectationDescription, Object actualValue) {
        return new CaseResult(caseName, Status.PASSED, expectationDescription, actualValue, null, null, null);
    }

    static CaseResult passedException(
            String caseName, String expectationDescription, Class<? extends Throwable> actualType) {
        return new CaseResult(caseName, Status.PASSED, expectationDescription, null, actualType, null, null);
    }

    static CaseResult failed(
            String caseName,
            String expectationDescription,
            Object actualValue,
            Throwable actualException,
            String failureDetail) {
        return new CaseResult(
                caseName,
                Status.FAILED,
                expectationDescription,
                actualValue,
                actualException == null ? null : actualException.getClass(),
                actualException == null ? null : actualException.getMessage(),
                failureDetail);
    }

    public String caseName() {
        return caseName;
    }

    public Status status() {
        return status;
    }

    public boolean isPassed() {
        return status == Status.PASSED;
    }

    public String expectationDescription() {
        return expectationDescription;
    }

    public Object actualValue() {
        return actualValue;
    }

    public Class<? extends Throwable> actualExceptionType() {
        return actualExceptionType;
    }

    public String actualExceptionMessage() {
        return actualExceptionMessage;
    }

    public String failureDetail() {
        return failureDetail;
    }

    /** 实际行为的签名，用于跨实现一致性比较。 */
    public String outcomeSignature() {
        if (actualExceptionType != null) {
            return "exception:" + actualExceptionType.getName();
        }
        return "value:" + actualValue;
    }

    public String describeActual() {
        if (actualExceptionType != null) {
            String message = actualExceptionMessage == null ? "" : ": " + actualExceptionMessage;
            return "threw " + actualExceptionType.getName() + message;
        }
        return "returned <" + actualValue + ">";
    }
}
