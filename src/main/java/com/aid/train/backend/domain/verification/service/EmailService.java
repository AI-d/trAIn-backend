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
 * 이메일 발송 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
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
            log.info("인증 이메일 발송 성공: {}", LogMaskingUtil.maskEmail(toEmail));

        } catch (MessagingException e) {
            log.error("인증 이메일 발송 실패: {}, error={}",
                    LogMaskingUtil.maskEmail(toEmail),
                    LogMaskingUtil.maskSensitiveData(e.getMessage()), e);
            throw new TrainException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    private String buildVerificationEmailHtml(String name, String otpCode) {
        // Brand Colors
        final String CALM_BLUE = "#5B7FDB";      // 메인 버튼, 로고 포인트
        final String DEEP_NAVY = "#2C3E5D";      // 헤더, 중요 텍스트
        final String SOFT_WHITE = "#F8F9FB";     // 배경, 카드

        return String.format("""
                <!DOCTYPE html>
                <html lang="ko">
                <body style="font-family: 'Segoe UI', Arial, sans-serif; text-align: center; color: %s; background-color: %s; padding: 20px; margin: 0;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 40px 30px; border-radius: 12px; background-color: #ffffff; box-shadow: 0 4px 12px rgba(44, 62, 93, 0.1);">
                        <h1 style="color: %s; margin-bottom: 20px; font-size: 28px; font-weight: 600;">Dialogym 이메일 인증</h1>
                        <p style="font-size: 16px; margin-bottom: 25px; line-height: 1.6;">안녕하세요, <strong style="color: %s;">%s</strong>님!</p>
                        <p style="font-size: 16px; margin-bottom: 30px; line-height: 1.6; color: #555;">Dialogym 서비스 회원가입을 완료하려면 아래 6자리 인증 코드를 입력해주세요.</p>
                        <div style="background-color: %s; padding: 30px; border-radius: 10px; margin: 30px 0; border: 2px solid %s;">
                            <h2 style="font-size: 36px; letter-spacing: 10px; margin: 0; color: %s; font-weight: bold;">%s</h2>
                        </div>
                        <p style="font-size: 14px; color: #666; margin-top: 25px;">이 코드는 <strong style="color: %s;">15분</strong>간 유효합니다.</p>
                        <p style="font-size: 13px; color: #999; margin-top: 30px; line-height: 1.5;">본인이 요청하지 않으셨다면 이 이메일을 무시해주세요.</p>
                        <hr style="border: 0; border-top: 1px solid #e0e0e0; margin: 35px 0;">
                        <p style="font-size: 12px; color: #aaa;">© 2025 Dialogym. All rights reserved.</p>
                    </div>
                </body>
                </html>
                """,
                DEEP_NAVY,      // body text color
                SOFT_WHITE,     // body background
                DEEP_NAVY,      // h1 color
                CALM_BLUE,      // name strong color
                name,
                SOFT_WHITE,     // code box background
                CALM_BLUE,      // code box border
                CALM_BLUE,      // code color
                otpCode,
                CALM_BLUE       // "15분" strong color
        );
    }
}
