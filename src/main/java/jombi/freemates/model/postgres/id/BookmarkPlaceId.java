package jombi.freemates.model.postgres.id;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkPlaceId implements Serializable {
  private UUID bookmarkId;
  private UUID placeId;
}
