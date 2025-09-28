package com.barda.infra.serverlog;

import static com.barda.infra.perf.PerfEvent.SERVER_LOG_BATCH_INSERT;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.barda.infra.perf.PerfHelper;

import io.micrometer.core.instrument.Tags;

/**
 * 服务器日志服务类。
 * 它使用并发队列来存储日志并定期将它们插入到数据库中。
 */
@Service
public class ServerLogService {

    /**
     * 服务器日志存储库。
     */
    @Autowired
    private ServerLogRepository serverLogRepository;

    /**
     * 性能帮助器。
     */
    @Autowired
    private PerfHelper perfHelper;

    /**
     * 存储服务器日志的并发队列。
     */
    private volatile Queue<ServerLog> serverLogs = new ConcurrentLinkedQueue<>();

    /**
     * 记录服务器日志。
     *
     * @param serverLog 要记录的服务器日志
     */
    public void record(ServerLog serverLog) {
        serverLogs.add(serverLog);
    }

    /**
     * 定期插入存储在并发队列中的服务器日志。
     * 该方法每秒执行一次。
     */
    @Scheduled(initialDelay = 1, fixedRate = 1, timeUnit = TimeUnit.SECONDS)
    private void scheduledInsert() {
        // 如果并发队列为空，则返回
        if (CollectionUtils.isEmpty(serverLogs)) {
            return;
        }
        // 取出并发队列中的所有日志，并创建一个新的并发队列
        var tmp = serverLogs;
        serverLogs = new ConcurrentLinkedQueue<>();
        // 批量插入日志并收集结果
        serverLogRepository.saveAll(tmp)
                .collectList()
                .subscribe(result -> perfHelper.count(SERVER_LOG_BATCH_INSERT, Tags.of("size", String.valueOf(result.size()))));
    }
}
