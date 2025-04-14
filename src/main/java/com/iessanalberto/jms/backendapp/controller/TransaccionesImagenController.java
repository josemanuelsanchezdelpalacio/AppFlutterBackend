package com.iessanalberto.jms.backendapp.controller;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.*;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Optional;

@RestController
@RequestMapping("/api/transacciones/images")
public class TransaccionesImagenController {

    //directorio de subida de archivos configurado en las propiedades
    @Value("${file.upload-dir}")
    private String directorioSubida;

    //metodo para obtener una imagen por su nombre
    @GetMapping("/{imageName:.+}")
    public ResponseEntity<Resource> obtenerImagen(@PathVariable String imageName) {
        try {
            //decodifico el nombre de la imagen
            String nombreImagenDecodificado = URLDecoder.decode(imageName, StandardCharsets.UTF_8.toString());

            //compruebo que la imagen está dentro del directorio de subida
            Path rutaBase = Paths.get(directorioSubida).toAbsolutePath().normalize();
            Path rutaImagen = rutaBase.resolve(nombreImagenDecodificado).normalize();

            //compruebo que la ruta resuelta esta dentro del directorio base
            if (!rutaImagen.startsWith(rutaBase)) {
                return ResponseEntity.badRequest().build();
            }

            //creo el recurso de la imagen
            Resource recurso = new UrlResource(rutaImagen.toUri());

            //compruebo si el recurso existe y es legible
            if (recurso.exists() && recurso.isReadable()) {
                //determino el tipo de contenido
                String tipoContenido = determinarTipoContenido(nombreImagenDecodificado);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(tipoContenido))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + recurso.getFilename() + "\"")
                        .body(recurso);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    //metodo para determinar el tipo de contenido de la imagen
    private String determinarTipoContenido(String nombreArchivo) {
        try {
            Path ruta = Paths.get(nombreArchivo);
            //intento obtener el tipo de contenido del sistema de archivos
            String tipoContenido = Files.probeContentType(ruta);

            if (tipoContenido == null) {
                String extension = Optional.ofNullable(FilenameUtils.getExtension(nombreArchivo))
                        .map(String::toLowerCase)
                        .orElse("");

                //mapeo de extensiones
                switch (extension) {
                    case "jpg":
                    case "jpeg":
                        return "image/jpeg";
                    case "png":
                        return "image/png";
                    case "gif":
                        return "image/gif";
                    case "webp":
                        return "image/webp";
                    default:
                        return "application/octet-stream";
                }
            }
            return tipoContenido;
        } catch (Exception e) {
            //devuelvo tipo de contenido generico en caso de error
            return "application/octet-stream";
        }
    }

    //metodo con soporte CORS para obtener imagen
    @CrossOrigin(origins = "*")
    public ResponseEntity<Resource> obtenerImagenConCORS(@PathVariable String imageName) {
        return obtenerImagen(imageName);
    }
}



