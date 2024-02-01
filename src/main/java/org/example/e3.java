package org.example;

import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.zip.*;

public class e3 {

    public static void main(String[] args) {
        //creamos una lsita con 10 URL Random
        List<String> urls = List.of(
                "http://www.example.org/",
                "https://www.example.com/bottle",
                "https://www.example.com/box",
                "https://www.example.com/pen",
                "https://www.example.com/pencil",
                "https://www.example.com/phone",
                "https://www.example.com/table",
                "https://www.example.com/tv",
                "https://www.example.com/watch",
                "https://www.example.com/water"
        );

        //creamos un executor con un pool de 10 hilos
        ExecutorService executor = Executors.newFixedThreadPool(urls.size());
        List<CompletableFuture<Path>> futures = new ArrayList<>();

        //descargamos las paginas web y las guardamos en un archivo
        for (String url : urls) {
            CompletableFuture<Path> future = CompletableFuture.supplyAsync(() -> descargarPagina(url), executor)
                    .thenApply(e3::guardarArchivo);
            futures.add(future);
        }

        //esperamos a que todas las descargas se completen
        CompletableFuture<Void> todasLasDescargas = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        todasLasDescargas.thenRun(() -> {
            try {
                //una vez descargadas las comprimimos
                comprimirArchivos(futures.stream().map(CompletableFuture::join).collect(Collectors.toList()), "paginas_comprimidas.zip");
                System.out.println("Compresión completada.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).join();//esperamos a que se completen todas las tareas del hilo

        executor.shutdown();
    }

    //metodo para descargar el contenido de la pagina web
    private static String descargarPagina(String url) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error al descargar la página: " + url, e);
        }
    }

    //metodo para guardar el archivo
    private static Path guardarArchivo(String contenido) {
        try {
            Path tempFile = Files.createTempFile("pagina", ".html");
            Files.writeString(tempFile, contenido);
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo", e);
        }
    }

    //metodo para comprimir los archivos
    private static void comprimirArchivos(List<Path> archivos, String nombreArchivoZip) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(nombreArchivoZip))) {
            for (Path archivo : archivos) {
                zos.putNextEntry(new ZipEntry(archivo.getFileName().toString()));
                Files.copy(archivo, zos);
                zos.closeEntry();
            }
        }
    }
}

