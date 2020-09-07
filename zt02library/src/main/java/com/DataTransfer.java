package com;

public class DataTransfer {
    private byte DataBuffer[];
    private int ReadPos=0;
    private int WritePos=0;
    private int BufferLen=0;
    private int DataLen=0;
    
    public DataTransfer(int mBufferLen){
    	DataBuffer=new byte[mBufferLen];
    	BufferLen=mBufferLen;
    }
    
    public void AddData(byte mbyte) {
    	DataBuffer[WritePos]=mbyte;
    	WritePos++;
    	if(DataLen<BufferLen){
    		DataLen++;
    	}
    	if(WritePos==BufferLen){
    		WritePos=0;
    	}
    }
	public void AddData(byte []mbyte,int len){
		int i;
		for(i=0;i<len;i++){
			AddData(mbyte[i]);
		}
	}
    public byte ReadData(){
    	if(DataLen==0){
    		return 0;
    	}
    	byte mbyte=DataBuffer[ReadPos];
    	return mbyte;
    }
    public byte ReadData(int Index){
    	if(DataLen<=Index){
    		return 0;
    	}
    	byte mbyte=DataBuffer[(ReadPos+Index)%BufferLen];
    	return mbyte;    	
    }
    public int DeleteFrontData(){
    	if(DataLen==0){
    		return 0;
    	}
    	ReadPos++;
    	DataLen--;
    	if(ReadPos==BufferLen){
    		ReadPos=0;
    	} 
    	return 1;
    }
    public int GetDataLen(){
    	return DataLen;
    }
    
    public int ReadMultiData(byte [] mbuffer,int Len){
    	if(Len>DataLen){
    		return 0;
    	}
    	
    	int i;
    	for(i=0;i<Len;i++){
    		mbuffer[i]=ReadData();
    		DeleteFrontData();
    	}
    	return 1;
    }
    /** 
     * ����ת��Ϊ�ֽ� 
     *  
     * @param f 
     * @return 
     */  
    public byte[] float2byte(float f) {  
          
        // ��floatת��Ϊbyte[]  
        int fbit = Float.floatToIntBits(f);  
          
        byte[] b = new byte[4];    
        for (int i = 0; i < 4; i++) {    
            b[i] = (byte) (fbit >> (24 - i * 8));    
        }   
          
        // ��ת����  
        int len = b.length;  
        // ����һ����Դ����Ԫ��������ͬ������  
        byte[] dest = new byte[len];  
        // Ϊ�˷�ֹ�޸�Դ���飬��Դ���鿽��һ�ݸ���  
        System.arraycopy(b, 0, dest, 0, len);  
        byte temp;  
        // ��˳λ��i���뵹����i������  
        for (int i = 0; i < len / 2; ++i) {  
            temp = dest[i];  
            dest[i] = dest[len - i - 1];  
            dest[len - i - 1] = temp;  
        }  
          
        return dest;  
          
    }  
      
    /** 
     * �ֽ�ת��Ϊ���� 
     *  
     * @param b �ֽڣ�����4���ֽڣ� 
     * @param index ��ʼλ�� 
     * @return 
     */  
    public float byte2float(byte[] b, int index) {    
        int l;                                             
        l = b[index + 0];                                  
        l &= 0xff;                                         
        l |= ((long) b[index + 1] << 8);                   
        l &= 0xffff;                                       
        l |= ((long) b[index + 2] << 16);                  
        l &= 0xffffff;                                     
        l |= ((long) b[index + 3] << 24);                  
        return Float.intBitsToFloat(l);                    
    }
 }
