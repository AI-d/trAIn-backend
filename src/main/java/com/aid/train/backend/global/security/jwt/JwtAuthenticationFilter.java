package com.aid.train.backend.global.security.jwt;

import com.aid.train.backend.global.exception.TrainException;
import com.aid.train.backend.global.security.service.CustomUserDetailsService;
import com.aid.train.backend.global.util.LogMaskingUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 모든 API 요청을 가로채 JWT 토큰을 검증하는 필터입니다.
 * <p>
 * HTTP 요청의 'Authorization' 헤더에서 Bearer 토큰을 추출하여 유효성을 검사하고,
 * 유효한 경우 해당 사용자의 인증 정보(UserDetails)를 SecurityContext에 설정합니다.
 * 이를 통해 Controller에서 @AuthenticationPrincipal로 인증된 사용자 정보에 접근할 수 있게 됩니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 실제 필터링 로직을 수행합니다.
     *
     * @param request     HttpServletRequest 객체
     * @param response    HttpServletResponse 객체
     * @param filterChain FilterChain 객체
     * @throws ServletException, IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);

        // 토큰이 유효하고, 현재 SecurityContext에 인증 정보가 없는 경우
        if (StringUtils.hasText(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    Long userId = jwtTokenProvider.getUserIdFromToken(token);
                    UserDetails userDetails = customUserDetailsService.loadUserById(userId);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}",
                            LogMaskingUtil.maskSensitiveData(userDetails.getUsername()),
                            request.getRequestURI());
                }
            } catch (TrainException ex) {
                // 유효하지 않은/만료 토큰 등: 인증 없이 통과시키되, 민감 메시지는 마스킹하여 로깅
                log.trace("JWT 검증 실패: {}", LogMaskingUtil.maskSensitiveData(ex.getMessage()));
            } catch (Exception ex) {
                // 예기치 못한 예외가 500으로 전파되는 것 방지
                log.warn("JWT 처리 중 예외 발생: {}", LogMaskingUtil.maskSensitiveData(ex.getMessage()));
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청 헤더에서 Bearer 토큰을 추출합니다.
     *
     * @param request HttpServletRequest 객체
     * @return 추출된 토큰 문자열 (없거나 형식이 다르면 null)
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}