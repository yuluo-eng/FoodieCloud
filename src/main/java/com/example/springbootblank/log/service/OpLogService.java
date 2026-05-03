package com.example.springbootblank.log.service;

import com.example.springbootblank.log.entity.OperationLog;
import com.example.springbootblank.log.mapper.OperationLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class OpLogService {

    private static final Logger log = LoggerFactory.getLogger(OpLogService.class);
    private final OperationLogMapper mapper;

    public OpLogService(OperationLogMapper mapper) {
        this.mapper = mapper;
    }

    @Async
    public void log(String operatorType, Long operatorId, String module, String action, String content) {
        try {
            OperationLog entry = new OperationLog();
            entry.setOperatorType(operatorType);
            entry.setOperatorId(operatorId);
            entry.setModule(module);
            entry.setAction(action);
            entry.setContent(content);
            mapper.insert(entry);
        } catch (Exception e) {
            log.warn("写入操作日志失败: {}", e.getMessage());
        }
    }
}
