package com.tashicell.sop.Record;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
	private String userName;
	private String firstName;
	private String middleName;
	private String lastName;
	private String mobileNo;
	private String email;
	private String password;
	private String confirmPassword;
	private String presentAddress;
	private Integer roleId;
	private Integer department;
	private Integer section;
	private Integer designation;
}
