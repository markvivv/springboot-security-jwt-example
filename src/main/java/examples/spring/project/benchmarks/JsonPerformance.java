package examples.spring.project.benchmarks;

import examples.spring.project.Body;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@Controller
@RequestMapping(value = "/benchmarks")
public class JsonPerformance {

    private final Logger logger = LogManager.getLogger(getClass());

    @PostMapping(value = "/post_dev_info")
    public ResponseEntity postDevInfo(@Valid @RequestBody Map<String, Object> param) {
        logger.debug("设备信息: {}", param);
        try {
            return ok(Body.build().ok("设备信息上报成功。", param));
        } catch (Exception e) {
            return ok(Body.build().fail("获取设备信息发生未知错误。" + e.getMessage()));
        }
    }
}
