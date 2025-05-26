package jombi.freemates.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import jombi.freemates.model.dto.BookmarkRequest;
import jombi.freemates.model.dto.BookmarkResponse;
import jombi.freemates.model.dto.CustomUserDetails;
import jombi.freemates.model.postgres.Bookmark;
import jombi.freemates.model.postgres.Member;
import jombi.freemates.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkService {
  private final BookmarkRepository bookmarkRepository;
  private final FileStorageService storage;

  @Transactional
  public BookmarkResponse create(CustomUserDetails customUserDetails, BookmarkRequest req) {
    // 파일 저장 → URL
    String imageUrl = storage.storeImage(req.getFile());
    Member member = customUserDetails.getMember();

    // 엔티티 생성 및 저장
    Bookmark b = Bookmark.builder()
        .member(member)
        .title(req.getTitle())
        .description(req.getDescription())
        .pinColor(req.getPinColor())
        .visibility(req.getVisibility())
        .imageUrl(imageUrl)
        .build();
    bookmarkRepository.save(b);

    // Builder 로 응답 생성
    return BookmarkResponse.builder()
        .memberId(member.getMemberId())       // Member 엔티티의 PK 얻기
        .nickname(member.getNickname())       // Member 엔티티의 닉네임 얻기
        .imageUrl(imageUrl)
        .title(b.getTitle())
        .description(b.getDescription())
        .pinColor(b.getPinColor())
        .visibility(b.getVisibility())
        .build();
  }

  @Transactional(Transactional.TxType.SUPPORTS)
  public List<BookmarkResponse> listByMember(CustomUserDetails customUserDetails) {
    Member member = customUserDetails.getMember();
    return bookmarkRepository.findAllByMember(member).stream()
        .map(b -> BookmarkResponse.builder()
            .memberId(member.getMemberId())
            .nickname(member.getNickname())
            .imageUrl(b.getImageUrl())
            .title(b.getTitle())
            .description(b.getDescription())
            .pinColor(b.getPinColor())
            .visibility(b.getVisibility())
            .build())
        .collect(Collectors.toList());
  }
}
