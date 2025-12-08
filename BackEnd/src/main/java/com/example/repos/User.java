package com.example.repos;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Entity 
@Table(name="users")
@Data
@Builder
public class User {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) 
  @Column(name = "id")
  private Long userId;
  
  @Column(nullable = false, unique = true)
  private String publicId;

  @Column(nullable=false, unique=true) 
  private String username;
  
  @Column(nullable=false, unique=true) 
  private String email;
  
  @Column(nullable=false) 
  private String password;
  
  @Column(nullable=false) 
  private String roles;
  
  @Enumerated(EnumType.STRING) 
  @Column(nullable=false, length=32)
  private UserStatus status = UserStatus.PENDING;
  
  @CreationTimestamp 
  @Column(name="created_at", updatable=false, nullable=false)
  private Timestamp createdAt;
  
  @UpdateTimestamp 
  @Column(name="updated_at", nullable=false)
  private Timestamp updatedAt;
}
