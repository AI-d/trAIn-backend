package com.aid.train.backend.domain.verification.service;

import com.aid.train.backend.global.exception.TrainException;
import com.aid.train.backend.global.exception.enums.ErrorCode;
import com.aid.train.backend.global.util.LogMaskingUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 이메일 발송 관련 비즈니스 로직을 처리하는 서비스 클래스입니다. (리팩토링 버전)
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async // 비동기 처리
    public void sendVerificationEmail(String toEmail, String name, String otpCode) {
        String title = "[Dialogym] 회원가입 이메일 인증";
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(title);
            helper.setText(buildVerificationEmailHtml(name, otpCode), true); // HTML 사용

            javaMailSender.send(mimeMessage);
            log.info("인증 이메일 발송 성공: {}", toEmail);

        } catch (MessagingException e) {
            log.error("인증 이메일 발송 실패: {}, error={}",
                    LogMaskingUtil.maskEmail(toEmail),
                    LogMaskingUtil.maskSensitiveData(e.getMessage()), e);
            throw new TrainException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    private String buildVerificationEmailHtml(String name, String otpCode) {
        return String.format("""
                <!DOCTYPE html>
                <html lang="ko">
                <body style="font-family: Arial, sans-serif; text-align: center; color: #333; background-color: #f9f9f9; padding: 20px;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 30px; border: 1px solid #ddd; border-radius: 12px; background-color: #fff; box-shadow: 0 4px 8px rgba(0,0,0,0.1);">
                        <h1 style="color: #0056b3; margin-bottom: 20px;">Dialogym 이메일 인증</h1>
                        <p style="font-size: 16px; margin-bottom: 25px;">안녕하세요, <strong style="color: #0056b3;">%s</strong>님!</p>
                        <p style="font-size: 16px; margin-bottom: 30px;">Dialogym 서비스 회원가입을 완료하려면 아래 6자리 인증 코드를 입력해주세요.</p>
                        <div style="background-color: #f0f8ff; padding: 25px; border-radius: 8px; margin: 30px 0; border: 1px dashed #0056b3;">
                            <h2 style="font-size: 32px; letter-spacing: 8px; margin: 0; color: #0056b3; font-weight: bold;">%s</h2>
                        </div>
                        <p style="font-size: 14px; color: #777;">이 코드는 <strong>15분</strong>간 유효합니다.</p>
                        <p style="font-size: 12px; color: #aaa; margin-top: 30px;">본인이 요청하지 않으셨다면 이 이메일을 무시해주세요.</p>
                        <hr style="border: 0; border-top: 1px solid #eee; margin: 30px 0;">
                        <p style="font-size: 12px; color: #aaa;">© 2024 Dialogym. All rights reserved.</p>
                    </div>
                </body>
                </html>
                """, name, otpCode);
    }
}