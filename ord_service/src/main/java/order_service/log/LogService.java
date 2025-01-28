package order_service.log;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    public void logAction(String action, String username, String details){
        LogEntry logEntry = new LogEntry();
        logEntry.setAction(action);
        logEntry.setUsername(username);
        logEntry.setTimestamp(LocalDateTime.now());
        logEntry.setDetails(details);
        logRepository.save(logEntry);
    }
}
