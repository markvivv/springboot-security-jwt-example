package xieshaohu.example.activemq;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ActiveMQExample {

    private static final Logger logger = LogManager.getLogger(NetMoBoce.class);

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Resource
    private JmsTemplate jmsTemplate;

    @Scheduled(fixedRate = 60000)
    public void moTest() {
        // 设置发布订阅模式
        jmsTemplate.setPubSubDomain(true);

        // 设置消息存活时间
        jmsTemplate.setExplicitQosEnabled(true);
        jmsTemplate.setTimeToLive(300000);
        jmsTemplate.convertAndSend("T-TOPIC", "上行测试", new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message msg) throws JMSException {
                logger.info("Push Message: {}", msg);
                return msg;
            }
        });
    }
}
