package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.config.JwtProperties;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final ObjectMapper objectMapper;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;
  private final JwtProperties jwtProperties;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) throws IOException {

    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

    String accessToken = jwtTokenProvider.createAccessToken(userDetails.getUserDto());

    String refreshToken = jwtTokenProvider.createRefreshToken(userDetails.getUserDto());

    jwtRegistry.registerJwtInformation(
        new JwtInformation(userDetails.getUserDto(), accessToken, refreshToken)
    );

    ResponseCookie refreshCookie = ResponseCookie
        .from(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, refreshToken)
        .httpOnly(true)
        .secure(false)
        .path("/")
        .maxAge(jwtProperties.refreshTokenExpiration())
        .sameSite("Lax")
        .build();

    response.addHeader(
        HttpHeaders.SET_COOKIE, refreshCookie.toString()
    );

    JwtDto jwtDto = new JwtDto(
        userDetails.getUserDto(), accessToken
    );

    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    objectMapper.writeValue(
        response.getWriter(),
        jwtDto
    );
  }
}
