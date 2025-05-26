package jombi.freemates.model.dto;

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
  private PinColor pinColor;
  private Visibility visibility;
  private MultipartFile file;

}
