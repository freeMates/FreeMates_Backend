package jombi.freemates.service.crawler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import jombi.freemates.model.dto.KakaoPlaceCrawlDetail;
import jombi.freemates.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import io.github.bonigarcia.wdm.WebDriverManager;

@RequiredArgsConstructor
@Service
@Slf4j
public class KakaoCrawler {
  private final OkHttpClient client;
  private static final String UA =
      "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
          + "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.2 Safari/605.1.15";
  private static final String baseUrl =
      "https://place.map.kakao.com";

  /** 장소 페이지를 Selenium으로 열어, 생성된 쿠키 전부를 String 으로 만들어 리턴 */
  private String fetchCookies(String placeId) {
    // 헤드리스 Chrome 드라이버 옵션
    ChromeOptions opts = new ChromeOptions();
    opts.addArguments("--no-sandbox", "--disable-gpu","--headless");
    WebDriverManager.chromedriver().setup();
    WebDriver driver = new ChromeDriver(opts);
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

    try {
      // 장소 메인 페이지 로드
      String mainUrl = baseUrl+'/' + placeId;
      driver.get(mainUrl);

      // 페이지가 충분히 로드될 때까지 잠시 대기
      try {
        wait.until(d -> d.manage().getCookieNamed("_T_ANO") != null);
      } catch (TimeoutException e) {
        throw new RuntimeException("쿠키 로딩 실패: 쿠키를 찾을 수 없습니다.", e);
      }
      log.debug("현재 URL = {}", driver.getCurrentUrl());
      log.debug("페이지 타이틀 = {}", driver.getTitle());

      // Selenium 에서 꺼낸 쿠키들을 "name=value; name2=value2; ..." 형태로 직렬화
      return driver.manage().getCookies().stream()
          .map(c -> c.getName() + "=" + c.getValue())
          .collect(Collectors.joining("; "));
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    } finally {
      driver.quit();
    }
  }

  /**
   * OkHttp 로 API 호출 시, 가져온 쿠키를 사용하기 위해
   * 그대로 헤더에 실어서 보낸다.
   */
  private JsonObject fetchJsonWithCookies(String url, String cookieHeader) throws IOException {

    Request req = new Request.Builder()
        .url(url)
        .get()
        .header("User-Agent",        UA)
        .header("Accept",            "application/json, text/plain, */*")
        .header("Accept-Language",   "ko-KR,ko;q=0.9")
        .header("Accept-Encoding",   "gzip, deflate, br")
        .header("Referer",           baseUrl+'/')
        .header("Origin",            baseUrl)
        .header("Sec-Fetch-Site",    "same-origin")
        .header("Sec-Fetch-Mode",    "cors")
        .header("Sec-Fetch-Dest",    "empty")
        .header("pf",                "web")
        .header("Priority",          "u=3, i")
        .header("Cookie",            cookieHeader)
        .build();

    try (Response res = client.newCall(req).execute()) {

      if (!res.isSuccessful()) {
        throw new IOException("API 호출 실패: " + res.code());
      }
      String payload = CommonUtil.nvl(res.body().string(), "");
      return JsonParser.parseString(payload).getAsJsonObject();
    }
  }

  /**
   * placeId 별로 Selenium → OkHttp 로 토큰 쿠키를 자동 획득 & JSON 파싱
   */
  public KakaoPlaceCrawlDetail crawlByPlaceId(String kakaoPlaceId) throws IOException {
    // Selenium 으로 먼저 메인 페이지 열고, 쿠키 획득
    String cookieHeader = fetchCookies(kakaoPlaceId);
    log.debug("쿠키 획득 완료: {}", cookieHeader);

    // panel 데이터를 쿠키와 함께 가져오기
    String panelUrl = "https://place-api.map.kakao.com/places/panel3/" + kakaoPlaceId;
    JsonObject panel = fetchJsonWithCookies(panelUrl, cookieHeader);

    // 이미지·소개 정보 파싱
    String introText = "";
    String imgUrl      = "";

    if (panel.has("my_store") && panel.get("my_store").isJsonObject()) {
      JsonObject myStore = panel.getAsJsonObject("my_store");

      if (myStore.has("mystore_intro")) {
        introText = myStore.get("mystore_intro").getAsString();
      }

      if (myStore.has("main_photo_url")) {
        imgUrl = myStore.get("main_photo_url").getAsString();
      }
    }

    // tags 배열 파싱
//    String description = "";
//
//    if (panel.has("place_add_info") && panel.get("place_add_info").isJsonObject()) {
//      JsonObject addInfo = panel.getAsJsonObject("place_add_info");
//
//      if (addInfo.has("tags") && addInfo.get("tags").isJsonArray()) {
//        JsonArray tagsArray = addInfo.getAsJsonArray("tags");
//
//        description = StreamSupport.stream(tagsArray.spliterator(), false)
//            .map(JsonElement::getAsString)
//            .collect(Collectors.joining(","));
//      }
//    }
    List<String> tags = new ArrayList<>();
    if (panel.has("place_add_info") && panel.get("place_add_info").isJsonObject()) {
      JsonObject addInfo = panel.getAsJsonObject("place_add_info");
      if (addInfo.has("tags") && addInfo.get("tags").isJsonArray()) {
        JsonArray tagsArray = addInfo.getAsJsonArray("tags");
        for (JsonElement el : tagsArray) {
          tags.add(el.getAsString());
        }
      }
    }


    return new KakaoPlaceCrawlDetail(imgUrl, introText, tags);
  }
}
