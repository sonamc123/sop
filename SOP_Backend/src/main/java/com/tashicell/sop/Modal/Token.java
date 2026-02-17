package com.tashicell.sop.Modal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sop_token")
public class Token {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;
	
	@Column(unique = true, columnDefinition = "LONGTEXT")
	public String token;
	
	@Enumerated(EnumType.STRING)
	public TokenType tokenType = TokenType.BEARER;
	
	public Boolean revoked;
	
	public Boolean expired;  
	  
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	public User user;
}
