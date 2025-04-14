package jombi.freemates.model.constant;

// Swagger DOCS에서 @ApiChangeLog 어노테이션에 사용
public enum Author {
  SUHSAECHAN("서새찬"),
  LEEDAYE("이다예");

  private final String displayName;

  Author(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
