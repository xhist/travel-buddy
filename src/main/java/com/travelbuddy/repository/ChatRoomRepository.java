package com.travelbuddy.repository;

import com.travelbuddy.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Modifying
    @Query("UPDATE ChatRoom r SET r.onlineUsers = r.onlineUsers + :username WHERE r.id = :roomId")
    void addOnlineUser(@Param("roomId") Long roomId, @Param("username") String username);

    @Modifying
    @Query("UPDATE ChatRoom r SET r.onlineUsers = r.onlineUsers - :username WHERE r.id = :roomId")
    void removeOnlineUser(@Param("roomId") Long roomId, @Param("username") String username);
}