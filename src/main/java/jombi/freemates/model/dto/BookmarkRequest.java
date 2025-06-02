package jombi.freemates.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jombi.freemates.model.constant.PinColor;
import jombi.freemates.model.constant.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkRequest {

  private String title;
  private String description;
  @Schema(implementation = PinColor.class)
  private PinColor pinColor;
  @Schema(implementation = Visibility.class)
  private Visibility visibility;


}
