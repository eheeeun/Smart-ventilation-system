package server;

import java.io.*;

public class DeviceProtocol implements Serializable {

   public static final int PT_UNDEFINED = -1;
   public static final int PT_EXIT = 0;
   public static final int PT_REQ_DEV_STATUS = 1;
   public static final int PT_RES_DEV_STATUS = 2;
   public static final int PT_Ventilator_ORDER = 20;
   public static final int LEN_TEMPERATURE = 10;
   public static final int LEN_HUMIDITY = 10;
   public static final int LEN_PROTOCOL_TYPE = 1;
   public static final int LEN_MAX = 1000;
   public static final int LEN_Ventilator_ORDER = 50;
   public static final int PT_USERHUM_SET = 5;
   public static final int PT_ID_ORDER = 6;
   public static final int LEN_DEV_ID = 20;

   public static final int PT_REQ_LOGIN = 9;
   public static final int PT_RES_LOGIN = 7;
   public static final int PT_DEV_LOGIN_RESULT = 8;
   public static final int LEN_DEV_LOGIN_RESULT = 20;
   public static final int LEN_USERHUM_SET = 50;
   
   public static final int PT_RES_FAN = 10;
   public static final int LEN_RES_FAN = 20;
   
   public static final int PT_RESFAN_RESULT = 11;
   public static final int LEN_RES_FAN_RESULT = 20;
   
   public static final int PT_ML = 12;
   public static final int LEN_ML = 50;
   public static final int LEN_ML_ID = 20;
   public static final int LEN_ML_HUMIDITY = 10;
   
   protected int protocolType;
   private byte[] packet;

   public DeviceProtocol() {
      this(PT_UNDEFINED);
   }

   public DeviceProtocol(int protocolType) {
      this.protocolType = protocolType;
      getPacket(protocolType);

   }

   // �������� Ÿ�Կ� ���� ������ �迭 ��ŭ ��Ŷ ����
   public byte[] getPacket(int protocolType) {
      if (packet == null) {
         switch (protocolType) {
         case PT_REQ_DEV_STATUS:
            packet = new byte[LEN_PROTOCOL_TYPE];
            break;
         case PT_RES_DEV_STATUS:
            packet = new byte[LEN_PROTOCOL_TYPE + LEN_TEMPERATURE + LEN_HUMIDITY];
            break;
         case PT_UNDEFINED:
            packet = new byte[LEN_MAX];
            break;
         case PT_EXIT:
            packet = new byte[LEN_PROTOCOL_TYPE];
            break;
         case PT_Ventilator_ORDER:
            packet = new byte[LEN_PROTOCOL_TYPE + LEN_Ventilator_ORDER];
            break;
         case PT_ID_ORDER:
            packet = new byte[LEN_PROTOCOL_TYPE + LEN_DEV_ID + LEN_Ventilator_ORDER];
            break;
         case PT_REQ_LOGIN:
            packet = new byte[LEN_PROTOCOL_TYPE];
            break;
         case PT_RES_LOGIN:
            packet = new byte[LEN_PROTOCOL_TYPE + LEN_DEV_ID];
            break;
         case PT_DEV_LOGIN_RESULT:
            packet = new byte[LEN_PROTOCOL_TYPE + LEN_DEV_LOGIN_RESULT];
            break;         
         case PT_USERHUM_SET:
                packet = new byte[LEN_PROTOCOL_TYPE + LEN_USERHUM_SET];
         case PT_RES_FAN:
            packet = new byte[LEN_PROTOCOL_TYPE + LEN_RES_FAN + LEN_DEV_ID];
            break;
         case PT_RESFAN_RESULT:
             packet = new byte[LEN_PROTOCOL_TYPE + LEN_RES_FAN_RESULT];
             break;
         case PT_ML:
             packet = new byte[LEN_PROTOCOL_TYPE + LEN_ML_ID + LEN_Ventilator_ORDER];
             break;
         }
      }
      packet[0] = (byte) protocolType;
      return packet;
   }

   public byte[] getPacket() {
      return packet;
   }

   public void setPacket(int pt, byte[] buf) {
      packet = null;
      packet = getPacket(pt);
      protocolType = pt;
      System.arraycopy(buf, 0, packet, 0, packet.length);
   }

   public int getProtocolType() {
      return protocolType;
   }

   public void setOrder(String order) {
      System.arraycopy(order.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE, order.trim().getBytes().length);
      packet[LEN_PROTOCOL_TYPE + order.trim().getBytes().length] = '\0';
   }

   public String getOrder() {
      return new String(packet, LEN_PROTOCOL_TYPE, LEN_Ventilator_ORDER).trim();
   }

   public void setProtocolType(int protocolType) {
      this.protocolType = protocolType;
   }

   public String getTemperature() {
      return new String(packet, LEN_PROTOCOL_TYPE + LEN_TEMPERATURE, LEN_HUMIDITY).trim();
   }

   public void setTemperature(String temperature) {
      System.arraycopy(temperature.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE + LEN_TEMPERATURE,
            temperature.trim().getBytes().length);
      packet[LEN_PROTOCOL_TYPE + LEN_TEMPERATURE + temperature.trim().getBytes().length] = '\0';
   }

   public String getHumidity() {
      return new String(packet, LEN_PROTOCOL_TYPE, LEN_TEMPERATURE).trim();
   }

   public void setHumidity(String humidity) {
      System.arraycopy(humidity.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE, humidity.trim().getBytes().length);
      packet[LEN_PROTOCOL_TYPE + humidity.trim().getBytes().length] = '\0';
   }

   public String getDevId() {
      return new String(packet, LEN_PROTOCOL_TYPE, LEN_DEV_ID).trim();
   }

   public void setDevId(String id) {
      System.arraycopy(id.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE, id.trim().getBytes().length);
      packet[LEN_PROTOCOL_TYPE + id.trim().getBytes().length] = '\0';
   }

   public String getDevOrder() {
      return new String(packet, LEN_PROTOCOL_TYPE + LEN_DEV_ID, LEN_Ventilator_ORDER).trim();
   }

   public void setDevOrder(String order) {
      System.arraycopy(order.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE + LEN_DEV_ID,
            order.trim().getBytes().length);
      packet[LEN_PROTOCOL_TYPE + LEN_DEV_ID + order.trim().getBytes().length] = '\0';
   }
   
   public String getLoginResult() {
        return new String(packet, LEN_PROTOCOL_TYPE, LEN_DEV_LOGIN_RESULT).trim();
    }

    public void setLoginResult(String ok) {
        System.arraycopy(ok.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE, ok.trim().getBytes().length);
        packet[LEN_PROTOCOL_TYPE + ok.trim().getBytes().length] = '\0';
    }
    
    public String getUserSetHum() {
        return new String(packet, LEN_PROTOCOL_TYPE, LEN_USERHUM_SET).trim();
    }

    public void setUserSetHum(String sethum) {
        System.arraycopy(sethum.trim().getBytes(),0 , packet, LEN_PROTOCOL_TYPE, sethum.trim().getBytes().length);
        packet[LEN_PROTOCOL_TYPE+sethum.trim().getBytes().length]='\0';
    }
    
    public String getResfan() {
        return new String(packet, LEN_PROTOCOL_TYPE, LEN_RES_FAN).trim();
    }

    public void setResfan(String resfan) {
        System.arraycopy(resfan.trim().getBytes(),0 , packet, LEN_PROTOCOL_TYPE, resfan.trim().getBytes().length);
        packet[LEN_PROTOCOL_TYPE+resfan.trim().getBytes().length]='\0';
    }

    public String getResId() {
        return new String(packet, LEN_PROTOCOL_TYPE+LEN_RES_FAN,LEN_DEV_ID).trim();
    }

    public void setResId(String  password) {
        System.arraycopy(password.trim().getBytes(), 0, packet,LEN_PROTOCOL_TYPE+LEN_RES_FAN,
                password.trim().getBytes().length);
        packet[LEN_PROTOCOL_TYPE+LEN_RES_FAN+password.trim().getBytes().length] = '\0';
    }
    
    public String getResfanResult() {
        return new String(packet, LEN_PROTOCOL_TYPE, LEN_RES_FAN_RESULT).trim();
    }

    public void setResfanResult(String ok) {
        System.arraycopy(ok.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE, ok.trim().getBytes().length);
        packet[LEN_PROTOCOL_TYPE + ok.trim().getBytes().length] = '\0';
    }
    
    public String getMLId() {
	      return new String(packet, LEN_PROTOCOL_TYPE, LEN_ML_ID).trim();
	   }

	   public void setMLId(String id) {
	      System.arraycopy(id.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE, id.trim().getBytes().length);
	      packet[LEN_PROTOCOL_TYPE + id.trim().getBytes().length] = '\0';
	   }

	   public String getMLOrder() {
	      return new String(packet, LEN_PROTOCOL_TYPE + LEN_ML_ID, LEN_Ventilator_ORDER).trim();
	   }

	   public void setMLOrder(String order) {
	      System.arraycopy(order.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE + LEN_ML_ID,
	            order.trim().getBytes().length);
	      packet[LEN_PROTOCOL_TYPE + LEN_ML_ID + order.trim().getBytes().length] = '\0';
	   }
	   
	   public String getMLHumidity() {
		      return new String(packet, LEN_PROTOCOL_TYPE + LEN_ML_ID + LEN_Ventilator_ORDER, LEN_ML_HUMIDITY).trim();
		   }

		   public void setMLHumidity(String Humidity) {
		      System.arraycopy(Humidity.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE + LEN_ML_ID + LEN_Ventilator_ORDER,
		    		  Humidity.trim().getBytes().length);
		      packet[LEN_PROTOCOL_TYPE + LEN_ML_ID + LEN_Ventilator_ORDER + Humidity.trim().getBytes().length] = '\0';
		   }
}