package jombi.freemates.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
    private Integer birth_year;
    private Integer gender;
    @Column(nullable = false)
    private Long phone;
    @Column(nullable = false,unique = true)
    private String nickname;

    private String role;

}
