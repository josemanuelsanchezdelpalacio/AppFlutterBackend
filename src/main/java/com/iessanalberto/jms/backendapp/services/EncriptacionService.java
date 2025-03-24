package com.iessanalberto.jms.backendapp.services;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Service
public class EncriptacionService {

    @Value("${encryption.master-key:claveSecretaPredeterminadaParaDesarrollo}")
    private String claveEncriptacion;

    //defino constantes para configuracion de encriptacion AES
    private static final String ALGORITMO = "AES/CBC/PKCS5Padding";
    private static final int LONGITUD_CLAVE = 256;
    private static final int LONGITUD_IV = 16;
    private static final int ITERACIONES = 65536;

    //encripto datos usando el algoritmo AES en modo CBC
    public String encriptar(String datos) {
        if (datos == null) {
            return null;
        }

        try {
            //genero vector de inicializacion aleatorio para AES
            byte[] iv = new byte[LONGITUD_IV];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            //genero clave AES a partir de la clave maestra
            SecretKey claveAES = generarClaveAES();

            //configuro cifrador en modo de encriptacion
            Cipher cifrador = Cipher.getInstance(ALGORITMO);
            cifrador.init(Cipher.ENCRYPT_MODE, claveAES, ivSpec);

            //encripto los datos
            byte[] datosEncriptados = cifrador.doFinal(datos.getBytes(StandardCharsets.UTF_8));

            //codifico IV y datos en Base64
            String ivCodificado = Base64.getEncoder().encodeToString(iv);
            String datosCodificados = Base64.getEncoder().encodeToString(datosEncriptados);

            //retorno datos en formato personalizado
            return "$AES$" + ivCodificado + "$" + datosCodificados;
        } catch (Exception e) {
            throw new RuntimeException("Error al encriptar datos", e);
        }
    }

    //desencripto datos encriptados con el metodo anterior
    public String desencriptar(String cadenaEncriptada) {
        if (cadenaEncriptada == null || !cadenaEncriptada.startsWith("$AES$")) {
            return cadenaEncriptada;
        }

        try {
            //divido la cadena en partes
            String[] partes = cadenaEncriptada.split("\\$");
            if (partes.length != 4) {
                throw new IllegalArgumentException("Formato de datos encriptados invalido");
            }

            //obtengo iv y datos encriptados
            String ivCodificado = partes[2];
            String datosEncriptados = partes[3];

            //decodifico iv y datos encriptados
            byte[] iv = Base64.getDecoder().decode(ivCodificado);
            byte[] encriptado = Base64.getDecoder().decode(datosEncriptados);

            //recreo la clave AES
            SecretKey claveAES = generarClaveAES();

            //configuro cifrador en modo de desencriptacion
            Cipher cifrador = Cipher.getInstance(ALGORITMO);
            cifrador.init(Cipher.DECRYPT_MODE, claveAES, new IvParameterSpec(iv));

            //desencripto los datos
            byte[] datosDesencriptados = cifrador.doFinal(encriptado);

            //retorno los datos desencriptados en texto
            return new String(datosDesencriptados, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error al desencriptar datos", e);
        }
    }

    //genero una clave AES derivada de la clave maestra usando PBKDF2
    private SecretKey generarClaveAES() throws Exception {
        //obtengo la sal a partir de la clave maestra
        byte[] sal = claveEncriptacion.getBytes(StandardCharsets.UTF_8);

        //derivo la clave con PBKDF2
        SecretKeyFactory fabrica = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec especificacion = new PBEKeySpec(
                claveEncriptacion.toCharArray(),
                sal,
                ITERACIONES,
                LONGITUD_CLAVE
        );
        byte[] claveBytes = fabrica.generateSecret(especificacion).getEncoded();

        //retorno clave AES final
        return new SecretKeySpec(claveBytes, "AES");
    }

    //convierto BigDecimal a String en la base de datos
    @Converter
    @Component
    public static class BigDecimalEncryptionConverter implements AttributeConverter<BigDecimal, String> {

        private static EncriptacionService encriptacionService;

        @Autowired
        public void setServicioEncriptacion(EncriptacionService encriptacionService) {
            BigDecimalEncryptionConverter.encriptacionService = encriptacionService;
        }

        @Override
        public String convertToDatabaseColumn(BigDecimal atributo) {
            return atributo != null ? encriptacionService.encriptar(atributo.toString()) : null;
        }

        @Override
        public BigDecimal convertToEntityAttribute(String datosBD) {
            if (datosBD == null) {
                return null;
            }
            String desencriptado = encriptacionService.desencriptar(datosBD);
            return new BigDecimal(desencriptado);
        }
    }

    //convierto String a String en la base de datos con encriptacion
    @Converter
    @Component
    public static class StringEncryptionConverter implements AttributeConverter<String, String> {

        private static EncriptacionService encriptacionService;

        @Autowired
        public void setServicioEncriptacion(EncriptacionService encriptacionService) {
            StringEncryptionConverter.encriptacionService = encriptacionService;
        }

        @Override
        public String convertToDatabaseColumn(String atributo) {
            return atributo != null ? encriptacionService.encriptar(atributo) : null;
        }

        @Override
        public String convertToEntityAttribute(String datosBD) {
            return datosBD != null ? encriptacionService.desencriptar(datosBD) : null;
        }
    }
}


