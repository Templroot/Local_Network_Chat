package PGP;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;


public class PGP {
    private PrivateKey privateKey;
    private PublicKey publicKey;
    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PGP() {
        try {
            // Метод getInstance() принимает имя алгоритма шифрования, который будет использоваться.
            // В данном случае мы используем алгоритм RSA.
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            // Инициализируем ключ размером 4096 бит максимальный объем
            // данных, который можно зашифровать таким ключом, равен 512
            generator.initialize(4096);
            // Генерируем ключевую пару
            KeyPair pair = generator.generateKeyPair();
            // Приватный ключ
            privateKey = pair.getPrivate();
            // Публичный ключ
            publicKey = pair.getPublic();
        } catch (Exception ignored) {
        }
    }
        //Метод шифрования
    public String encrypt(String message, PublicKey spkay) throws Exception{
        byte[] messageToBytes = message.getBytes();
        // Создание шифра RSA c режимом шифрования ECB (Electronic Codebook (Режим электронной кодовой книги)) и схемой
        // дополнения PKCS5Padding
        Cipher cipher = Cipher.getInstance("RSA");
        // Инициализация шифра в режиме шифрования
        cipher.init(Cipher.ENCRYPT_MODE, spkay);
        // Шифрование блока данных
        byte[] encryptedBytes = cipher.doFinal(messageToBytes);
        return encode(encryptedBytes);
    }


     // Метод расшифрования
     //param encryptedMessage - сообщение для дешифровки
    public String decrypt(String encryptedMessage) throws Exception {
        byte[] encryptedBytes = decode(encryptedMessage);
        Cipher cipher = Cipher.getInstance("RSA");
        // Инициализация шифра в режиме расшифровки
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedMessage = cipher.doFinal(encryptedBytes);
        return new String(decryptedMessage, "UTF8");
    }
    private byte[] decode(String data){
        return Base64.getDecoder().decode(data);
    }

    private String encode(byte[] data){
        return Base64.getEncoder().encodeToString(data);
    }
}