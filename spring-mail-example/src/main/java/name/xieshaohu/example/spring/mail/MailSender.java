package name.xieshaohu.example.spring.mail;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.mail.internet.MimeMessage;

@Component
public class MailSender {

    private Logger logger = LogManager.getLogger(MailSender.class);

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${lance.mail.sender}")
    private static String MAIL_SENDER = "xiesh@51bsi.com";

    @PostConstruct
    public void sendHtmlMail() {
        MimeMessage mimeMailMessage = null;
        try {
            mimeMailMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true);
            mimeMessageHelper.setFrom(MAIL_SENDER);
            mimeMessageHelper.setTo("xieshaohu@xxx.com");
            mimeMessageHelper.setSubject("test测试邮件！");
            StringBuilder sb = new StringBuilder();
            sb.append("<h1>SpringBoot邮件HTML</h1>")
                    .append("<p style='color:#F00'>你是真的太棒了！</p>")
                    .append("<p style='text-align:right'>右对齐</p>");
            mimeMessageHelper.setText(sb.toString(), true);
            javaMailSender.send(mimeMailMessage);
        } catch (Exception e) {
            logger.error("邮件发送失败", e);
        }
    }

}
