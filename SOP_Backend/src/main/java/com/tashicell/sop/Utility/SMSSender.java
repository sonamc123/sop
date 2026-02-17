package com.tashicell.sop.Utility;

import org.smpp.Data;
import org.smpp.Session;
import org.smpp.TCPIPConnection;
import org.smpp.pdu.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import java.util.Properties;
@Component
public class SMSSender {
    //method to connect to SMSC
    private Session session;
    private int responseStatus =1;
    private Properties props;
    public Integer establishSMSCConnection(){
        try {
            Resource resource = new ClassPathResource("/smscDetails.properties");
            props = PropertiesLoaderUtils.loadProperties(resource);
            
            BindRequest request = null;
			BindResponse response = null;
            
            request = new BindTransciever();
            request.setSystemId(props.getProperty("smscUsername"));
            request.setPassword(props.getProperty("smscPassword"));
            request.setSystemType(props.getProperty("systemType"));
            request.setAddressRange(props.getProperty("addressRange"));
            request.setInterfaceVersion((byte) 0x34);
            
            TCPIPConnection connection = new TCPIPConnection(props.getProperty("smscHost"),Integer.parseInt(props.getProperty("smscPort")));
//            connection.setReceiveTimeout(Long.parseLong(props.getProperty("BIND_TIMEOUT")));
            session = new Session(connection);
            response = session.bind(request);
            if (response.getCommandStatus() == Data.ESME_ROK) {
	            responseStatus = response.getCommandStatus();
			} 
            else {
				System.out.println("Bind failed, code " + response.getCommandStatus());
				closeSMSCConnection();
			}
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return responseStatus;
    }
    //method to check if the connection is alive
    public Integer checkIfConnectionIsAlive(){
        try {
            EnquireLink request = new EnquireLink();
            EnquireLinkResp response = session.enquireLink(request);
            responseStatus = response.getCommandStatus();
            System.out.println("response"+responseStatus);
        } catch (Throwable e) {
            System.out.println("Enquirelink"+responseStatus);
            e.printStackTrace();
        }
        return responseStatus;
    }

    public void closeSMSCConnection(){
        try {
	        UnbindResp response = session.unbind();
            System.out.println("Unbind response " + response.debugString());
        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println("Unbind operation failed. " + e);
        }
    }

    private static Address createAddress(String address) throws WrongLengthOfStringException {
        Address addressInst = new Address();
        addressInst.setTon((byte)2); // national ton
        addressInst.setNpi((byte)8); // national npi
        addressInst.setAddress(address, Data.SM_ADDR_LEN);
        return addressInst;
    }
	
	public void sendFulfillmentSMS(String mobileNo, String messageContent) {
		try {
            final SubmitSM request = new SubmitSM();
            request.setServiceType(props.getProperty("serviceType"));
            request.setShortMessage(messageContent);
            request.setScheduleDeliveryTime(null);
            request.setReplaceIfPresentFlag((byte) 0);
            request.setEsmClass((byte) 0);
            request.setProtocolId((byte) 0);
            request.setPriorityFlag((byte) 0);
            request.setRegisteredDelivery((byte) 1);
            request.setDataCoding((byte) 0);
            request.setSmDefaultMsgId((byte) 0);
            
            request.setDestAddr(createAddress(mobileNo));
            final SubmitSMResp response = session.submit(request);
            responseStatus = response.getCommandStatus();
            
        } catch (Throwable e) {
            e.printStackTrace();
        }
	}
}
