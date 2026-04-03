package com.datn.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "chat_lieu")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatLieu {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String maChatLieu;
    @Nationalized
    String tenChatLieu;
    Byte trangThai;

}
