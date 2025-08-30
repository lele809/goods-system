package com.shelf.config;

import com.shelf.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.PersistenceException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;

/**
 * 数据库异常全局处理器
 */
@Slf4j
@RestControllerAdvice
public class DatabaseExceptionHandler {

    /**
     * 处理PostgreSQL异常
     */
    @ExceptionHandler(PSQLException.class)
    public ResponseEntity<ApiResponse<Void>> handlePSQLException(PSQLException e) {
        log.error("PostgreSQL数据库异常: {}, SQLState: {}", e.getMessage(), e.getSQLState(), e);
        
        String message = "数据库操作失败";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        
        // 根据不同的SQL状态码返回不同的错误信息
        switch (e.getSQLState()) {
            case "23505": // unique_violation
                message = "数据已存在，请检查是否重复";
                status = HttpStatus.CONFLICT;
                break;
            case "23503": // foreign_key_violation
                message = "关联数据不存在，操作失败";
                status = HttpStatus.BAD_REQUEST;
                break;
            case "23514": // check_violation
                message = "数据格式不符合要求";
                status = HttpStatus.BAD_REQUEST;
                break;
            case "22001": // string_data_right_truncation
                message = "输入数据过长，请缩短内容";
                status = HttpStatus.BAD_REQUEST;
                break;
            case "42P05": // duplicate_prepared_statement
                message = "系统繁忙，请稍后重试";
                status = HttpStatus.SERVICE_UNAVAILABLE;
                break;
            case "25P02": // in_failed_sql_transaction
                message = "事务已中止，请重试操作";
                status = HttpStatus.SERVICE_UNAVAILABLE;
                break;
            case "42P18": // indeterminate_datatype
                message = "查询参数类型错误";
                status = HttpStatus.BAD_REQUEST;
                break;
            default:
                log.error("未处理的PostgreSQL错误: SQLState={}, Message={}", e.getSQLState(), e.getMessage());
                break;
        }
        
        return ResponseEntity.status(status).body(ApiResponse.error(message));
    }

    /**
     * 处理JPA系统异常
     */
    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<ApiResponse<Void>> handleJpaSystemException(JpaSystemException e) {
        log.error("JPA系统异常: {}", e.getMessage(), e);
        
        // 检查是否是由PostgreSQL异常引起的
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof PSQLException) {
                return handlePSQLException((PSQLException) cause);
            }
            cause = cause.getCause();
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("数据库操作异常，请重试"));
    }

    /**
     * 处理事务系统异常
     */
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ApiResponse<Void>> handleTransactionSystemException(TransactionSystemException e) {
        log.error("事务系统异常: {}", e.getMessage(), e);
        
        // 检查是否是由数据库异常引起的
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof PSQLException) {
                return handlePSQLException((PSQLException) cause);
            }
            if (cause instanceof SQLException) {
                return handleSQLException((SQLException) cause);
            }
            cause = cause.getCause();
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("事务处理失败，请重试"));
    }

    /**
     * 处理持久化异常
     */
    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<ApiResponse<Void>> handlePersistenceException(PersistenceException e) {
        log.error("持久化异常: {}", e.getMessage(), e);
        
        // 检查是否是由数据库异常引起的
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof PSQLException) {
                return handlePSQLException((PSQLException) cause);
            }
            cause = cause.getCause();
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("数据持久化失败"));
    }

    /**
     * 处理一般数据访问异常
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccessException(DataAccessException e) {
        log.error("数据访问异常: {}", e.getMessage(), e);
        
        // 检查是否是由数据库异常引起的
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof PSQLException) {
                return handlePSQLException((PSQLException) cause);
            }
            cause = cause.getCause();
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("数据访问失败，请检查操作是否正确"));
    }

    /**
     * 处理MySQL约束违反异常
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleMySQLConstraintViolation(SQLIntegrityConstraintViolationException e) {
        log.error("MySQL约束违反异常: {}, SQLState: {}", e.getMessage(), e.getSQLState(), e);
        
        String message = "数据操作失败";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        // 根据错误码处理MySQL特定错误
        if (e.getErrorCode() == 1062) { // Duplicate entry
            message = "数据已存在，请检查是否重复";
            status = HttpStatus.CONFLICT;
        } else if (e.getErrorCode() == 1452) { // Foreign key constraint
            message = "关联数据不存在，操作失败";
        } else if (e.getErrorCode() == 1406) { // Data too long
            message = "输入数据过长，请缩短内容";
        }
        
        return ResponseEntity.status(status).body(ApiResponse.error(message));
    }

    /**
     * 处理MySQL语法错误异常
     */
    @ExceptionHandler(SQLSyntaxErrorException.class)
    public ResponseEntity<ApiResponse<Void>> handleMySQLSyntaxError(SQLSyntaxErrorException e) {
        log.error("MySQL语法错误: {}, SQLState: {}", e.getMessage(), e.getSQLState(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("查询语句错误，请联系管理员"));
    }

    /**
     * 处理SQL超时异常
     */
    @ExceptionHandler(SQLTimeoutException.class)
    public ResponseEntity<ApiResponse<Void>> handleSQLTimeout(SQLTimeoutException e) {
        log.error("SQL超时异常: {}, SQLState: {}", e.getMessage(), e.getSQLState(), e);
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                .body(ApiResponse.error("查询超时，请稍后重试"));
    }

    /**
     * 处理一般SQL异常
     */
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiResponse<Void>> handleSQLException(SQLException e) {
        log.error("SQL异常: {}, SQLState: {}, ErrorCode: {}", e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        
        // 优先处理特定类型的异常
        if (e instanceof PSQLException) {
            return handlePSQLException((PSQLException) e);
        }
        if (e instanceof SQLIntegrityConstraintViolationException) {
            return handleMySQLConstraintViolation((SQLIntegrityConstraintViolationException) e);
        }
        if (e instanceof SQLSyntaxErrorException) {
            return handleMySQLSyntaxError((SQLSyntaxErrorException) e);
        }
        if (e instanceof SQLTimeoutException) {
            return handleSQLTimeout((SQLTimeoutException) e);
        }
        
        // 根据SQLState处理通用错误
        String sqlState = e.getSQLState();
        if (sqlState != null) {
            switch (sqlState) {
                case "08001": // 连接失败
                case "08003": // 连接不存在
                case "08007": // 事务回滚
                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .body(ApiResponse.error("数据库连接异常，请稍后重试"));
                case "40001": // 死锁
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(ApiResponse.error("系统繁忙，请重试"));
                case "42000": // 语法错误或访问违规
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(ApiResponse.error("请求参数错误"));
            }
        }
        
        // 默认处理
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("数据库操作异常，请稍后重试"));
    }

    /**
     * 处理运行时异常中包含的数据库异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException e) {
        // 检查是否包含数据库相关异常
        if (e.getMessage() != null && 
            (e.getMessage().contains("JDBC exception") || 
             e.getMessage().contains("prepared statement") ||
             e.getMessage().contains("transaction") ||
             e.getMessage().contains("Unable to commit") ||
             e.getMessage().contains("Connection"))) {
            
            log.error("运行时异常包含数据库错误: {}", e.getMessage(), e);
            
            // 尝试提取具体的数据库异常
            Throwable cause = e.getCause();
            while (cause != null) {
                if (cause instanceof PSQLException) {
                    return handlePSQLException((PSQLException) cause);
                }
                if (cause instanceof SQLIntegrityConstraintViolationException) {
                    return handleMySQLConstraintViolation((SQLIntegrityConstraintViolationException) cause);
                }
                if (cause instanceof SQLSyntaxErrorException) {
                    return handleMySQLSyntaxError((SQLSyntaxErrorException) cause);
                }
                if (cause instanceof SQLTimeoutException) {
                    return handleSQLTimeout((SQLTimeoutException) cause);
                }
                if (cause instanceof SQLException) {
                    return handleSQLException((SQLException) cause);
                }
                cause = cause.getCause();
            }
            
            // 如果找不到具体的数据库异常，返回通用错误
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("数据库操作异常，请重试"));
        }
        
        // 对于其他运行时异常，不在这里处理
        throw e;
    }
}