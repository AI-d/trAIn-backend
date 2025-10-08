package com.aid.train.backend.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Provider {
    LOCAL("로컬", "email"),
    KAKAO("카카오", "https://kauth.kakao.com"),
    GOOGLE("구글", "https://accounts.google.com"),
    NAVER("네이버", "https://nid.naver.com");

    private final String displayName;
    private final String authUrl;
}
