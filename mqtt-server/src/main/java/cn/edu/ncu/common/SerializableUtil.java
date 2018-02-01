package cn.edu.ncu.common;

import java.io.*;

public class SerializableUtil {

    public byte[] encode(Object object){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }


    public Object decode(byte[] bytes){
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = null;
        Object object = null;
        try {
            ois = new ObjectInputStream(bais);
            object = ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return object;
    }
}
