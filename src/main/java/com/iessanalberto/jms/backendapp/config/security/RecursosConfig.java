package com.iessanalberto.jms.backendapp.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RecursosConfig implements WebMvcConfigurer {

    //obtengo el directorio de subida de archivos desde la configuración
    @Value("${file.upload-dir}")
    private String directorioSubida;

    //configuro el manejador de recursos estáticos para servir archivos desde el directorio de subida
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registro) {
        //verifico si la ruta termina con '/' para asegurar una correcta concatenación
        String ruta = directorioSubida.endsWith("/") ? directorioSubida : directorioSubida + "/";

        //añado el manejador de recursos para la ruta /uploads/**
        registro.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + ruta);

        //imprimo la configuración en la consola para depuración
        System.out.println("Manejador de recursos configurado para: file:" + ruta);
    }
}


