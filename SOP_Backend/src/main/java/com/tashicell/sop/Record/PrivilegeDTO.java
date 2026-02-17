package com.tashicell.sop.Record;

import com.tashicell.sop.Modal.PriviledgeMaster;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeDTO {
    private Long id;
    private String privilegeName;
    
    public PrivilegeDTO(PriviledgeMaster privilege) {
    	this.setId(privilege.getId());
		this.setPrivilegeName(privilege.getPrivilegeName());
    }
}
