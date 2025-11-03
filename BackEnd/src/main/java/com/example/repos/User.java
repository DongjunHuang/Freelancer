package com.example.repos;

import java.security.Timestamp;

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
import lombok.Data;

@Entity 
@Table(name="users")
@Data
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
  
  @Enumerated(EnumType.STRING) 
  @Column(nullable=false) 
  private UserStatus status = UserStatus.PENDING;
  
  @CreationTimestamp 
  private Timestamp createdAt;
  
  @UpdateTimestamp 
  private Timestamp updatedAt;
}
