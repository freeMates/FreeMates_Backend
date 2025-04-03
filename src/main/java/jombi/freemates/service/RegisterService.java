package jombi.freemates.service;

import jombi.freemates.dto.RegisterRequest;
import jombi.freemates.entity.UserEntity;
import jombi.freemates.exception.CustomException;
import jombi.freemates.exception.ErrorCode;
import jombi.freemates.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
@Slf4j
@RequiredArgsConstructor
@Service
public class RegisterService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 회원가입
     */
    public String register(RegisterRequest request) {

        // 1. 중복 아이디 검증
        if (userRepository.existsByUsername(request.getUsername())) {
            log.error("이미 사용중인 아이디 입니다. 요청 아이디: {}", request.getUsername());
            throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
        }

        // 2. 회원가입 완료
        userRepository.save(UserEntity.builder()
                .username(request.getUsername())
                .password(bCryptPasswordEncoder.encode(request.getPassword()))
                .build());

        log.info("회원가입 완료");
        return "회원가입 성공!!";
    }
}