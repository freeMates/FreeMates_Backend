package jombi.freemates.model.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SearchType {
  NAME,       // placeName 필드 검색
  INTRO,      // introText 필드 검색
  TAG,        // tags(태그 리스트) 검색
  ALL;        // NAME || INTRO || TAG 전부

  public static SearchType defaultType() {
    return ALL;
  }
}
