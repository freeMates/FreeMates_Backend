package jombi.freemates.util.object;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;
import jombi.freemates.model.constant.HashType;
import jombi.freemates.model.postgres.BasePostgresEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class HashRegistry extends BasePostgresEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID hashRegistryId;

  @Enumerated(EnumType.STRING)
  @Column(unique = true, nullable = false)
  private HashType hashType;

  @Column(nullable = false)
  private String hashValue;

  private String message;
}
