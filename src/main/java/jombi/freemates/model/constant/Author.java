package jombi.freemates.model.constant;

// Swagger DOCS에서 @ApiChangeLog 어노테이션에 사용
public enum Author {
    SUHSAECHAN("서새찬"),
    BAEKJIHOON("백지훈"),
    WISEUNGJAE("위승재"),
    KIMNAYOUNG("김나영"),
    KIMKYUSEOP("김규섭");

    private final String displayName;

    Author(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
