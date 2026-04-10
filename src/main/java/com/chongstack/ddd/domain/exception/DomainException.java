package com.chongstack.ddd.domain.exception;

/**
 * 领域异常基类。
 * <p>
 * 用于表达业务规则违反，异常名称应体现业务含义。
 * 建议子类命名如：TaskDependencyCycleException、InvalidTaskScheduleException 等。
 * <p>
 * 使用 RuntimeException 以避免强制 try-catch 污染领域代码。
 */
public class DomainException extends RuntimeException {

    private final String code;

    public DomainException(String message) {
        super(message);
        this.code = null;
    }

    public DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    public DomainException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
