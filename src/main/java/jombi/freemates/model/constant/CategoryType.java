package jombi.freemates.model.constant;

import java.util.Arrays;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 카카오 API의 카테고리 그룹 코드를 기반으로 한 장소 카테고리 타입
 * @see <a href="https://developers.kakao.com/docs/latest/ko/local/dev-guide#search-by-category-request-category-group-code">카카오 개발자 문서</a>
 */
@AllArgsConstructor
@Getter
public enum CategoryType {
  CAFE("카페", Set.of("CE7")),
  FOOD("먹거리", Set.of("FD6")),
  SHOPPING("쇼핑", Set.of("MT1", "CS2")),
  WALK("산책", Set.of("AT4")),
  PLAY("놀거리", Set.of("CT1")),
  HOSPITAL("병원", Set.of("HP8"));

  private final String label;
  private final Set<String> kakaoCodes;


  public boolean matches(String kakaoCode) {
    return kakaoCodes.contains(kakaoCode);
  }

  public static CategoryType of(String kakaoCode) {
    return Arrays.stream(values())
        .filter(cat -> cat.matches(kakaoCode))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Invalid kakaoCode: " + kakaoCode));
  }
}

